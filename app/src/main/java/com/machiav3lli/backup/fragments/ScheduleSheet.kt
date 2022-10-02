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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
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

class ScheduleSheet(private val scheduleId: Long) : BaseSheet() {
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var database: ODatabase

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
        schedule: Schedule? = viewModel.schedule.value,
        rescheduleBoolean: Boolean
    ) {
        viewModel.updateSchedule(schedule, rescheduleBoolean)
    }

    private fun startSchedule() {
        viewModel.schedule.value?.let {
            val message = StringBuilder()
            message.append(
                "\n${getString(R.string.sched_mode)} ${
                    modesToString(
                        requireContext(),
                        modeToModes(it.mode)
                    )
                }"
            )
            message.append(
                "\n${getString(R.string.backup_filters)} ${
                    filterToString(
                        requireContext(),
                        it.filter
                    )
                }"
            )
            message.append(
                "\n${getString(R.string.other_filters_options)} ${
                    specialFilterToString(
                        requireContext(),
                        it.specialFilter
                    )
                }"
            )
            // TODO list the CL packages
            message.append(
                "\n${getString(R.string.customListTitle)}: ${
                    if (it.customList.isNotEmpty()) getString(
                        R.string.dialogYes
                    ) else getString(R.string.dialogNo)
                }"
            )
            // TODO list the BL packages
            message.append(
                "\n${getString(R.string.sched_blocklist)}: ${
                    if (it.blockList.isNotEmpty()) getString(
                        R.string.dialogYes
                    ) else getString(R.string.dialogNo)
                }"
            )
            AlertDialog.Builder(requireActivity())
                .setTitle("${it.name}: ${getString(R.string.sched_activateButton)}?")
                .setMessage(message)
                .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                    if (it.mode != MODE_UNSET)
                        StartSchedule(requireContext(), database.scheduleDao, scheduleId).execute()
                }
                .setNegativeButton(R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                .show()
        }
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

    private fun showNameEditorDialog() {
        ScheduleNameDialog(viewModel.schedule.value?.name.toString()) {
            viewModel.schedule.value?.name = it
            refresh(rescheduleBoolean = false)
        }.show(requireActivity().supportFragmentManager, "SCHEDULENAME_DIALOG")
    }

    private fun showTimePickerDialog() {
        TimePickerDialog(
            requireContext(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialTimePicker,
            { _, hourOfDay, minute ->
                viewModel.schedule.value?.timeHour = hourOfDay
                viewModel.schedule.value?.timeMinute = minute
                refresh(rescheduleBoolean = true)
            },
            viewModel.schedule.value?.timeHour ?: 0,
            viewModel.schedule.value?.timeMinute ?: 0,
            true
        )
            .show()
    }

    private fun showIntervalSetterDialog() {
        IntervalInDaysDialog(viewModel.schedule.value?.interval.toString()) { newInterval: Int ->
            viewModel.schedule.value?.interval = newInterval
            refresh(rescheduleBoolean = true)
        }.show(requireActivity().supportFragmentManager, "INTERVALDAYS_DIALOG")
    }

    private fun showCustomListDialog() {
        val selectedPackages = viewModel.schedule.value?.customList?.toList() ?: listOf()
        PackagesListDialogFragment(
            selectedPackages, viewModel.schedule.value?.filter
                ?: MAIN_FILTER_DEFAULT, false
        ) { newList: Set<String> ->
            viewModel.schedule.value?.customList = newList
            refresh(rescheduleBoolean = false)
        }.show(requireActivity().supportFragmentManager, "CUSTOMLIST_DIALOG")
    }

    private fun showBlockListDialog() {
        val blocklistedPackages = viewModel.schedule.value?.blockList?.toList() ?: listOf()
        PackagesListDialogFragment(
            blocklistedPackages, viewModel.schedule.value?.filter
                ?: MAIN_FILTER_DEFAULT, true
        ) { newList: Set<String> ->
            viewModel.schedule.value?.blockList = newList
            refresh(rescheduleBoolean = false)
        }.show(requireActivity().supportFragmentManager, "BLOCKLIST_DIALOG")
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun SchedulePage() {
        val schedule by viewModel.schedule.observeAsState()
        val (checked, check) = mutableStateOf(viewModel.schedule.value?.enabled == true)
        val nestedScrollConnection = rememberNestedScrollInteropConnection()

        AppTheme {
            schedule?.let {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Row {
                                AnimatedVisibility(visible = checked) {
                                    Text(
                                        text = "${stringResource(id = R.string.sched_timeLeft)} "
                                            .plus(getTimeLeft(it))
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CheckChip(
                                    checked = it.enabled,
                                    textId = R.string.sched_checkbox,
                                    checkedTextId = R.string.enabled,
                                    onCheckedChange = { checked ->
                                        check(checked)
                                        it.enabled = checked
                                        refresh(it, true)
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
                                onClick = { startSchedule() }
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
                                    text = it.name,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showNameEditorDialog() },
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
                                    text = LocalTime.of(it.timeHour, it.timeMinute).toString(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { showTimePickerDialog() },
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
                                    text = it.interval.toString(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable { showIntervalSetterDialog() },
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
                                    positive = it.customList.isNotEmpty(),
                                    fullWidth = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showCustomListDialog() }
                                )
                                ElevatedActionButton(
                                    icon = Phosphor.Prohibit,
                                    text = stringResource(id = R.string.sched_blocklist),
                                    positive = it.blockList.isNotEmpty(),
                                    fullWidth = true,
                                    modifier = Modifier.weight(1f),
                                    onClick = { showBlockListDialog() }
                                )
                            }
                        }

                        item {
                            TitleText(R.string.filter_options)
                            MultiSelectableChipGroup(
                                list = if (requireContext().specialBackupsEnabled) mainFilterChipItems
                                else mainFilterChipItems.minus(ChipItem.Special),
                                selectedFlags = it.filter
                            ) { flag ->
                                it.filter = it.filter xor flag
                                refresh(it, false)
                            }
                        }
                        item {
                            TitleText(R.string.sched_mode)
                            MultiSelectableChipGroup(
                                list = scheduleBackupModeChipItems,
                                selectedFlags = it.mode
                            ) { flag ->
                                it.mode = it.mode xor flag
                                refresh(it, false)
                            }
                        }
                        item {
                            TitleText(R.string.other_filters_options)
                            SelectableChipGroup(
                                list = schedSpecialFilterChipItems,
                                selectedFlag = it.specialFilter
                            ) { flag ->
                                it.specialFilter = flag
                                refresh(it, false)
                            }
                        }
                    }
                }
            }
        }
    }
}