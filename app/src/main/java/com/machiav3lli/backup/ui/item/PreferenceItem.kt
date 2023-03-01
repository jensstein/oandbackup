package com.machiav3lli.backup.ui.item

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.tracePrefs
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import com.machiav3lli.backup.utils.getPrivateSharedPrefs

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
    var group: String = ""
) {
    companion object {

        val preferences: MutableMap<String, MutableList<Pref>> = mutableMapOf()

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
            } catch(e: Throwable) {
                default
            }

        fun setPrefFlag(name: String, value: Boolean, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = $value" }
            getPrefs(private).edit().putBoolean(name, value).apply().also { onPrefChange() }
        }

        fun prefString(name: String, default: String, private: Boolean = false) =
            try {
                getPrefs(private).getString(name, default) ?: default
            } catch(e: Throwable) {
                default
            }

        fun setPrefString(name: String, value: String, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = '$value'" }
            getPrefs(private).edit().putString(name, value).apply().also { onPrefChange() }
        }

        fun prefInt(name: String, default: Int, private: Boolean = false) =
            try {
                getPrefs(private).getInt(name, default)
            } catch(e: Throwable) {
                default
            }

        fun setPrefInt(name: String, value: Int, private: Boolean = false) {
            if (!private) tracePrefs { "set pref $name = $value" }
            getPrefs(private).edit().putInt(name, value).apply().also { onPrefChange() }
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

        preferences.getOrPut(group) { mutableListOf() }.add(this)
    }

    override fun toString(): String = ""
}

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


