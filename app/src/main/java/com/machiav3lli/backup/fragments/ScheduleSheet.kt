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

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SCHED_FILTER_ALL
import com.machiav3lli.backup.SCHED_FILTER_NEW_UPDATED
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.databinding.SheetScheduleBinding
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog
import com.machiav3lli.backup.dialogs.PackagesListDialogFragment
import com.machiav3lli.backup.dialogs.ScheduleNameDialog
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.services.ScheduleService
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.ScheduleViewModel
import com.machiav3lli.backup.viewmodels.ScheduleViewModelFactory
import java.lang.ref.WeakReference
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class ScheduleSheet(private val scheduleId: Long) : BottomSheetDialogFragment() {
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var binding: SheetScheduleBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return sheet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = SheetScheduleBinding.inflate(inflater, container, false)
        val scheduleDB = ScheduleDatabase.getInstance(requireContext()).scheduleDao
        val viewModelFactory = ScheduleViewModelFactory(scheduleId, scheduleDB, requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ScheduleViewModel::class.java)

        viewModel.schedule.observe(viewLifecycleOwner, {
            binding.schedName.text = it.name
            binding.schedFilter.check(filterToId(it.filter))
            modeToIds(it.mode).forEach { id ->
                binding.schedMode.check(id)
            }
            binding.enableCheckbox.isChecked = it.enabled
            binding.customListButton.setColor(it.customList)
            binding.blocklistButton.setColor(it.blockList)
            setTimeLeft(it, System.currentTimeMillis())
            binding.timeOfDay.text = LocalTime.of(it.timeHour, it.timeMinute).toString()
            binding.intervalDays.text = java.lang.String.valueOf(it.interval)
            binding.excludeSystem.setExists(viewModel.schedule.value?.filter == SCHED_FILTER_NEW_UPDATED)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClicks()
    }

    private fun setupOnClicks() {
        binding.dismiss.setOnClickListener { dismissAllowingStateLoss() }
        binding.schedName.setOnClickListener {
            ScheduleNameDialog(binding.schedName.text) {
                viewModel.schedule.value?.name = it
                refresh(false)
            }.show(requireActivity().supportFragmentManager, "SCHEDULENAME_DIALOG")
        }
        binding.schedFilter.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            viewModel.schedule.value?.filter = idToFilter(checkedId)
            refresh(false)
        }
        binding.schedMode.children.forEach {
            it.setOnClickListener {
                viewModel.schedule.value?.let { schedule ->
                    schedule.mode = schedule.mode xor idToMode(it.id)
                }
                refresh(false)
            }
        }
        binding.timeOfDay.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                viewModel.schedule.value?.timeHour = hourOfDay
                viewModel.schedule.value?.timeMinute = minute
                refresh(true)
            }, viewModel.schedule.value?.timeHour ?: 0,
                    viewModel.schedule.value?.timeMinute ?: 0, true)
                    .show()
        }
        binding.intervalDays.setOnClickListener {
            IntervalInDaysDialog(binding.intervalDays.text) { intervalInDays: Int ->
                viewModel.schedule.value?.interval = intervalInDays
                refresh(true)
            }.show(requireActivity().supportFragmentManager, "INTERVALDAYS_DIALOG")
        }
        binding.excludeSystem.setOnCheckedChangeListener { _, isChecked ->
            viewModel.schedule.value?.excludeSystem = isChecked
            refresh(false)
        }
        binding.customListButton.setOnClickListener {
            val selectedPackages = viewModel.schedule.value?.customList?.toList() ?: listOf()
            PackagesListDialogFragment(selectedPackages, viewModel.schedule.value?.filter
                    ?: SCHED_FILTER_ALL, false) { newList: Set<String> ->
                viewModel.schedule.value?.customList = newList
                refresh(false)
            }.show(requireActivity().supportFragmentManager, "CUSTOMLIST_DIALOG")
        }
        binding.blocklistButton.setOnClickListener {
            val blocklistedPackages = viewModel.schedule.value?.blockList?.toList() ?: listOf()
            PackagesListDialogFragment(blocklistedPackages, viewModel.schedule.value?.filter
                    ?: SCHED_FILTER_ALL, true) { newList: Set<String> ->
                viewModel.schedule.value?.blockList = newList
                refresh(false)
            }.show(requireActivity().supportFragmentManager, "BLOCKLIST_DIALOG")
        }
        binding.enableCheckbox.setOnClickListener {
            viewModel.schedule.value?.enabled = (it as AppCompatCheckBox).isChecked
            refresh(true)
        }
        binding.removeButton.setOnClickListener {
            viewModel.deleteSchedule()
            cancelAlarm(requireContext(), scheduleId.toInt())
            dismissAllowingStateLoss()
        }
        binding.activateButton.setOnClickListener { startSchedule() }
    }

    private fun setTimeLeft(schedule: Schedule, now: Long) {
        if (!schedule.enabled) {
            binding.timeLeft.text = ""
            binding.timeLeftLine.visibility = View.GONE
        } else {
            val timeDiff = abs(timeUntilNextEvent(schedule, now))
            val days = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
            if (days == 0) {
                binding.daysLeft.visibility = View.GONE
            } else {
                binding.daysLeft.visibility = View.VISIBLE
                binding.daysLeft.text = requireContext().resources.getQuantityString(R.plurals.days_left, days, days)
            }
            val hours = TimeUnit.MILLISECONDS.toHours(timeDiff).toInt() % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt() % 60
            binding.timeLeft.text = LocalTime.of(hours, minutes).toString()
            binding.timeLeftLine.visibility = View.VISIBLE
        }
    }

    private fun refresh(rescheduleBoolean: Boolean) {
        Thread(UpdateRunnable(viewModel.schedule.value, requireActivity() as SchedulerActivityX, rescheduleBoolean))
                .start()
    }

    private fun startSchedule() {
        viewModel.schedule.value?.let {
            val message = StringBuilder()
            message.append("\n${getString(R.string.sched_mode)} ${modesToString(requireContext(), modeToModes(it.mode))}")
            message.append("\n${getString(R.string.backup_filters)} ${filterToString(requireContext(), it.filter)}")
            if (it.filter == SCHED_FILTER_NEW_UPDATED)
                message.append("\n${getString(R.string.sched_excludeSystemCheckBox)}: ${if (it.excludeSystem) getString(R.string.dialogYes) else getString(R.string.dialogNo)}")
            message.append("\n${getString(R.string.customListTitle)}: ${if (it.customList.isNotEmpty()) getString(R.string.dialogYes) else getString(R.string.dialogNo)}") // TODO list the packages
            message.append("\n${getString(R.string.sched_blocklist)}: ${if (it.blockList.isNotEmpty()) getString(R.string.dialogYes) else getString(R.string.dialogNo)}") // TODO list the packages
            AlertDialog.Builder(requireActivity())
                    .setTitle("${it.name}: ${getString(R.string.sched_activateButton)}?")
                    .setMessage(message)
                    .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                        if (it.mode != MODE_UNSET)
                            StartSchedule(requireContext(), scheduleId).execute()
                    }
                    .setNegativeButton(R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                    .show()
        }
    }

    class UpdateRunnable(private val schedule: Schedule?, scheduler: SchedulerActivityX?, private val rescheduleBoolean: Boolean)
        : Runnable {
        private val activityReference: WeakReference<SchedulerActivityX?> = WeakReference(scheduler)

        override fun run() {
            val scheduler = activityReference.get()
            if (scheduler != null && !scheduler.isFinishing) {
                val scheduleDatabase = ScheduleDatabase.getInstance(scheduler)
                val scheduleDao = scheduleDatabase.scheduleDao
                schedule?.let {
                    scheduleDao.update(it)
                    if (it.enabled) scheduleAlarm(scheduler, it.id, rescheduleBoolean)
                    else cancelAlarm(scheduler, it.id.toInt())
                }
            }
        }
    }

    internal class StartSchedule(val context: Context, private val scheduleId: Long)
        : ShellCommands.Command {

        override fun execute() {
            Thread {
                val serviceIntent = Intent(context, ScheduleService::class.java)
                serviceIntent.putExtra("scheduleId", scheduleId)
                context.startService(serviceIntent)
            }.start()
        }
    }
}