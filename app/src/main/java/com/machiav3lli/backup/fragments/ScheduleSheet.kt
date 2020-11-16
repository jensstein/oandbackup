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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.databinding.SheetScheduleBinding
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog
import com.machiav3lli.backup.schedules.BlacklistsDBHelper
import com.machiav3lli.backup.schedules.CustomPackageList.showList
import com.machiav3lli.backup.schedules.HandleAlarms
import com.machiav3lli.backup.schedules.HandleAlarms.Companion.timeUntilNextEvent
import com.machiav3lli.backup.schedules.HandleScheduledBackups
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.utils.CommandUtils.Command
import com.machiav3lli.backup.utils.idToMode
import com.machiav3lli.backup.utils.idToSubMode
import com.machiav3lli.backup.utils.modeToId
import com.machiav3lli.backup.utils.subModeToId
import com.machiav3lli.backup.viewmodels.ScheduleViewModel
import com.machiav3lli.backup.viewmodels.ScheduleViewModelFactory
import java.lang.ref.WeakReference
import java.time.LocalTime

class ScheduleSheet(val id: Long) : BottomSheetDialogFragment() {
    private lateinit var handleAlarms: HandleAlarms
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var binding: SheetScheduleBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        handleAlarms = HandleAlarms(requireContext())
        return sheet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = SheetScheduleBinding.inflate(inflater, container, false)

        val dataSource = ScheduleDatabase.getInstance(requireContext(), SchedulerActivityX.DATABASE_NAME).scheduleDao

        val viewModelFactory = ScheduleViewModelFactory(id, dataSource, requireActivity().application)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ScheduleViewModel::class.java)

        viewModel.schedule.observe(viewLifecycleOwner, {
            binding.schedMode.check(modeToId(it.mode.value))
            binding.schedSubMode.check(subModeToId(it.subMode.value))
            binding.enableCheckbox.isChecked = it.enabled
            binding.enableCustomList.isChecked = it.enableCustomList
            setTimeLeft(it, System.currentTimeMillis())
            binding.timeOfDay.text = LocalTime.of(it.timeHour, it.timeMinute).toString()
            binding.intervalDays.text = java.lang.String.valueOf(it.interval)
            toggleSecondaryButtons(binding.schedMode)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClicks()
    }

    private fun setupOnClicks() {
        binding.dismiss.setOnClickListener { dismissAllowingStateLoss() }
        binding.schedMode.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            viewModel.schedule.value?.mode = idToMode(checkedId)
            refresh()
            toggleSecondaryButtons(binding.schedMode)
        }
        binding.schedSubMode.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            viewModel.schedule.value?.subMode = idToSubMode(checkedId)
            refresh()
        }
        binding.timeOfDay.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                viewModel.schedule.value?.timeHour = hourOfDay
                viewModel.schedule.value?.timeMinute = minute
                refresh()
            }, viewModel.schedule.value?.timeHour ?: 0,
                    viewModel.schedule.value?.timeMinute ?: 0, true)
                    .show()
        }
        binding.intervalDays.setOnClickListener {
            IntervalInDaysDialog(binding.intervalDays.text) { intervalInDays: Int ->
                viewModel.schedule.value?.interval = intervalInDays
                refresh()
            }.show(requireActivity().supportFragmentManager, "DialogFragment")
        }
        binding.excludeSystem.setOnCheckedChangeListener { _, isChecked ->
            viewModel.schedule.value?.excludeSystem = isChecked
            refresh()
        }
        binding.enableCustomList.setOnCheckedChangeListener { _, isChecked ->
            viewModel.schedule.value?.enableCustomList = isChecked
            refresh()
        }
        binding.customListUpdate.setOnClickListener {
            showList(requireActivity(), id.toInt(), idToMode(binding.schedMode.checkedChipId))
        }
        binding.enableCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.schedule.value?.enabled = isChecked
            if (!isChecked) handleAlarms.cancelAlarm(id.toInt())
            refresh()
        }
        binding.removeButton.setOnClickListener {
            viewModel.deleteSchedule()
            handleAlarms.cancelAlarm(id.toInt())
            dismissAllowingStateLoss()
        }
        binding.activateButton.setOnClickListener {
            startSchedule()
        }
    }

    private fun setTimeLeft(schedule: Schedule, now: Long) {
        if (!schedule.enabled) {
            binding.timeLeft.text = ""
            binding.timeLeftLine.visibility = View.GONE
        } else {
            val timeDiff = timeUntilNextEvent(schedule.interval,
                    schedule.timeHour, schedule.timeMinute, schedule.timePlaced, now)
            val days = (timeDiff / (1000 * 60 * 60 * 24)).toInt()
            if (days == 0) {
                binding.daysLeft.visibility = View.GONE
            } else {
                binding.daysLeft.visibility = View.VISIBLE
                binding.daysLeft.text = requireContext().resources.getQuantityString(R.plurals.days_left, days, days)
            }
            val hours = (timeDiff / (1000 * 60 * 60)).toInt() % 24
            val minutes = (timeDiff / (1000 * 60)).toInt() % 60
            binding.timeLeft.text = LocalTime.of(hours, minutes).toString()
            binding.timeLeftLine.visibility = View.VISIBLE
        }
    }

    private fun toggleSecondaryButtons(chipGroup: ChipGroup) {
        if (chipGroup.checkedChipId == R.id.schedNewUpdated) {
            if (binding.excludeSystem.visibility != View.GONE) return
            binding.excludeSystem.visibility = View.VISIBLE
        } else {
            binding.excludeSystem.visibility = View.GONE
            viewModel.schedule.value?.excludeSystem = false
        }
    }

    private fun refresh() {
        Thread(UpdateRunnable(viewModel.schedule.value, BlacklistsDBHelper.DATABASE_NAME,
                requireActivity() as SchedulerActivityX))
                .start()
    }

    private fun startSchedule() {
        AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.sched_activateButton))
                .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                    StartSchedule(requireContext(), HandleScheduledBackups(requireContext()),
                            id, BlacklistsDBHelper.DATABASE_NAME)
                            .execute()
                }
                .setNegativeButton(R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                .show()
    }

    class UpdateRunnable(private val schedule: Schedule?, private val databaseName: String,
                         scheduler: SchedulerActivityX?)
        : Runnable {
        private val activityReference: WeakReference<SchedulerActivityX?> = WeakReference(scheduler)

        override fun run() {
            val scheduler = activityReference.get()
            if (scheduler != null && !scheduler.isFinishing) {
                val scheduleDatabase = ScheduleDatabase.getInstance(scheduler, databaseName)
                val scheduleDao = scheduleDatabase.scheduleDao
                schedule?.let { scheduleDao.update(it) }
            }
        }
    }

    internal class StartSchedule(context: Context, handleScheduledBackups: HandleScheduledBackups,
                                 private val id: Long, private val databaseName: String)
        : Command {
        private val contextReference = WeakReference(context)
        private val handleScheduledBackupsReference = WeakReference(handleScheduledBackups)

        override fun execute() {
            val t = Thread {
                val context = contextReference.get()
                if (context != null) {
                    val scheduleDatabase = ScheduleDatabase.getInstance(context, databaseName)
                    val scheduleDao = scheduleDatabase.scheduleDao
                    val schedule = scheduleDao.getSchedule(id)
                    val handleScheduledBackups = handleScheduledBackupsReference.get()
                    handleScheduledBackups?.initiateBackup(id.toInt(), schedule!!.mode,
                            schedule.subMode.value, schedule.excludeSystem, schedule.enableCustomList)
                }
            }
            t.start()
        }
    }

    companion object {
        private val TAG = classTag(".ScheduleSheet")
    }
}