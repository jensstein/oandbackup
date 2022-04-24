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
package com.machiav3lli.backup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.SortFilterModel
import com.machiav3lli.backup.mainBackupModeChipItems
import com.machiav3lli.backup.mainFilterChipItems
import com.machiav3lli.backup.mainSpecialFilterChipItems
import com.machiav3lli.backup.sortChipItems
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.DoubleVerticalText
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.SwitchChip
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.ui.compose.recycler.MultiSelectableChipGroup
import com.machiav3lli.backup.ui.compose.recycler.SelectableChipGroup
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.item.ChipItem
import com.machiav3lli.backup.utils.sortFilterModel
import com.machiav3lli.backup.utils.specialBackupsEnabled

class SortFilterSheet(
    private var mSortFilterModel: SortFilterModel = SortFilterModel(),
    private val stats: Triple<Int, Int, Int>
) : BaseSheet() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mSortFilterModel = requireContext().sortFilterModel

        return ComposeView(requireContext()).apply {
            setContent { SortFilterPage() }
        }
    }

    // TODO Omit using layout fully in next iteration
    @OptIn(
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
        ExperimentalLayoutApi::class
    )
    @Composable
    fun SortFilterPage() {
        AppTheme(
            darkTheme = isSystemInDarkTheme()
        ) {
            mSortFilterModel.let {
                Column(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                        Column(
                            modifier = Modifier
                                .background(color = Color.Transparent)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedCard(
                                modifier = Modifier.padding(top = 4.dp),
                                //colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                //),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DoubleVerticalText(
                                        upperText = stats.first.toString(),
                                        bottomText = stringResource(id = R.string.stats_apps),
                                        modifier = Modifier.weight(1f)
                                    )
                                    DoubleVerticalText(
                                        upperText = stats.second.toString(),
                                        bottomText = stringResource(id = R.string.stats_backups),
                                        modifier = Modifier.weight(1f)
                                    )
                                    DoubleVerticalText(
                                        upperText = stats.third.toString(),
                                        bottomText = stringResource(id = R.string.stats_updated),
                                        modifier = Modifier.weight(1f)
                                    )
                                    RoundButton(icon = painterResource(id = R.drawable.ic_arrow_down)) {
                                        dismissAllowingStateLoss()
                                    }
                                }
                            }
                            TitleText(R.string.sort_options)
                            SelectableChipGroup(
                                list = sortChipItems,
                                selectedFlag = it.sort
                            ) { flag ->
                                it.sort = flag
                            }
                            SwitchChip(
                                firstTextId = R.string.sortAsc,
                                firstIconId = R.drawable.ic_arrow_up,
                                secondTextId = R.string.sortDesc,
                                secondIconId = R.drawable.ic_arrow_down,
                                firstSelected = it.sortAsc,
                                onCheckedChange = { checked -> it.sortAsc = checked }
                            )
                            TitleText(R.string.filter_options)
                            MultiSelectableChipGroup(
                                list = if (requireContext().specialBackupsEnabled) mainFilterChipItems
                                else mainFilterChipItems.minus(ChipItem.Special),
                                selectedFlags = it.mainFilter
                            ) { flag ->
                                it.mainFilter = it.mainFilter xor flag
                            }
                            TitleText(R.string.backup_filters)
                            MultiSelectableChipGroup(
                                list = mainBackupModeChipItems,
                                selectedFlags = it.backupFilter
                            ) { flag ->
                                it.backupFilter = it.backupFilter xor flag
                            }
                            TitleText(R.string.other_filters_options)
                            SelectableChipGroup(
                                list = mainSpecialFilterChipItems,
                                selectedFlag = it.specialFilter
                            ) { flag ->
                                it.specialFilter = flag
                            }
                        }
                    }
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        Row(
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.surface)
                                .fillMaxWidth()
                                .padding(8.dp)
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ActionChip(
                                text = stringResource(id = R.string.resetFilter),
                                icon = painterResource(id = R.drawable.ic_reset),
                                modifier = Modifier.weight(1f),
                                fullWidth = true,
                                positive = false,
                                onClick = {
                                    requireContext().sortFilterModel = SortFilterModel()
                                    requireMainActivity().refreshView()
                                    dismissAllowingStateLoss()
                                }
                            )
                            ActionChip(
                                text = stringResource(id = R.string.applyFilter),
                                icon = painterResource(id = R.drawable.ic_apply),
                                modifier = Modifier.weight(1f),
                                fullWidth = true,
                                positive = true,
                                onClick = {
                                    requireContext().sortFilterModel = mSortFilterModel
                                    requireMainActivity().refreshView()
                                    dismissAllowingStateLoss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}