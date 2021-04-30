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
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.ActionResult

fun setDayNightTheme(theme: String?) {
    when (theme) {
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

fun Activity.showActionResult(result: ActionResult, saveMethod: DialogInterface.OnClickListener?) = runOnUiThread {
    val builder = AlertDialog.Builder(this)
            .setPositiveButton(R.string.dialogOK, null)
    if (saveMethod != null) {
        builder.setNegativeButton(R.string.dialogSave, saveMethod)
    }
    if (!result.succeeded) {
        builder.setTitle(R.string.errorDialogTitle)
                .setMessage(result.message)
        builder.show()
    }
}


fun Activity.showError(message: String?) = runOnUiThread {
    AlertDialog.Builder(this)
            .setTitle(R.string.errorDialogTitle)
            .setMessage(message)
            .setPositiveButton(R.string.dialogOK, null).show()
}

fun Activity.showFatalUiWarning(message: String) =
        showWarning(this.javaClass.simpleName, message) { _: DialogInterface?, _: Int -> finishAffinity() }

fun Activity.showWarning(title: String, message: String, callback: DialogInterface.OnClickListener?) =
        runOnUiThread {
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


