package com.machiav3lli.backup.ui.item

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.preferences.publicPreferences
import com.machiav3lli.backup.tracePrefs
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.getPrivateSharedPrefs
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class Pref(
    var key: String,
    val private: Boolean = false,
    val defaultValue: Any? = null,
    @StringRes val titleId: Int,
    val summary: String? = null,
    @StringRes val summaryId: Int,
    val icon: ImageVector? = null,
    val iconTint: Color?,
    val enableIf: (() -> Boolean)? = null,
    val onChanged: ((Pref) -> Unit)? = null,
    var group: String = "",
) {
    companion object {

        val prefGroups: MutableMap<String, MutableList<Pref>> = mutableMapOf()
        val prefs get() = prefGroups.values.flatten()
        var lockedActions = 0

        val prefChangeListeners = mutableStateMapOf<Pref, (pref: Pref) -> Unit>()
        private fun onPrefChange(name: String) {
            prefChangeListeners.forEach { (pref, listener) ->
                listener(pref)
            }
            prefs.find { it.key == name }?.let { pref ->
                pref.onChanged?.let { onChanged ->
                    MainScope().launch {
                        while (lockedActions > 0)
                            delay(500)
                        onChanged(pref)
                    }
                }
            }
        }

        private fun getPrefs(private: Boolean = false) =
            if (private)
                OABX.context.getPrivateSharedPrefs()
            else
                OABX.context.getDefaultSharedPreferences()

        fun prefFlag(name: String, default: Boolean, private: Boolean = false) =
            try {
                getPrefs(private).getBoolean(name, default)
            } catch (e: Throwable) {
                default
            }

        fun setPrefFlag(name: String, value: Boolean, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = $value" }
            getPrefs(private).edit().putBoolean(name, value).apply()
            onPrefChange(name)
        }

        fun prefString(name: String, default: String, private: Boolean = false) =
            try {
                getPrefs(private).getString(name, default) ?: default
            } catch (e: Throwable) {
                default
            }

        fun setPrefString(name: String, value: String, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = '$value'" }
            getPrefs(private).edit().putString(name, value).apply()
            onPrefChange(name)
        }

        fun prefInt(name: String, default: Int, private: Boolean = false) =
            try {
                getPrefs(private).getInt(name, default)
            } catch (e: Throwable) {
                default
            }

        fun setPrefInt(name: String, value: Int, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = $value" }
            getPrefs(private).edit().putInt(name, value).apply()
            onPrefChange(name)
        }

        private val toBeEscaped =
            Regex("""[\\"\n\r\t]""")      // blacklist, only escape those that are necessary

        private val toBeUnescaped =
            Regex("""\\(.)""")      // blacklist, only escape those that are necessary

        fun escape(value: String): String {
            return value.replace(toBeEscaped) {
                when (it.value) {
                    "\n" -> "\\n"
                    "\r" -> "\\r"
                    "\t" -> "\\t"
                    else -> "\\${it.value}"
                }
            }
        }

        fun unescape(value: String): String {
            return value.replace(toBeUnescaped) { match ->
                match.groupValues[1].let {
                    when (it) {
                        "n"  -> "\n"
                        "r"  -> "\r"
                        "t"  -> "\t"
                        else -> it
                    }
                }
            }
        }

        fun toSimpleFormat(entries: Map<String, Any>): String {
            return entries.toSortedMap().mapNotNull {
                when (it.value) {
                    is String  -> it.key to "\"" + escape(it.value as String) + "\""
                    is Int     -> it.key to (it.value as Int).toString()
                    is Boolean -> it.key to (it.value as Boolean).toString()
                    else       -> null
                }
            }.joinToString("\n") { (key, value) ->
                "$key: $value"
            }
        }

        fun fromSimpleFormat(serialized: String): Map<String, Any> {
            val map = mutableMapOf<String, Any>()
            serialized.lineSequence().forEach {
                var (key, value) = it.split(":", limit = 2)
                value = value.trim()
                runCatching {
                    when {
                        value.startsWith('"')
                                && value.endsWith('"') -> {
                            value = unescape(value.removeSurrounding("\""))
                            map.put(key, value)
                        }

                        value == "true"                -> {
                            map.put(key, true)
                        }

                        value == "false"               -> {
                            map.put(key, false)
                        }

                        else                           -> {
                            map.put(key, value.toInt())
                        }
                    }
                }
            }
            return map
        }

        fun preferencesToSerialized(): String {

            val prefs: Map<String, Any> =
                publicPreferences().mapNotNull { pref ->
                    try {
                        when (pref) {
                            // order from derived to base classes (otherwise base would obscure derived)
                            is EnumPref    -> pref.key to pref.value
                            is ListPref    -> pref.key to pref.value
                            //is PasswordPref -> pref.key to pref.value     // don't store
                            is StringPref  -> pref.key to pref.value
                            is BooleanPref -> pref.key to pref.value
                            is IntPref     -> pref.key to pref.value
                            else           -> null
                        }
                    } catch (e: Throwable) {
                        LogsHandler.unexpectedException(e)
                        null
                    }
                }.toMap()

            val serialized = try {
                //OABX.toSerialized(OABX.prefsSerializer, prefs)
                toSimpleFormat(prefs)
            } catch (e: Throwable) {
                LogsHandler.unexpectedException(e)
                ""
            }

            return serialized
        }

        fun preferencesFromSerialized(serialized: String) {

            val prefs = fromSimpleFormat(serialized)
            //OABX.fromSerialized<Map<String, Any>>(serialized)

            synchronized(Pref) { lockedActions++ }
            prefs.forEach { (key, value) ->
                when (value) {
                    is String  -> setPrefString(key, value)
                    is Int     -> setPrefInt(key, value)
                    is Boolean -> setPrefFlag(key, value)
                }
            }
            synchronized(Pref) { lockedActions-- }
        }
    }

    init {
        try {
            val (g, k) = key.split(".", limit = 2)
            if (k.isNotEmpty()) {
                group = g
                key = k
            }
        } catch (e: Throwable) {
            // ignore
        }
        //Timber.d("add pref $group - $key")

        prefGroups.getOrPut(group) { mutableListOf() }.add(this)
    }

    override fun toString(): String = ""
}

// keep all other Pref classes final, because they are used in when clauses
// and derived classes would need to come first, which is worth more than the savings

class BooleanPref(
    key: String,
    private: Boolean = false,
    defaultValue: Boolean,
    @StringRes summaryId: Int = -1,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : Pref(
    key = key,
    private = private,
    defaultValue = defaultValue,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
) {
    var state: Boolean? by mutableStateOf(null)
    var value: Boolean
        get() {
            if (state == null)
                state = prefFlag(key, defaultValue as Boolean, private)
            return state!!
        }
        set(value) {
            state = value
            setPrefFlag(key, value, private)
        }

    override fun toString(): String = value.toString()
}

class IntPref(
    key: String,
    private: Boolean = false,
    defaultValue: Int,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: List<Int>,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : Pref(
    key = key,
    private = private,
    defaultValue = defaultValue,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
) {
    var state: Int? by mutableStateOf(null)
    var value: Int
        get() {
            if (state == null)
                state = prefInt(key, defaultValue as Int, private)
            return state!!
        }
        set(value) {
            state = value
            setPrefInt(key, value, private)
        }

    override fun toString(): String = value.toString()
}

open class StringPref(
    key: String,
    private: Boolean = false,
    defaultValue: String,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : Pref(
    key = key,
    private = private,
    defaultValue = defaultValue,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
) {
    var state: String? by mutableStateOf(null)
    var value: String
        get() {
            if (state == null)
                state = prefString(key, defaultValue as String, private)
            return state!!
        }
        set(value) {
            state = value
            setPrefString(key, value, private)
        }

    override fun toString(): String = value.toString()
}

class PasswordPref(
    key: String,
    private: Boolean = true,
    defaultValue: String,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : StringPref(
    key = key,
    private = private,
    defaultValue = defaultValue,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
)


class ListPref(
    key: String,
    private: Boolean = false,
    defaultValue: String,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: Map<String, String>,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : StringPref(
    key = key,
    private = private,
    defaultValue = defaultValue,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
)


class EnumPref(
    key: String,
    private: Boolean = false,
    defaultValue: Int,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: Map<Int, Int>,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : Pref(
    key = key,
    private = private,
    defaultValue = defaultValue,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
) {
    var state: Int? by mutableStateOf(null)
    var value: Int
        get() {
            if (state == null)
                state = prefInt(key, defaultValue as Int, private)
            return state!!
        }
        set(value) {
            state = value
            setPrefInt(key, value, private)
        }

    override fun toString(): String = value.toString()
}


class LinkPref(
    key: String,
    private: Boolean = false,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : Pref(
    key = key,
    private = private,
    defaultValue = null,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
)


class LaunchPref(
    key: String,
    private: Boolean = false,
    @StringRes titleId: Int = -1,
    summary: String? = null,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
    val onClick: () -> Unit = {},
) : Pref(
    key = key,
    private = private,
    defaultValue = null,
    titleId = titleId,
    summary = summary,
    summaryId = summaryId,
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
)
