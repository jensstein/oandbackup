package com.machiav3lli.backup.ui.item

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.preferences.publicPreferences
import com.machiav3lli.backup.tracePrefs
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.getPrivateSharedPrefs
import kotlinx.serialization.Contextual

open class Pref(
    var key: String,
    val private: Boolean = false,
    val summary: String? = null,
    @StringRes val titleId: Int,
    @StringRes val summaryId: Int,
    val defaultValue: Any? = null,
    val icon: ImageVector? = null,
    val iconTint: Color?,
    val enableIf: (() -> Boolean)? = null,
    var group: String = "",
) {
    companion object {

        val prefGroups: MutableMap<String, MutableList<Pref>> = mutableMapOf()

        val prefChangeListeners = mutableStateMapOf<Pref, (pref: Pref) -> Unit>()
        fun onPrefChange() {
            prefChangeListeners.forEach {
                it.value(it.key)
            }
        }

        fun getPrefs(private: Boolean = false) =
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
            getPrefs(private).edit().putBoolean(name, value).apply().also { onPrefChange() }
        }

        fun prefString(name: String, default: String, private: Boolean = false) =
            try {
                getPrefs(private).getString(name, default) ?: default
            } catch (e: Throwable) {
                default
            }

        fun setPrefString(name: String, value: String, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = '$value'" }
            getPrefs(private).edit().putString(name, value).apply().also { onPrefChange() }
        }

        fun prefInt(name: String, default: Int, private: Boolean = false) =
            try {
                getPrefs(private).getInt(name, default)
            } catch (e: Throwable) {
                default
            }

        fun setPrefInt(name: String, value: Int, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = $value" }
            getPrefs(private).edit().putInt(name, value).apply().also { onPrefChange() }
        }

        val toBeEscaped =
            Regex("""[\\"\n\r\t]""")      // blacklist, only escape those that are necessary

        val toBeUnescaped =
            Regex("""\\(.)""")      // blacklist, only escape those that are necessary

        fun escape(value: String): String {
            return value.replace(toBeEscaped) {
                when(it.value) {
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
                    when(it) {
                        "n" -> "\n"
                        "r" -> "\r"
                        "t" -> "\t"
                        else  -> it
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
            }.map {
                "${it.first}: ${it.second}"
            }.joinToString("\n")
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
                            is IntPref     -> pref.key to pref.value
                            is BooleanPref -> pref.key to pref.value
                            is StringPref  -> pref.key to pref.value
                            is ListPref    -> pref.key to pref.value
                            is EnumPref    -> pref.key to pref.value
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

            prefs.forEach { key, value ->
                when (value) {
                    is String  -> setPrefString(key, value)
                    is Int     -> setPrefInt(key, value)
                    is Boolean -> setPrefFlag(key, value)
                }
            }
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

//typealias PrefValue = @Polymorphic Any
typealias PrefValue = @Contextual Any

class BooleanPref(
    key: String,
    private: Boolean = false,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    defaultValue: Boolean,
    enableIf: (() -> Boolean)? = null,
) : Pref(key, private, summary, titleId, summaryId, defaultValue, icon, iconTint, enableIf) {
    var value
        get() = prefFlag(key, defaultValue as Boolean, private)
        set(value) = setPrefFlag(key, value, private)

    override fun toString(): String = value.toString()
}

class IntPref(
    key: String,
    private: Boolean = false,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: List<Int>,
    defaultValue: Int,
    enableIf: (() -> Boolean)? = null,
) : Pref(key, private, summary, titleId, summaryId, defaultValue, icon, iconTint, enableIf) {
    var value
        get() = prefInt(key, defaultValue as Int, private)
        set(value) = setPrefInt(key, value, private)

    override fun toString(): String = value.toString()
}

open class StringPref(
    key: String,
    private: Boolean = false,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    defaultValue: String,
    enableIf: (() -> Boolean)? = null,
) : Pref(key, private, summary, titleId, summaryId, defaultValue, icon, iconTint, enableIf) {
    open var value
        get() = prefString(key, defaultValue as String, private)
        set(value) = setPrefString(key, value, private)

    override fun toString(): String = value.toString()
}

class PasswordPref(
    key: String,
    private: Boolean = true,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    defaultValue: String,
    enableIf: (() -> Boolean)? = null,
) : StringPref(key, private, summary, titleId, summaryId, icon, iconTint, defaultValue, enableIf) {
    override var value
        get() = prefString(key, defaultValue as String, private)
        set(value) = setPrefString(key, value, private)

    override fun toString(): String = value.toString()
}

class ListPref(
    key: String,
    private: Boolean = false,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: Map<String, String>,
    defaultValue: String,
    enableIf: (() -> Boolean)? = null,
) : Pref(key, private, summary, titleId, summaryId, defaultValue, icon, iconTint, enableIf) {
    var value
        get() = prefString(key, defaultValue as String, private)
        set(value) = setPrefString(key, value, private)

    override fun toString(): String = value.toString()
}

class EnumPref(
    key: String,
    private: Boolean = false,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: Map<Int, Int>,
    defaultValue: Int,
    enableIf: (() -> Boolean)? = null,
) : Pref(key, private, summary, titleId, summaryId, defaultValue, icon, iconTint, enableIf) {
    var value
        get() = prefInt(key, defaultValue as Int, private)
        set(value) = setPrefInt(key, value, private)

    override fun toString(): String = value.toString()
}

class LinkPref(
    key: String,
    private: Boolean = false,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enableIf: (() -> Boolean)? = null,
) : Pref(key, private, summary, titleId, summaryId, null, icon, iconTint, enableIf)

class LaunchPref(
    key: String,
    private: Boolean = false,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    enableIf: (() -> Boolean)? = null,
    val onClick: (() -> Unit) = {},
) : Pref(key, private, summary, titleId, summaryId, null, icon, iconTint, enableIf)


