package com.machiav3lli.backup.ui.item

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.OABX
import timber.log.Timber

open class Pref(
    var key: String,
    val summary: String? = null,
    @StringRes val titleId: Int,
    @StringRes val summaryId: Int,
    val icon: ImageVector? = null,
    val iconTint: Color?,
    var group: String = ""
) {
    companion object {
        val preferences: MutableMap<String, MutableList<Pref>> = mutableMapOf()
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
        Timber.d("add pref $group.$key")
        preferences.getOrPut(group) { mutableListOf() }.add(this)
    }
}

class BooleanPref(
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val defaultValue: Boolean
) : Pref(key, summary, titleId, summaryId, icon, iconTint) {
    var value
        get() = OABX.prefFlag(key, defaultValue)
        set(value) = OABX.setPrefFlag(key, value)
}

class IntPref(
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: List<Int>,
    val defaultValue: Int
) : Pref(key, summary, titleId, summaryId, icon, iconTint) {
    var value
        get() = OABX.prefInt(key, defaultValue)
        set(value) = OABX.setPrefInt(key, value)
}

open class StringPref(
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val defaultValue: String
) : Pref(key, summary, titleId, summaryId, icon, iconTint) {
    open var value
        get() = OABX.prefString(key, defaultValue)
        set(value) = OABX.setPrefString(key, value)
}

class PasswordPref(
    key: String,
    summary: String? = null,
    titleId: Int = -1,
    summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    defaultValue: String
) : StringPref(key, summary, titleId, summaryId, icon, iconTint, defaultValue) {
    override var value
        get() = OABX.prefPrivateString(key, defaultValue)
        set(value) = OABX.setPrefPrivateString(key, value)
}

class ListPref(
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: Map<String, String>,
    val defaultValue: String
) : Pref(key, summary, titleId, summaryId, icon, iconTint) {
    var value
        get() = OABX.prefString(key, defaultValue)
        set(value) = OABX.setPrefString(key, value)
}

class EnumPref(
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val entries: Map<Int, Int>,
    val defaultValue: Int
) : Pref(key, summary, titleId, summaryId, icon, iconTint) {
    var value
        get() = OABX.prefInt(key, defaultValue)
        set(value) = OABX.setPrefInt(key, value)
}

class LinkPref(
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null
) : Pref(key, summary, titleId, summaryId, icon, iconTint)

class LaunchPref(
    key: String,
    summary: String? = null,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    val onClick: (() -> Unit) = {}
) : Pref(key, summary, titleId, summaryId, icon, iconTint)
