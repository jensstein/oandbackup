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
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.dao.ScheduleDao
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.dialogs.ScheduleNameDialog
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.mainFilterChipItems
import com.machiav3lli.backup.schedSpecialFilterChipItems
import com.machiav3lli.backup.scheduleBackupModeChipItems
import com.machiav3lli.backup.services.ScheduleService
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.item.CheckChip
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.ui.compose.recycler.MultiSelectableChipGroup
import com.machiav3lli.backup.ui.compose.recycler.SelectableChipGroup
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.item.ChipItem
import com.machiav3lli.backup.utils.calculateTimeToRun
import com.machiav3lli.backup.utils.cancelAlarm
import com.machiav3lli.backup.utils.filterToString
import com.machiav3lli.backup.utils.modeToModes
import com.machiav3lli.backup.utils.modesToString
import com.machiav3lli.backup.utils.specialBackupsEnabled
import com.machiav3lli.backup.utils.specialFilterToString
import com.machiav3lli.backup.viewmodels.ScheduleViewModel
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class ScheduleSheet() : BaseSheet() {
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var database: ODatabase

    constructor(scheduleId: Long) : this() {
        arguments = Bundle().apply {
            putLong(EXTRA_SCHEDULE_ID, scheduleId)
        }
    }

    private val scheduleId: Long
        get() = requireArguments().getLong(EXTRA_SCHEDULE_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        database = ODatabase.getInstance(requireContext())
        val viewModelFactory = ScheduleViewModel.Factory(
            scheduleId,
            database.scheduleDao,
            requireActivity().application
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[ScheduleViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { SchedulePage() }
        }
    }

    private fun getTimeLeft(schedule: Schedule): String {
        var text = ""
        if (schedule.enabled) {
            val now = System.currentTimeMillis()
            val timeDiff = abs(calculateTimeToRun(schedule, now) - now)
            val days = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
            if (days != 0) {
                text += requireContext().resources
                    .getQuantityString(R.plurals.days_left, days, days)
            }
            val hours = TimeUnit.MILLISECONDS.toHours(timeDiff).toInt() % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt() % 60
            text += LocalTime.of(hours, minutes).toString()
        }
        return text
    }

    private fun refresh(
        schedule: Schedule,
        rescheduleBoolean: Boolean
    ) = viewModel.updateSchedule(schedule, rescheduleBoolean)

    private fun startSchedule(schedule: Schedule) {

        val message = StringBuilder()
        message.append(
            "\n${getString(R.string.sched_mode)} ${
                modesToString(
                    requireContext(),
                    modeToModes(schedule.mode)
                )
            }"
        )
        message.append(
            "\n${getString(R.string.backup_filters)} ${
                filterToString(
                    requireContext(),
                    schedule.filter
                )
            }"
        )
        message.append(
            "\n${getString(R.string.other_filters_options)} ${
                specialFilterToString(
                    requireContext(),
                    schedule.specialFilter
                )
            }"
        )
        // TODO list the CL packages
        message.append(
            "\n${getString(R.string.customListTitle)}: ${
                if (schedule.customList.isNotEmpty()) getString(
                    R.string.dialogYes
                ) else getString(R.string.dialogNo)
            }"
        )
        // TODO list the BL packages
        message.append(
            "\n${getString(R.string.sched_blocklist)}: ${
                if (schedule.blockList.isNotEmpty()) getString(
                    R.string.dialogYes
                ) else getString(R.string.dialogNo)
            }"
        )
        AlertDialog.Builder(requireActivity())
            .setTitle("${schedule.name}: ${getString(R.string.sched_activateButton)}?")
            .setMessage(message)
            .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                if (schedule.mode != MODE_UNSET)
                    StartSchedule(requireContext(), database.scheduleDao, scheduleId).execute()
            }
            .setNegativeButton(R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
            .show()
    }

    internal class StartSchedule(
        val context: Context,
        val scheduleDao: ScheduleDao,
        private val scheduleId: Long
    ) :
        ShellCommands.Command {

        override fun execute() {
            Thread {
                val now = System.currentTimeMillis()
                val serviceIntent = Intent(context, ScheduleService::class.java)
                scheduleDao.getSchedule(scheduleId)?.let { schedule ->
                    serviceIntent.putExtra("scheduleId", scheduleId)
                    serviceIntent.putExtra("name", schedule.getBatchName(now))
                    context.startService(serviceIntent)
                }
            }.start()
        }
    }

    private fun showNameEditorDialog(schedule: Schedule) {
        ScheduleNameDialog(schedule.name) {
            schedule.name = it
            refresh(schedule, rescheduleBoolean = false)
        }.show(requireActivity().supportFragmentManager, "SCHEDULENAME_DIALOG")
    }

    private fun showTimePickerDialog(schedule: Schedule) {
        TimePickerDialog(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialTimePicker,
            { _, hourOfDay, minute ->
                schedule.timeHour = hourOfDay
                schedule.timeMinute = minute
                refresh(schedule, rescheduleBoolean = true)
            },
            schedule.timeHour,
            schedule.timeMinute,
            true
        )
            .show()
    }

    private fun showIntervalSetterDialog(schedule: Schedule) {
        IntervalInDaysDialog(schedule.interval.toString()) { newInterval: Int ->
            schedule.interval = newInterval
            refresh(schedule, rescheduleBoolean = true)
        }.show(requireActivity().supportFragmentManager, "INTERVALDAYS_DIALOG")
    }

    private fun showCustomListDialog(schedule: Schedule) {
        val selectedPackages = schedule.customList.toList()
        PackagesListDialogFragment(
            selectedPackages, schedule.filter, false
        ) { newList: Set<String> ->
            schedule.customList = newList
            refresh(schedule, rescheduleBoolean = false)
        }.show(requireActivity().supportFragmentManager, "CUSTOMLIST_DIALOG")
    }

    private fun showBlockListDialog(schedule: Schedule) {
        val blocklistedPackages = schedule.blockList.toList()
        PackagesListDialogFragment(
            blocklistedPackages, schedule.filter, true
        ) { newList: Set<String> ->
            schedule.blockList = newList
            refresh(schedule, rescheduleBoolean = false)
        }.show(requireActivity().supportFragmentManager, "BLOCKLIST_DIALOG")
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun SchedulePage() {
        val schedule by viewModel.schedule.collectAsState()
        val customList by viewModel.customList.collectAsState(emptySet())
        val blockList by viewModel.blockList.collectAsState(emptySet())
        val (checked, check) = mutableStateOf(schedule.enabled)
        val nestedScrollConnection = rememberNestedScrollInteropConnection()

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
                            AnimatedVisibility(visible = checked) {
                                Text(
                                    text = "${stringResource(id = R.string.sched_timeLeft)} "
                                        .plus(getTimeLeft(schedule))
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CheckChip(
                                checked = schedule.enabled,
                                textId = R.string.sched_checkbox,
                                checkedTextId = R.string.enabled,
                                onCheckedChange = { checked ->
                                    check(checked)
                                    schedule.enabled = checked
                                    refresh(schedule, true)
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
                            icon = Phosphor.ArchiveTray,
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
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TitleText(R.string.sched_name)
                            Text(
                                text = schedule.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showNameEditorDialog(schedule) },
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TitleText(R.string.sched_hourOfDay)
                            Text(
                                text = LocalTime.of(schedule.timeHour, schedule.timeMinute)
                                    .toString(),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { showTimePickerDialog(schedule) },
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
                            TitleText(R.string.sched_interval)
                            Text(
                                text = schedule.interval.toString(),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { showIntervalSetterDialog(schedule) },
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
                            ElevatedActionButton(
                                icon = Phosphor.CheckCircle,
                                text = stringResource(id = R.string.customListTitle),
                                positive = customList.isNotEmpty(),
                                fullWidth = true,
                                modifier = Modifier.weight(1f),
                                onClick = { showCustomListDialog(schedule) }
                            )
                            ElevatedActionButton(
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
                        TitleText(R.string.filter_options)
                        MultiSelectableChipGroup(
                            list = if (requireContext().specialBackupsEnabled) mainFilterChipItems
                            else mainFilterChipItems.minus(ChipItem.Special),
                            selectedFlags = schedule.filter
                        ) { flag ->
                            schedule.filter = schedule.filter xor flag
                            refresh(schedule, false)
                        }
                    }
                    item {
                        TitleText(R.string.sched_mode)
                        MultiSelectableChipGroup(
                            list = scheduleBackupModeChipItems,
                            selectedFlags = schedule.mode
                        ) { flag ->
                            schedule.mode = schedule.mode xor flag
                            refresh(schedule, false)
                        }
                    }
                    item {
                        TitleText(R.string.other_filters_options)
                        SelectableChipGroup(
                            list = schedSpecialFilterChipItems,
                            selectedFlag = schedule.specialFilter
                        ) { flag ->
                            schedule.specialFilter = flag
                            refresh(schedule, false)
                        }
                    }
                }
            }
        }
    }
}