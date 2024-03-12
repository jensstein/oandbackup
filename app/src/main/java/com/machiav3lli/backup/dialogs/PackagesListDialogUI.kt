/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2023  Antonios Hazim
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

import android.content.pm.PackageInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.MAIN_FILTER_SPECIAL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.getPackageInfoList
import com.machiav3lli.backup.utils.specialBackupsEnabled

@Composable
fun PackagesListDialogUI(
    selectedPackageNames: Set<String>,
    filter: Int,
    title: String,
    openDialogCustom: MutableState<Boolean>,
    onPackagesListChanged: (newList: Set<String>) -> Unit,
) {
    val context = LocalContext.current
    val pm = context.packageManager

    var packageInfos = context.getPackageInfoList(filter)
    packageInfos = packageInfos.sortedWith { pi1: PackageInfo, pi2: PackageInfo ->
        val b1 = selectedPackageNames.contains(pi1.packageName)
        val b2 = selectedPackageNames.contains(pi2.packageName)
        if (b1 != b2)
            if (b1) -1 else 1
        else {
            val l1 = pi1.applicationInfo.loadLabel(pm).toString()
            val l2 = pi2.applicationInfo.loadLabel(pm).toString()
            l1.compareTo(l2, ignoreCase = true)
        }
    }
    val packagePairs = mutableListOf<Pair<String, String>>()

    if (specialBackupsEnabled && filter and MAIN_FILTER_SPECIAL == MAIN_FILTER_SPECIAL) {
        var specialInfos = SpecialInfo.getSpecialInfos(OABX.NB)
        specialInfos = specialInfos.sortedWith { si1, si2 ->
            val b1 = selectedPackageNames.contains(si1.packageName)
            val b2 = selectedPackageNames.contains(si2.packageName)
            if (b1 != b2)
                if (b1) -1 else 1
            else {
                val l1 = si1.packageLabel
                val l2 = si2.packageLabel
                l1.compareTo(l2, ignoreCase = true)
            }
        }
        packagePairs.addAll(
            specialInfos.map { specialInfo ->
                Pair(specialInfo.packageName, specialInfo.packageLabel)
            }
        )
    }
    packagePairs.addAll(
        packageInfos.map { packageInfo ->
            Pair(packageInfo.packageName, packageInfo.applicationInfo.loadLabel(pm).toString())
        }
    )

    MultiSelectionDialogUI(
        titleText = title,
        entryMap = packagePairs.toMap(),
        selectedItems = selectedPackageNames.toList(),
        openDialogCustom = openDialogCustom,
    ) {
        onPackagesListChanged(it.toSet())
    }
}

@Composable
fun BlockListDialogUI(
    schedule: Schedule,
    openDialogCustom: MutableState<Boolean>,
    onPackagesListChanged: (newList: Set<String>) -> Unit,
) {
    PackagesListDialogUI(
        selectedPackageNames = schedule.blockList,
        filter = schedule.filter,
        title = stringResource(id = R.string.sched_blocklist),
        openDialogCustom = openDialogCustom,
        onPackagesListChanged = onPackagesListChanged,
    )
}

@Composable
fun GlobalBlockListDialogUI(
    currentBlocklist: Set<String>,
    openDialogCustom: MutableState<Boolean>,
    onPackagesListChanged: (newList: Set<String>) -> Unit,
) {
    PackagesListDialogUI(
        selectedPackageNames = currentBlocklist,
        filter = MAIN_FILTER_DEFAULT,
        title = stringResource(id = R.string.sched_blocklist),
        openDialogCustom = openDialogCustom,
        onPackagesListChanged = onPackagesListChanged,
    )
}

@Composable
fun CustomListDialogUI(
    schedule: Schedule,
    openDialogCustom: MutableState<Boolean>,
    onPackagesListChanged: (newList: Set<String>) -> Unit,
) {
    PackagesListDialogUI(
        selectedPackageNames = schedule.customList,
        filter = schedule.filter,
        title = stringResource(id = R.string.customListTitle),
        openDialogCustom = openDialogCustom,
        onPackagesListChanged = onPackagesListChanged,
    )
}
