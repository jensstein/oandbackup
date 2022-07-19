package com.machiav3lli.backup.ui.item

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

open class Pref(
    val key: String,
    @StringRes val titleId: Int,
    @StringRes val summaryId: Int,
    @DrawableRes val iconId: Int = -1,
    val iconTint: Color?
) {

    class BooleanPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        @DrawableRes iconId: Int = -1,
        iconTint: Color? = null,
        val defaultValue: Boolean
    ) : Pref(key, titleId, summaryId, iconId, iconTint)

    class IntPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        @DrawableRes iconId: Int = -1,
        iconTint: Color? = null,
        val entries: List<Int>,
        val defaultValue: Int
    ) : Pref(key, titleId, summaryId, iconId, iconTint)

    class StringPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        @DrawableRes iconId: Int = -1,
        iconTint: Color? = null,
        val defaultValue: String
    ) : Pref(key, titleId, summaryId, iconId, iconTint)

    class ListPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        @DrawableRes iconId: Int = -1,
        iconTint: Color? = null,
        val entries: Map<String, String>,
        val defaultValue: String
    ) : Pref(key, titleId, summaryId, iconId, iconTint)

    class EnumPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        @DrawableRes iconId: Int = -1,
        iconTint: Color? = null,
        val entries: Map<Int, Int>,
        val defaultValue: Int
    ) : Pref(key, titleId, summaryId, iconId, iconTint)

    class LinkPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        @DrawableRes iconId: Int = -1,
        iconTint: Color? = null
    ) : Pref(key, titleId, summaryId, iconId, iconTint)
}