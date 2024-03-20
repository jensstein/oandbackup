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
package com.machiav3lli.backup.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.BACKUP_FILTER_DEFAULT
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SPECIAL_FILTER_ALL
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.enabledFilterChipItems
import com.machiav3lli.backup.installedFilterChipItems
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.latestFilterChipItems
import com.machiav3lli.backup.launchableFilterChipItems
import com.machiav3lli.backup.mainBackupModeChipItems
import com.machiav3lli.backup.mainFilterChipItems
import com.machiav3lli.backup.sortChipItems
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowUUpLeft
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.Check
import com.machiav3lli.backup.ui.compose.icons.phosphor.SortAscending
import com.machiav3lli.backup.ui.compose.icons.phosphor.SortDescending
import com.machiav3lli.backup.ui.compose.item.DoubleVerticalText
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.ExpandableBlock
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.SwitchChip
import com.machiav3lli.backup.ui.compose.recycler.MultiSelectableChipGroup
import com.machiav3lli.backup.ui.compose.recycler.SelectableChipGroup
import com.machiav3lli.backup.ui.item.ChipItem
import com.machiav3lli.backup.updatedFilterChipItems
import com.machiav3lli.backup.utils.applyFilter
import com.machiav3lli.backup.utils.getStats
import com.machiav3lli.backup.utils.sortFilterModel
import com.machiav3lli.backup.utils.specialBackupsEnabled

@Composable
fun SortFilterSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val mActivity = context as MainActivityX
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val packageList by mActivity.viewModel.notBlockedList.collectAsState()
    var model by rememberSaveable { mutableStateOf(sortFilterModel) }
    fun currentStats() = getStats(
        packageList.applyFilter(
            model,
            OABX.context,
        )
    )  //TODO hg42 use central function for all the filtering

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = {
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                headlineContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DoubleVerticalText(
                            upperText = currentStats().first.toString(),
                            bottomText = stringResource(id = R.string.stats_apps),
                            modifier = Modifier.weight(1f)
                        )
                        DoubleVerticalText(
                            upperText = currentStats().second.toString(),
                            bottomText = stringResource(id = R.string.stats_backups),
                            modifier = Modifier.weight(1f)
                        )
                        DoubleVerticalText(
                            upperText = currentStats().third.toString(),
                            bottomText = stringResource(id = R.string.stats_updated),
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                trailingContent = {
                    RoundButton(icon = Phosphor.CaretDown) {
                        onDismiss()
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ElevatedActionButton(
                    text = stringResource(id = R.string.resetFilter),
                    icon = Phosphor.ArrowUUpLeft,
                    modifier = Modifier.weight(1f),
                    fullWidth = true,
                    positive = false,
                    onClick = {
                        sortFilterModel = SortFilterModel()
                        onDismiss()
                    }
                )
                ElevatedActionButton(
                    text = stringResource(id = R.string.applyFilter),
                    icon = Phosphor.Check,
                    modifier = Modifier.weight(1f),
                    fullWidth = true,
                    positive = true,
                    onClick = {
                        sortFilterModel = model
                        onDismiss()
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .blockBorder()
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.sorting_order),
                    preExpanded = true,
                ) {
                    SelectableChipGroup(
                        list = sortChipItems,
                        selectedFlag = model.sort
                    ) { flag ->
                        model = model.copy(sort = flag)
                    }
                    SwitchChip(
                        firstTextId = R.string.sortAsc,
                        firstIcon = Phosphor.SortAscending,
                        secondTextId = R.string.sortDesc,
                        secondIcon = Phosphor.SortDescending,
                        firstSelected = model.sortAsc,
                        onCheckedChange = { checked ->
                            model = model.copy(sortAsc = checked)
                        }
                    )
                }
            }
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.filters_app),
                    preExpanded = model.mainFilter != MAIN_FILTER_DEFAULT,
                ) {
                    MultiSelectableChipGroup(
                        list = if (specialBackupsEnabled) mainFilterChipItems
                        else mainFilterChipItems.minus(ChipItem.Special),
                        selectedFlags = model.mainFilter
                    ) { flags, _ ->
                        model = model.copy(mainFilter = flags)
                    }
                }
            }
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.filters_backup),
                    preExpanded = model.backupFilter != BACKUP_FILTER_DEFAULT,
                ) {
                    MultiSelectableChipGroup(
                        list = mainBackupModeChipItems,
                        selectedFlags = model.backupFilter
                    ) { flags, _ ->
                        model = model.copy(backupFilter = flags)
                    }
                }
            }
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.filters_installed),
                    preExpanded = model.installedFilter != SPECIAL_FILTER_ALL,
                ) {
                    SelectableChipGroup(
                        list = installedFilterChipItems,
                        selectedFlag = model.installedFilter
                    ) { flag ->
                        model = model.copy(installedFilter = flag)
                    }
                }
            }
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.filters_launchable),
                    preExpanded = model.launchableFilter != SPECIAL_FILTER_ALL,
                ) {
                    SelectableChipGroup(
                        list = launchableFilterChipItems,
                        selectedFlag = model.launchableFilter
                    ) { flag ->
                        model = model.copy(launchableFilter = flag)
                    }
                }
            }
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.filters_updated),
                    preExpanded = model.updatedFilter != SPECIAL_FILTER_ALL,
                ) {
                    SelectableChipGroup(
                        list = updatedFilterChipItems,
                        selectedFlag = model.updatedFilter
                    ) { flag ->
                        model = model.copy(updatedFilter = flag)
                    }
                }
            }
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.filters_latest),
                    preExpanded = model.latestFilter != SPECIAL_FILTER_ALL,
                ) {
                    SelectableChipGroup(
                        list = latestFilterChipItems,
                        selectedFlag = model.latestFilter
                    ) { flag ->
                        model = model.copy(latestFilter = flag)
                    }
                }
            }
            item {
                ExpandableBlock(
                    heading = stringResource(id = R.string.filters_enabled),
                    preExpanded = model.enabledFilter != SPECIAL_FILTER_ALL,
                ) {
                    SelectableChipGroup(
                        list = enabledFilterChipItems,
                        selectedFlag = model.enabledFilter
                    ) { flag ->
                        model = model.copy(enabledFilter = flag)
                    }
                }
            }
        }
    }
}
