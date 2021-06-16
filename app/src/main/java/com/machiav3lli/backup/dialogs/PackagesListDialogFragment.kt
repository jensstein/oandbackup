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
package com.machiav3lli.backup.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.getPackageInfoList

class PackagesListDialogFragment(
    private val selectedPackages: List<String>,
    val filter: Int, private val isBlocklist: Boolean,
    private val onPackagesListChanged: (newList: Set<String>) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstance: Bundle?): Dialog {
        val pm = requireContext().packageManager

        var packageInfoList = requireContext().getPackageInfoList(filter)
        packageInfoList = packageInfoList.sortedWith { pi1: PackageInfo, pi2: PackageInfo ->
            val b1 = selectedPackages.contains(pi1.packageName)
            val b2 = selectedPackages.contains(pi2.packageName)
            if (b1 != b2)
                if (b1) -1 else 1
            else {
                val l1 = pi1.applicationInfo.loadLabel(pm).toString()
                val l2 = pi2.applicationInfo.loadLabel(pm).toString()
                l1.compareTo(l2, ignoreCase = true)
            }
        }
        val labels = mutableListOf<String>()
        val packagesNames = mutableListOf<String>()
        val checkedIndexes = BooleanArray(packageInfoList.size)
        val selections = mutableListOf<Int>()
        packageInfoList.forEachIndexed { i, packageInfo ->
            labels.add(packageInfo.applicationInfo.loadLabel(pm).toString())
            packagesNames.add(packageInfo.packageName)
            if (selectedPackages.contains(packageInfo.packageName)) {
                checkedIndexes[i] = true
                selections.add(i)
            }
        }
        return AlertDialog.Builder(requireActivity())
            .setTitle(if (isBlocklist) R.string.sched_blocklist else R.string.customListTitle)
            .setMultiChoiceItems(
                labels.toTypedArray<CharSequence>(),
                checkedIndexes
            ) { _: DialogInterface?, index: Int, isChecked: Boolean ->
                if (isChecked) selections.add(index) else selections.remove(index) // cast as Integer to distinguish between remove(Object) and remove(index)
            }
            .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                saveSelected(packagesNames, selections)
            }
            .setNegativeButton(R.string.dialogCancel) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
            .create()
    }

    private fun saveSelected(packagesNames: List<String>, selections: List<Int>) {
        val selectedPackages = selections
            .map { packagesNames[it] }
            .toSet()
        onPackagesListChanged(selectedPackages)
    }
}