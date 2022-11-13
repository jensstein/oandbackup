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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.machiav3lli.backup.dialogs.BatchDialogFragment
import com.machiav3lli.backup.fragments.AppSheet
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.item.ActionButton
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.ExpandingFadingVisibility
import com.machiav3lli.backup.ui.compose.recycler.HomePackageRecycler
import com.machiav3lli.backup.ui.compose.recycler.UpdatedPackageRecycler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomePage() {
    // TODO include tags in search
    val main = OABX.main!!
    var appSheet: AppSheet? = null

    val filteredList by main.viewModel.filteredList.collectAsState(emptyList())
    val updatedPackages by main.viewModel.updatedPackages.collectAsState(emptyList())
    var updaterExpanded by remember { mutableStateOf(false) }

    val queriedList = filteredList

    val batchConfirmListener = object : BatchDialogFragment.ConfirmListener {
        override fun onConfirmed(selectedPackages: List<String?>, selectedModes: List<Int>) {
            main.startBatchAction(true, selectedPackages, selectedModes) {
                it.removeObserver(this)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            AnimatedVisibility(
                modifier = Modifier.padding(start = 28.dp),
                visible = updatedPackages?.isNotEmpty() ?: true
            ) {
                ExpandingFadingVisibility(
                    expanded = updaterExpanded,
                    expandedView = {
                        Column(
                            modifier = Modifier
                                .shadow(
                                    elevation = 6.dp,
                                    MaterialTheme.shapes.large
                                )
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.shapes.large
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ActionButton(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(id = R.string.backup_all_updated),
                                ) {
                                    val selectedList = updatedPackages.orEmpty()
                                        .map { it.packageInfo }
                                        .toCollection(ArrayList())
                                    val selectedListModes = updatedPackages.orEmpty()
                                        .mapNotNull {
                                            it.latestBackup?.let { bp ->
                                                when {
                                                    bp.hasApk && bp.hasAppData -> ALT_MODE_BOTH
                                                    bp.hasApk -> ALT_MODE_APK
                                                    bp.hasAppData -> ALT_MODE_DATA
                                                    else -> ALT_MODE_UNSET
                                                }
                                            }
                                        }
                                        .toCollection(ArrayList())
                                    if (selectedList.isNotEmpty()) {
                                        BatchDialogFragment(
                                            true,
                                            selectedList,
                                            selectedListModes,
                                            batchConfirmListener
                                        )
                                            .show(
                                                main.supportFragmentManager,
                                                "DialogFragment"
                                            )
                                    }
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
                                    if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                                    appSheet = AppSheet(item.packageName)
                                    appSheet?.showNow(
                                        main.supportFragmentManager,
                                        "Package ${item.packageName}"
                                    )
                                }
                            )
                        }
                    },
                    collapsedView = {
                        val text = pluralStringResource(
                            id = R.plurals.updated_apps,
                            count = updatedPackages.orEmpty().size,
                            updatedPackages.orEmpty().size
                        )
                        ExtendedFloatingActionButton(
                            text = { Text(text = text) },
                            icon = {
                                Icon(
                                    imageVector = if (updaterExpanded) Phosphor.CaretDown else Phosphor.CircleWavyWarning,
                                    contentDescription = text
                                )
                            },
                            onClick = { updaterExpanded = !updaterExpanded }
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        HomePackageRecycler(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            productsList = queriedList ?: listOf(),
            onClick = { item ->
                if (appSheet != null) appSheet?.dismissAllowingStateLoss()
                appSheet = AppSheet(item.packageName)
                appSheet?.showNow(
                    main.supportFragmentManager,
                    "Package ${item.packageName}"
                )
            }
        )
    }
}
