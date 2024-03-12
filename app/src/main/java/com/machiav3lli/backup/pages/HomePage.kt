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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.ALT_MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.BatchActionDialogUI
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_languages
import com.machiav3lli.backup.preferences.pref_menuButtonAlwaysVisible
import com.machiav3lli.backup.sheets.AppSheet
import com.machiav3lli.backup.sheets.Sheet
import com.machiav3lli.backup.traceCompose
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.icons.phosphor.List
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.ExpandingFadingVisibility
import com.machiav3lli.backup.ui.compose.item.IconCache
import com.machiav3lli.backup.ui.compose.item.MainPackageContextMenu
import com.machiav3lli.backup.ui.compose.item.cachedAsyncImagePainter
import com.machiav3lli.backup.ui.compose.recycler.HomePackageRecycler
import com.machiav3lli.backup.ui.compose.recycler.UpdatedPackageRecycler
import com.machiav3lli.backup.utils.TraceUtils.beginNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.endNanoTimer
import com.machiav3lli.backup.utils.altModeToMode
import com.machiav3lli.backup.viewmodels.AppSheetViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage() {
    // TODO include tags in search
    val mActivity = OABX.main!!
    val scope = rememberCoroutineScope()
    val viewModel = mActivity.viewModel

    val filteredList by viewModel.filteredList.collectAsState(emptyList())
    val updatedPackages by viewModel.updatedPackages.collectAsState(emptyList())
    val updaterVisible = updatedPackages.isNotEmpty()  // recompose is already triggered above
    var updaterExpanded by remember { mutableStateOf(false) }
    val selection = viewModel.selection
    val nSelected = selection.filter { it.value }.keys.size
    var menuPackage by remember { mutableStateOf<Package?>(null) }
    val menuExpanded = viewModel.menuExpanded
    val menuButtonAlwaysVisible = pref_menuButtonAlwaysVisible.value
    val openBatchDialog = remember { mutableStateOf(false) }
    val appSheetState = rememberModalBottomSheetState(true)
    val appSheetPN: MutableState<String?> = rememberSaveable { mutableStateOf(null) }
    val appSheetPackage: MutableState<Package?> = remember(appSheetPN.value) {
        mutableStateOf(
            (filteredList + updatedPackages)
                .find { it.packageName == appSheetPN.value }
        )
    }
    val appSheetVM = remember(appSheetPackage.value) {
        if (appSheetPackage.value != null) AppSheetViewModel(
            appSheetPackage.value,
            OABX.db,
            ShellCommands(),
        ) else null
    }

    traceCompose {
        "HomePage filtered=${
            filteredList.size
        } updated=${
            updatedPackages.size
        }->${
            if (updaterVisible) "visible" else "hidden"
        } menu=${
            menuExpanded.value
        } always=${menuButtonAlwaysVisible} language=${pref_languages.value}"
    }

    // prefetch icons
    if (filteredList.size > IconCache.size) {    // includes empty cache and empty filteredList
        beginNanoTimer("prefetchIcons")
        filteredList.forEach { pkg ->
            cachedAsyncImagePainter(model = pkg.iconData)
        }
        endNanoTimer("prefetchIcons")
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (nSelected > 0 || updaterVisible || menuButtonAlwaysVisible) {
                Row(
                    modifier = Modifier.padding(start = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (updaterVisible) {
                        ExpandingFadingVisibility(
                            expanded = updaterExpanded,
                            expandedView = {
                                Column {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ActionButton(
                                            modifier = Modifier.weight(1f),
                                            text = stringResource(id = R.string.backup_all_updated),
                                        ) {
                                            openBatchDialog.value = true
                                        }
                                        ElevatedActionButton(
                                            text = "",
                                            icon = Phosphor.CaretDown,
                                            withText = false
                                        ) {
                                            updaterExpanded = !updaterExpanded
                                        }
                                    }
                                    UpdatedPackageRecycler(
                                        productsList = updatedPackages,
                                        onClick = { item ->
                                            appSheetPN.value = item.packageName
                                        }
                                    )
                                }
                            },
                            collapsedView = {
                                val text = pluralStringResource(
                                    id = R.plurals.updated_apps,
                                    count = updatedPackages.size,
                                    updatedPackages.size
                                )
                                ExtendedFloatingActionButton(
                                    text = { Text(text = text) },
                                    icon = {
                                        Icon(
                                            imageVector = if (updaterExpanded) Phosphor.CaretDown else Phosphor.CircleWavyWarning,
                                            contentDescription = text
                                        )
                                    },
                                    containerColor = Color.Transparent,
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                                    onClick = { updaterExpanded = !updaterExpanded }
                                )
                            }
                        )
                    }
                    if (!(updaterVisible && updaterExpanded) &&
                        (nSelected > 0 || menuButtonAlwaysVisible)
                    ) {
                        ExtendedFloatingActionButton(
                            text = { Text(text = nSelected.toString()) },
                            icon = {
                                Icon(
                                    imageVector = Phosphor.List,
                                    contentDescription = stringResource(id = R.string.context_menu)
                                )
                            },
                            onClick = {
                                menuExpanded.value = true
                            },
                        )
                    }
                }
            }
        }
    ) {
        HomePackageRecycler(
            modifier = Modifier
                .blockBorder()
                .fillMaxSize(),
            productsList = filteredList,
            selection = selection,
            onLongClick = { item ->
                if (selection[item.packageName] == true) {
                    menuPackage = item
                    menuExpanded.value = true
                } else {
                    selection[item.packageName] = selection[item.packageName] != true
                }
            },
            onClick = { item ->
                if (filteredList.none { selection[it.packageName] == true }) {
                    appSheetPN.value = item.packageName
                } else {
                    selection[item.packageName] = selection[item.packageName] != true
                }
            },
        )

        if (menuExpanded.value) {
            Box(
                modifier = Modifier     // necessary to move the menu on the whole screen
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
            ) {
                MainPackageContextMenu(
                    expanded = menuExpanded,
                    packageItem = menuPackage,
                    productsList = filteredList,
                    selection = selection,
                    openSheet = { item ->
                        appSheetPN.value = item.packageName
                    }
                )
            }
        }
        if (appSheetPN.value != null) {
            val dismiss = {
                scope.launch { appSheetState.hide() }
                appSheetPN.value = null
            }
            Sheet(
                sheetState = appSheetState,
                onDismissRequest = dismiss
            ) {
                AppSheet(
                    viewModel = appSheetVM!!,
                    packageName = appSheetPN.value ?: "",
                    onDismiss = dismiss,
                )
            }
        }
        if (openBatchDialog.value) BaseDialog(openDialogCustom = openBatchDialog) {
            val selectedList = updatedPackages
                .map { it.packageInfo }
                .toCollection(ArrayList())
            val selectedApk = mutableMapOf<String, Int>()
            val selectedData = mutableMapOf<String, Int>()
            val selectedListModes = updatedPackages
                .map {
                    altModeToMode(
                        it.latestBackup?.let { bp ->
                            when {
                                bp.hasApk && bp.hasAppData -> {
                                    selectedApk[bp.packageName] = 1
                                    selectedData[bp.packageName] = 1
                                    ALT_MODE_BOTH
                                }

                                bp.hasApk                  -> {
                                    selectedApk[bp.packageName] = 1
                                    ALT_MODE_APK
                                }

                                bp.hasAppData              -> {
                                    selectedData[bp.packageName] = 1
                                    ALT_MODE_DATA
                                }

                                else                       -> ALT_MODE_UNSET
                            }
                        } ?: ALT_MODE_BOTH  // no backup -> try all
                        , true
                    )
                }
                .toCollection(ArrayList())

            BatchActionDialogUI(
                backupBoolean = true,
                selectedPackageInfos = selectedList,
                selectedApk = selectedApk,
                selectedData = selectedData,
                openDialogCustom = openBatchDialog,
            ) {
                mActivity.startBatchAction(
                    true,
                    selectedPackageNames = selectedList.map { it.packageName },
                    selectedModes = selectedListModes,
                )
            }
        }
    }
}
