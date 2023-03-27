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

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.EXTRA_SCHEDULE_ID
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.mainFilterChipItems
import com.machiav3lli.backup.schedSpecialFilterChipItems
import com.machiav3lli.backup.scheduleBackupModeChipItems
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.Play
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.CheckChip
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.MorphableTextField
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.ui.compose.recycler.MultiSelectableChipGroup
import com.machiav3lli.backup.ui.compose.recycler.SelectableChipGroup
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.item.ChipItem
import com.machiav3lli.backup.utils.cancelAlarm
import com.machiav3lli.backup.utils.specialBackupsEnabled
import com.machiav3lli.backup.utils.startSchedule
import com.machiav3lli.backup.utils.timeLeft
import com.machiav3lli.backup.viewmodels.ScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.time.LocalTime

class ScheduleSheet() : BaseSheet() {
    private lateinit var viewModel: ScheduleViewModel

    constructor(scheduleId: Long) : this() {
        arguments = Bundle().apply {
            putLong(EXTRA_SCHEDULE_ID, scheduleId)
        }
    }

    private val scheduleId: Long
        get() = requireArguments().getLong(EXTRA_SCHEDULE_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val viewModelFactory = ScheduleViewModel.Factory(
            scheduleId,
            OABX.db.scheduleDao,
            requireActivity().application
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[ScheduleViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { SchedulePage() }
        }
    }

    private fun refresh(
        schedule: Schedule,
        rescheduleBoolean: Boolean,
    ) = viewModel.updateSchedule(schedule, rescheduleBoolean)

    private fun showTimePickerDialog(schedule: Schedule) {
        TimePickerDialog(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialTimePicker,
            { _, hourOfDay, minute ->
                refresh(
                    schedule.copy(timeHour = hourOfDay, timeMinute = minute),
                    rescheduleBoolean = true
                )
            },
            schedule.timeHour,
            schedule.timeMinute,
            true
        )
            .show()
    }

    private fun showIntervalSetterDialog(schedule: Schedule) {
        IntervalInDaysDialog(schedule.interval.toString()) { newInterval: Int ->
            refresh(
                schedule.copy(interval = newInterval),
                rescheduleBoolean = true
            )
        }.show(requireActivity().supportFragmentManager, "INTERVALDAYS_DIALOG")
    }

    private fun showCustomListDialog(schedule: Schedule) {
        val selectedPackages = schedule.customList.toList()
        PackagesListDialogFragment(
            selectedPackages, schedule.filter, false
        ) { newList: Set<String> ->
            refresh(
                schedule.copy(customList = newList),
                rescheduleBoolean = false,
            )
        }.show(requireActivity().supportFragmentManager, "CUSTOMLIST_DIALOG")
    }

    private fun showBlockListDialog(schedule: Schedule) {
        val blocklistedPackages = schedule.blockList.toList()
        PackagesListDialogFragment(
            blocklistedPackages, schedule.filter, true
        ) { newList: Set<String> ->
            refresh(
                schedule.copy(blockList = newList),
                rescheduleBoolean = false,
            )
        }.show(requireActivity().supportFragmentManager, "BLOCKLIST_DIALOG")
    }

    @Composable
    fun SchedulePage() {
        val schedule by viewModel.schedule.collectAsState()
        val customList by viewModel.customList.collectAsState(emptySet())
        val blockList by viewModel.blockList.collectAsState(emptySet())
        val nestedScrollConnection = rememberNestedScrollInteropConnection()
        val (absTime, relTime) = timeLeft(schedule, CoroutineScope(Dispatchers.Default))
            .collectAsState().value

        //traceDebug { "*** recompose schedule <<- ${schedule}" }

        AppTheme {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                bottomBar = {
                    Column(
                        modifier = Modifier.padding(
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 12.dp,
                        )
                    ) {
                        Divider(thickness = 2.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            if (schedule.enabled) {
                                Text(text = "🕒 $absTime    ⏳ $relTime") // TODO replace by resource icons
                            } else {
                                Text(text = "🕒 $absTime") // TODO replace by resource icons
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CheckChip(
                                checked = schedule.enabled,
                                textId = R.string.sched_checkbox,
                                checkedTextId = R.string.enabled,
                                onCheckedChange = { checked ->
                                    refresh(
                                        schedule.copy(enabled = checked),
                                        true,
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            ElevatedActionButton(
                                text = stringResource(id = R.string.delete),
                                icon = Phosphor.TrashSimple,
                                positive = false,
                                fullWidth = false
                            ) {
                                viewModel.deleteSchedule()
                                cancelAlarm(requireContext(), scheduleId)
                                dismissAllowingStateLoss()
                            }
                        }
                        ElevatedActionButton(
                            text = stringResource(id = R.string.sched_activateButton),
                            icon = Phosphor.Play,
                            fullWidth = true,
                            onClick = { startSchedule(schedule) }
                        )
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .nestedScroll(nestedScrollConnection)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TitleText(R.string.sched_name)
                            MorphableTextField(
                                modifier = Modifier.weight(1f),
                                text = schedule.name,
                                textAlignment = TextAlign.Center,
                                onCancel = {
                                },
                                onSave = {
                                    refresh(
                                        schedule.copy(name = it),
                                        rescheduleBoolean = false,
                                    )
                                }
                            )
                            RoundButton(
                                icon = Phosphor.CaretDown,
                                description = stringResource(id = R.string.dismiss),
                                onClick = { dismissAllowingStateLoss() }
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .clickable { showTimePickerDialog(schedule) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TitleText(R.string.sched_hourOfDay)
                            Text(
                                text = LocalTime.of(schedule.timeHour, schedule.timeMinute)
                                    .toString(),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                    item {
                        // TODO replace with slider Or custom Compose Dialog?
                        Row(
                            modifier = Modifier
                                .clickable { showIntervalSetterDialog(schedule) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TitleText(R.string.sched_interval)
                            Text(
                                text = schedule.interval.toString(),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ActionChip(
                                icon = Phosphor.CheckCircle,
                                text = stringResource(id = R.string.customListTitle),
                                positive = customList.isNotEmpty(),
                                fullWidth = true,
                                modifier = Modifier.weight(1f),
                                onClick = { showCustomListDialog(schedule) }
                            )
                            ActionChip(
                                icon = Phosphor.Prohibit,
                                text = stringResource(id = R.string.sched_blocklist),
                                positive = blockList.isNotEmpty(),
                                fullWidth = true,
                                modifier = Modifier.weight(1f),
                                onClick = { showBlockListDialog(schedule) }
                            )
                        }
                    }

                    item {
                        //traceDebug { "*** recompose filter ${schedule.filter}" }
                        TitleText(R.string.filter_options)
                        MultiSelectableChipGroup(
                            list = if (specialBackupsEnabled)
                                mainFilterChipItems
                            else
                                mainFilterChipItems.minus(ChipItem.Special),
                            //selectedFlags = schedule.filter
                            selectedFlags = schedule.filter
                        ) { flags, flag ->
                            //traceDebug { "*** onClick filter ${schedule.filter} xor ${flag} -> $flags (${schedule.filter xor flag})" }
                            refresh(
                                schedule.copy(filter = flags),
                                false,
                            )
                        }
                    }
                    item {
                        //traceDebug { "*** recompose mode ${schedule.mode}" }
                        TitleText(R.string.sched_mode)
                        MultiSelectableChipGroup(
                            list = scheduleBackupModeChipItems,
                            selectedFlags = schedule.mode
                        ) { flags, flag ->
                            traceDebug { "*** onClick mode ${schedule.mode} xor ${flag} -> $flags (${schedule.mode xor flag})" }
                            refresh(
                                schedule.copy(mode = flags),
                                false,
                            )
                        }
                    }
                    item {
                        //traceDebug { "*** recompose specialFilter ${schedule.specialFilter}" }
                        TitleText(R.string.other_filters_options)
                        SelectableChipGroup(
                            list = schedSpecialFilterChipItems,
                            selectedFlag = schedule.specialFilter
                        ) { flag ->
                            traceDebug { "*** onClick specialFilter -> $flag" }
                            refresh(
                                schedule.copy(specialFilter = flag),
                                false,
                            )
                        }
                    }
                }
            }
        }
    }
}
