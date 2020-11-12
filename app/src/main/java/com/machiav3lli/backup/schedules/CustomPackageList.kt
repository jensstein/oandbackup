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
package com.machiav3lli.backup.schedules

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.util.ArraySet
import androidx.appcompat.app.AlertDialog
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.customListAddress
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.schedules.db.Schedule

object CustomPackageList {
    fun showList(activity: Activity, number: Int, mode: Schedule.Mode?) {
        val selectedList = getScheduleCustomList(activity, number)
        var packageInfoList = BackendController.getPackageInfoList(activity, mode)
        packageInfoList = packageInfoList.sortedWith { pi1: PackageInfo, pi2: PackageInfo ->
            val pm = activity.application.applicationContext.packageManager
            val b1 = selectedList!!.contains(pi1.packageName)
            val b2 = selectedList.contains(pi2.packageName)
            if (b1 != b2)
                if (b1) -1 else 1
            else {
                val l1 = pi1.applicationInfo.loadLabel(pm).toString()
                val l2 = pi2.applicationInfo.loadLabel(pm).toString()
                l1.compareTo(l2, ignoreCase = true)
            }
        }
        val labels = ArrayList<String>()
        val packageNames = ArrayList<String>()
        val checkedBooleanArray = BooleanArray(packageInfoList.size)
        val selected = ArrayList<Int>()
        for ((i, packageInfo) in packageInfoList.withIndex()) {
            labels.add(packageInfo.applicationInfo.loadLabel(activity.packageManager).toString())
            packageNames.add(packageInfo.packageName)
            if (selectedList!!.contains(packageInfo.packageName)) {
                checkedBooleanArray[i] = true
                selected.add(i)
            }
        }
        AlertDialog.Builder(activity)
                .setTitle(R.string.customListTitle)
                .setMultiChoiceItems(labels.toTypedArray<CharSequence>(), checkedBooleanArray
                ) { _: DialogInterface?, id: Int, isChecked: Boolean ->
                    if (isChecked) {
                        selected.add(id)
                    } else {
                        selected.remove(id) // cast as Integer to distinguish between remove(Object) and remove(index)
                    }
                }
                .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int -> saveSelcted(activity, number, packageNames.toTypedArray(), selected) }
                .setNegativeButton(R.string.dialogCancel) { dialog: DialogInterface?, _: Int -> dialog!!.cancel() }
                .show()
    }

    private fun saveSelcted(context: Context, index: Int, items: Array<CharSequence>, selected: ArrayList<Int>) {
        val selectedPackages = selected.map { pos: Int? -> items[pos!!].toString() }.toSet()
        setScheduleCustomList(context, index, selectedPackages)
    }

    fun getScheduleCustomList(context: Context, index: Int): Set<String>? {
        return context.getSharedPreferences(Constants.PREFS_SCHEDULES, Context.MODE_PRIVATE).getStringSet(customListAddress(index), ArraySet())
    }

    fun setScheduleCustomList(context: Context, index: Int, packagesList: Set<String>?) {
        context.getSharedPreferences(Constants.PREFS_SCHEDULES, Context.MODE_PRIVATE).edit().putStringSet(customListAddress(index), packagesList).apply()
    }
}