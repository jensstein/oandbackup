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
package com.machiav3lli.backup.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.BatchActionDialogUI
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_singularBackupRestore
import com.machiav3lli.backup.sheets.BatchPrefsSheet
import com.machiav3lli.backup.sheets.Sheet
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.DiamondsFour
import com.machiav3lli.backup.ui.compose.icons.phosphor.HardDrives
import com.machiav3lli.backup.ui.compose.icons.phosphor.Nut
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.StateChip
import com.machiav3lli.backup.ui.compose.recycler.BatchPackageRecycler
import com.machiav3lli.backup.ui.compose.theme.ColorAPK
import com.machiav3lli.backup.ui.compose.theme.ColorData
import com.machiav3lli.backup.utils.altModeToMode
import com.machiav3lli.backup.viewmodels.BatchViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchPage(viewModel: BatchViewModel, backupBoolean: Boolean) {
    val main = OABX.main!!
    val scope = rememberCoroutineScope()
    val filteredList by main.viewModel.filteredList.collectAsState(emptyList())
    val showBatchSheet = remember { mutableStateOf(false) }
    val backupBatchSheet = remember { mutableStateOf(false) }
    val batchSheetState = rememberModalBottomSheetState(true)
    val openDialog = remember { mutableStateOf(false) }

    val filterPredicate = { item: Package ->
        if (backupBoolean) item.isInstalled else item.hasBackups
    }
    val workList = filteredList.filter(filterPredicate)

    var allApkChecked by remember(workList, viewModel.apkBackupCheckedList) {
        mutableStateOf(
            viewModel.apkBackupCheckedList.size == workList
                .filter { !it.isSpecial && (backupBoolean || it.latestBackup?.hasApk == true) }
                .size
        )
    }
    var allDataChecked by remember(workList, viewModel.dataBackupCheckedList) {
        mutableStateOf(
            viewModel.dataBackupCheckedList.size == workList
                .filter { backupBoolean || it.latestBackup?.hasData == true }
                .size
        )
    }

    val selection = remember { mutableStateMapOf<Package, Boolean>() }
    filteredList.forEach {
        selection.putIfAbsent(it, false)
    }

    Scaffold(containerColor = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            BatchPackageRecycler(
                modifier = Modifier
                    .blockBorder()
                    .weight(1f, true)
                    .fillMaxSize(),
                productsList = workList,
                restore = !backupBoolean,
                apkBackupCheckedList = viewModel.apkBackupCheckedList,
                dataBackupCheckedList = viewModel.dataBackupCheckedList,
                onBackupApkClick = { packageName: String, b: Boolean, i: Int ->
                    if (b) viewModel.apkBackupCheckedList[packageName] = i
                    else if (viewModel.apkBackupCheckedList[packageName] == i)
                        viewModel.apkBackupCheckedList[packageName] = -1
                    allApkChecked =
                        viewModel.apkBackupCheckedList.filterValues { it == 0 }.size ==
                                workList.filter { ai ->
                                    backupBoolean || ai.latestBackup?.hasApk ?: false
                                }.size
                },
                onBackupDataClick = { packageName: String, b: Boolean, i: Int ->
                    if (b) viewModel.dataBackupCheckedList[packageName] = i
                    else if (viewModel.dataBackupCheckedList[packageName] == i)
                        viewModel.dataBackupCheckedList[packageName] = -1
                    allDataChecked =
                        viewModel.dataBackupCheckedList.filterValues { it == 0 }.size ==
                                workList.filter { ai ->
                                    backupBoolean || ai.latestBackup?.hasData ?: false
                                }.size
                },
            ) { item, checkApk, checkData ->
                when (checkApk) {
                    true -> viewModel.apkBackupCheckedList[item.packageName] = 0
                    else -> viewModel.apkBackupCheckedList[item.packageName] = -1
                }
                when (checkData) {
                    true -> viewModel.dataBackupCheckedList[item.packageName] = 0
                    else -> viewModel.dataBackupCheckedList[item.packageName] = -1
                }
                allApkChecked = viewModel.apkBackupCheckedList.size == workList
                    .filter { !it.isSpecial && (backupBoolean || it.latestBackup?.hasApk == true) }
                    .size
                allDataChecked = viewModel.dataBackupCheckedList.size == workList
                    .filter { backupBoolean || it.latestBackup?.hasData == true }
                    .size
            }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                StateChip(
                    icon = Phosphor.DiamondsFour,
                    text = stringResource(id = R.string.all_apk),
                    checked = allApkChecked,
                    color = ColorAPK
                ) {
                    val checkBoolean = !allApkChecked
                    allApkChecked = checkBoolean
                    when {
                        checkBoolean -> workList
                            .filter { (backupBoolean && !it.isSpecial) || it.latestBackup?.hasApk == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.apkBackupCheckedList[it] = 0
                            }

                        else         -> workList
                            .filter { backupBoolean || it.latestBackup?.hasApk == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.apkBackupCheckedList[it] = -1
                            }
                    }
                }
                StateChip(
                    icon = Phosphor.HardDrives,
                    text = stringResource(id = R.string.all_data),
                    checked = allDataChecked,
                    color = ColorData
                ) {
                    val checkBoolean = !allDataChecked
                    allDataChecked = checkBoolean
                    when {
                        checkBoolean -> workList
                            .filter { backupBoolean || it.latestBackup?.hasData == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.dataBackupCheckedList[it] = 0
                            }

                        else         -> workList
                            .filter { backupBoolean || it.latestBackup?.hasData == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.dataBackupCheckedList[it] = -1
                            }
                    }
                }
                RoundButton(icon = Phosphor.Nut) {
                    showBatchSheet.value = true
                }
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = if (backupBoolean) R.string.backup else R.string.restore),
                    positive = true
                ) {
                    if (viewModel.apkBackupCheckedList.filterValues { it != -1 }.isNotEmpty()
                        || viewModel.dataBackupCheckedList.filterValues { it != -1 }.isNotEmpty()
                    ) openDialog.value = true
                }
            }
        }


        if (showBatchSheet.value) {
            val dismiss = {
                scope.launch { batchSheetState.hide() }
                showBatchSheet.value = false
            }
            Sheet(
                sheetState = batchSheetState,
                onDismissRequest = dismiss
            ) {
                BatchPrefsSheet(
                    backupBoolean = backupBatchSheet.value
                )
            }
        }
        if (openDialog.value) BaseDialog(openDialogCustom = openDialog) {
            val selectedApk = viewModel.apkBackupCheckedList.filterValues { it != -1 }
            val selectedData = viewModel.dataBackupCheckedList.filterValues { it != -1 }
            val selectedPackageNames = selectedApk.keys.plus(selectedData.keys).distinct()

            BatchActionDialogUI(
                backupBoolean = backupBoolean,
                selectedPackageInfos = filteredList
                    .filter { it.packageName in selectedPackageNames }
                    .map(Package::packageInfo),
                selectedApk = selectedApk,
                selectedData = selectedData,
                openDialogCustom = openDialog,
            ) {
                if (pref_singularBackupRestore.value && !backupBoolean) main.startBatchRestoreAction(
                    selectedPackageNames = selectedPackageNames,
                    selectedApk = selectedApk,
                    selectedData = selectedData,
                )
                else main.startBatchAction(
                    backupBoolean,
                    selectedPackageNames = selectedPackageNames,
                    selectedModes = selectedPackageNames.map { pn ->
                        altModeToMode(
                            when {
                                selectedData[pn] == 0 && selectedApk[pn] == 0 -> ALT_MODE_BOTH
                                selectedData[pn] == 0                         -> ALT_MODE_DATA
                                else                                          -> ALT_MODE_APK
                            }, backupBoolean
                        )
                    }
                )
            }
        }
    }
}