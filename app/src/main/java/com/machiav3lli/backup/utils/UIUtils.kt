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
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.ActionResult

fun Context.setCustomTheme() {
    AppCompatDelegate.setDefaultNightMode(getThemeStyle(themeStyle))
    setTheme(R.style.AppTheme)
    theme.applyStyle(getAccentStyle(accentStyle), true)
    theme.applyStyle(getSecondaryStyle(secondaryStyle), true)
}

fun Activity.showActionResult(result: ActionResult, saveMethod: DialogInterface.OnClickListener) =
    runOnUiThread {
        val builder = AlertDialog.Builder(this)
            .setPositiveButton(R.string.dialogOK, null)
        if (!result.succeeded) {
            builder.setNegativeButton(R.string.dialogSave, saveMethod)
        }
        if (!result.succeeded) {
            builder.setTitle(R.string.errorDialogTitle)
                .setMessage(LogsHandler.handleErrorMessages(this, result.message))
            builder.show()
        }
    }


fun Activity.showError(message: String?) = runOnUiThread {
    AlertDialog.Builder(this)
        .setTitle(R.string.errorDialogTitle)
        .setMessage(message)
        .setPositiveButton(R.string.dialogOK, null).show()
}

fun Activity.showFatalUiWarning(message: String) = showWarning(
    this.javaClass.simpleName,
    message
) { _: DialogInterface?, _: Int -> finishAffinity() }

fun Activity.showWarning(
    title: String,
    message: String,
    callback: DialogInterface.OnClickListener?
) = runOnUiThread {
    AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setNeutralButton(R.string.dialogOK, callback)
        .setCancelable(false)
        .show()
}

fun Activity.showToast(message: String?) = runOnUiThread {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

val Context.colorAccent: Int
    get() {
        val tA = obtainStyledAttributes(intArrayOf(R.attr.colorAccent))
        val color = tA.getColor(0, 0)
        tA.recycle()
        return color
    }

val Context.colorSecondary: Int
    get() {
        val tA = obtainStyledAttributes(intArrayOf(R.attr.colorSecondary))
        val color = tA.getColor(0, 0)
        tA.recycle()
        return color
    }

fun getThemeStyle(theme: String) = when (theme) {
    "light" -> AppCompatDelegate.MODE_NIGHT_NO
    "dark" -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}

fun getAccentStyle(accent: String) = when (accent.last().digitToInt()) {
    1 -> R.style.Accent1
    2 -> R.style.Accent2
    3 -> R.style.Accent3
    4 -> R.style.Accent4
    5 -> R.style.Accent5
    6 -> R.style.Accent6
    7 -> R.style.Accent7
    else -> R.style.Accent0
}

fun getSecondaryStyle(secondary: String) = when (secondary.last().digitToInt()) {
    1 -> R.style.Secondary1
    2 -> R.style.Secondary2
    3 -> R.style.Secondary3
    4 -> R.style.Secondary4
    5 -> R.style.Secondary5
    6 -> R.style.Secondary6
    7 -> R.style.Secondary7
    else -> R.style.Secondary0
}
