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

import android.animation.Animator
import android.app.Activity
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.ActionResult

object UIUtils {
    private val TAG = classTag(".UIUtils")

    @JvmStatic
    fun setDayNightTheme(theme: String?) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    @JvmStatic
    fun showActionResult(activity: Activity, result: ActionResult, saveMethod: DialogInterface.OnClickListener?) {
        activity.runOnUiThread {
            val builder = AlertDialog.Builder(activity)
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
    }

    @JvmStatic
    fun showError(activity: Activity, message: String?) {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.errorDialogTitle)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialogOK, null).show()
        }
    }

    @JvmStatic
    fun showWarning(activity: Activity, title: String?, message: String?, callback: DialogInterface.OnClickListener?) {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton(R.string.dialogOK, callback)
                    .setCancelable(false)
                    .show()
        }
    }

    @JvmStatic
    fun setVisibility(view: View, visibility: Int, withAnimation: Boolean) {
        view.animate().alpha(if (visibility == View.VISIBLE) 1.0f else 0.0f)
                .setDuration(if (withAnimation) 600 else 1.toLong())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        if (visibility == View.VISIBLE && view.visibility == View.GONE) view.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = visibility
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        // not relevant
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        // not relevant
                    }
                })
    }
}