/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.DynamicColors
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_LANGUAGES_SYSTEM
import com.machiav3lli.backup.R
import com.machiav3lli.backup.THEME_BLACK
import com.machiav3lli.backup.THEME_DARK
import com.machiav3lli.backup.THEME_DYNAMIC
import com.machiav3lli.backup.THEME_DYNAMIC_BLACK
import com.machiav3lli.backup.THEME_DYNAMIC_DARK
import com.machiav3lli.backup.THEME_DYNAMIC_LIGHT
import com.machiav3lli.backup.THEME_LIGHT
import com.machiav3lli.backup.THEME_SYSTEM_BLACK
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.theme.ApricotOrange
import com.machiav3lli.backup.ui.compose.theme.ArcticCyan
import com.machiav3lli.backup.ui.compose.theme.AzureBlue
import com.machiav3lli.backup.ui.compose.theme.BoldGreen
import com.machiav3lli.backup.ui.compose.theme.CalmIndigo
import com.machiav3lli.backup.ui.compose.theme.ChartreuseLime
import com.machiav3lli.backup.ui.compose.theme.FinePurple
import com.machiav3lli.backup.ui.compose.theme.FlamingoPink
import com.machiav3lli.backup.ui.compose.theme.LavaOrange
import com.machiav3lli.backup.ui.compose.theme.Limette
import com.machiav3lli.backup.ui.compose.theme.Mint
import com.machiav3lli.backup.ui.compose.theme.OceanTeal
import com.machiav3lli.backup.ui.compose.theme.PumpkinPerano
import com.machiav3lli.backup.ui.compose.theme.RedComet
import com.machiav3lli.backup.ui.compose.theme.Slate
import com.machiav3lli.backup.ui.compose.theme.ThunderYellow
import com.machiav3lli.backup.ui.compose.theme.TigerAmber
import com.machiav3lli.backup.ui.compose.theme.Turquoise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Locale

fun Context.setCustomTheme() {
    AppCompatDelegate.setDefaultNightMode(getThemeStyleX(styleTheme))
    if (!(isDynamicTheme && DynamicColors.isDynamicColorAvailable())) {
        setTheme(R.style.AppTheme)
        theme.applyAccentStyle()
        theme.applySecondaryStyle()
    } // TODO allow fine control on using custom accent/secondary colors?
    if (isBlackTheme && isNightMode())
        theme.applyStyle(R.style.Black, true)
}

val isBlackTheme: Boolean
    get() = when (styleTheme) {
        THEME_BLACK,
        THEME_SYSTEM_BLACK,
        THEME_DYNAMIC_BLACK,
             -> true

        else -> false
    }

val isDynamicTheme: Boolean
    get() = when (styleTheme) {
        THEME_DYNAMIC,
        THEME_DYNAMIC_LIGHT,
        THEME_DYNAMIC_DARK,
        THEME_DYNAMIC_BLACK,
             -> true

        else -> false
    }


private var sysLocale: LocaleList? = null
private var sysLocaleJVM: Locale? = null

fun Context.setLanguage(lang: String = ""): Configuration {

    // only works on start of activity

    var setLocaleCode = if (lang.isEmpty()) language else lang  // parameter for tests
    traceDebug { "Locale.set: ${setLocaleCode}" }

    val config = resources.configuration

    //TODO hg42 for now, cache the initial value, but this doesn't change with system settings
    //TODO hg42 look for another method to retrieve the system setting
    //TODO hg42 maybe asking the unwrapped app or activity context, but how to get this?
    if (sysLocale == null) {
        sysLocale = config.locales
        sysLocaleJVM = Locale.getDefault()
        traceDebug { "Locale.sys: $sysLocale $sysLocaleJVM" }
    }

    var wantSystem = false
    if (setLocaleCode == PREFS_LANGUAGES_SYSTEM) {
        wantSystem = true
        setLocaleCode = sysLocale.toString()
    }

    if (wantSystem) {
        config.setLocales(sysLocale)
        sysLocaleJVM?.let { Locale.setDefault(it) }
    } else {
        val newLocale = getLocaleOfCode(setLocaleCode)
        traceDebug { "Locale.new: ${newLocale}" }
        config.setLocale(newLocale)
        Locale.setDefault(newLocale)
    }
    traceDebug { "Locale.===: ${config.locales} ${Locale.getDefault()}" }

    return config
}

//TODO hg42 at least one of the following Save buttons was reported as not doing anything

fun Activity.showActionResult(result: ActionResult, saveMethod: DialogInterface.OnClickListener) =
    runOnUiThread {
        val builder = AlertDialog.Builder(this)
            .setPositiveButton(R.string.dialogOK, null)
        if (!result.succeeded) {
            builder.setNegativeButton(R.string.dialogSave, saveMethod)      //TODO hg42 either this
            builder.setTitle(R.string.errorDialogTitle)
                .setMessage(LogsHandler.handleErrorMessages(this, result.message))
            builder.show()
        }
    }

fun Activity.showError(message: String?) = runOnUiThread {
    runOnUiThread {
        AlertDialog.Builder(this)
            .setTitle(R.string.errorDialogTitle)
            .setMessage(message)
            .setPositiveButton(R.string.dialogOK, null)
            .setNegativeButton(R.string.dialogSave) { _: DialogInterface?, _: Int ->
                LogsHandler.logErrors(message ?: "ERROR")           //TODO hg42 or that
            }
            .show()
    }
}

fun Activity.showFatalUiWarning(message: String) = showWarning(
    getString(R.string.app_name),
    message
) { _: DialogInterface?, _: Int -> finishAffinity() }

fun Activity.showWarning(
    title: String,
    message: String,
    callback: DialogInterface.OnClickListener?,
) = runOnUiThread {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setNeutralButton(R.string.dialogOK, callback)
        .setCancelable(false)
        .show()
}

fun getThemeStyleX(theme: Int) = when (theme) {
    THEME_LIGHT,
    THEME_DYNAMIC_LIGHT,
    -> AppCompatDelegate.MODE_NIGHT_NO

    THEME_DARK,
    THEME_DYNAMIC_DARK,
    THEME_DYNAMIC_BLACK,
    -> AppCompatDelegate.MODE_NIGHT_YES

    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}

fun Context.isNightMode() =
    resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

fun Resources.Theme.applyAccentStyle() = applyStyle(
    when (stylePrimary) {
        1    -> R.style.Accent1
        2    -> R.style.Accent2
        3    -> R.style.Accent3
        4    -> R.style.Accent4
        5    -> R.style.Accent5
        6    -> R.style.Accent6
        7    -> R.style.Accent7
        8    -> R.style.Accent8
        else -> R.style.Accent0
    }, true
)

val primaryColor
    get() = when (stylePrimary) {
        1    -> FinePurple
        2    -> CalmIndigo
        3    -> Turquoise
        4    -> BoldGreen
        5    -> ChartreuseLime
        6    -> ThunderYellow
        7    -> ApricotOrange
        8    -> PumpkinPerano
        else -> RedComet
    }

fun Resources.Theme.applySecondaryStyle() = applyStyle(
    when (styleSecondary) {
        1    -> R.style.Secondary1
        2    -> R.style.Secondary2
        3    -> R.style.Secondary3
        4    -> R.style.Secondary4
        5    -> R.style.Secondary5
        6    -> R.style.Secondary6
        7    -> R.style.Secondary7
        8    -> R.style.Secondary8
        else -> R.style.Secondary0
    }, true
)

val secondaryColor
    get() = when (styleSecondary) {
        1    -> OceanTeal
        2    -> Limette
        3    -> TigerAmber
        4    -> LavaOrange
        5    -> FlamingoPink
        6    -> Slate
        7    -> AzureBlue
        8    -> Mint
        else -> ArcticCyan
    }

fun Context.restartApp() {
    Timber.w(
        "restarting application"
    )
    startActivity(
        Intent.makeRestartActivityTask(
            ComponentName(this, MainActivityX::class.java)
        )
    )
}

var recreateActivitiesJob: Job? = null

fun Context.recreateActivities() {
    runBlocking {
        recreateActivitiesJob?.cancel()
    }
    recreateActivitiesJob = MainScope().launch {
        //Timber.w("recreating activities...")
        delay(500)
        Timber.w(
            "recreating activities ${
                OABX.activities.map {
                    "${it.javaClass.simpleName}@${Integer.toHexString(it.hashCode())}"
                }.joinToString(" ")
            }"
        )
        OABX.activities
            .forEach {
                runCatching {
                    it.recreate()
                }
            }
    }
}

fun <T> LazyListScope.gridItems(
    items: List<T>,
    columns: Int,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    itemContent: @Composable BoxScope.(T) -> Unit,
) {
    val itemsCount = items.count()
    val rows = when {
        itemsCount >= 1 -> 1 + (itemsCount - 1) / columns
        else            -> 0
    }
    items(rows, key = { it.hashCode() }) { rowIndex ->
        Row(
            horizontalArrangement = horizontalArrangement,
            modifier = modifier
        ) {
            (0 until columns).forEach { columnIndex ->
                val itemIndex = columns * rowIndex + columnIndex
                if (itemIndex < itemsCount) {
                    Box(
                        modifier = Modifier.weight(1f),
                        propagateMinConstraints = true
                    ) {
                        itemContent(items[itemIndex])
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

fun Color.brighter(rate: Float): Color {
    val hslVal = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hslVal)
    hslVal[2] += rate * (1 - hslVal[2])
    hslVal[2] = hslVal[2].coerceIn(0f..1f)
    return Color(ColorUtils.HSLToColor(hslVal))
}

fun Color.darker(rate: Float): Color {
    val hslVal = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hslVal)
    hslVal[2] -= rate * hslVal[2]
    hslVal[2] = hslVal[2].coerceIn(0f..1f)
    return Color(ColorUtils.HSLToColor(hslVal))
}

// TODO make easy callable from different contexts
fun SnackbarHostState.show(
    coroutineScope: CoroutineScope,
    message: String,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    coroutineScope.launch {
        showSnackbar(message = message, actionLabel = actionText, withDismissAction = true).apply {
            if (this == SnackbarResult.ActionPerformed) onAction()
        }
    }
}
