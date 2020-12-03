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
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.databinding.SheetScheduleBinding
import com.machiav3lli.backup.dbs.BlacklistDatabase
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.dialogs.BlacklistDialogFragment
import com.machiav3lli.backup.dialogs.CustomListDialogFragment
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog
import com.machiav3lli.backup.handler.AlarmsHandler
import com.machiav3lli.backup.handler.ScheduledBackupsHandler
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.ScheduleViewModel
import com.machiav3lli.backup.viewmodels.ScheduleViewModelFactory
import java.lang.ref.WeakReference
import java.time.LocalTime

class ScheduleSheet(val id: Long) : BottomSheetDialogFragment(),
        BlacklistDialogFragment.BlacklistListener, CustomListDialogFragment.CustomListListener {
    private val TAG = classTag(".ScheduleSheet")
    private lateinit var alarmsHandler: AlarmsHandler
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var binding: SheetScheduleBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        alarmsHandler = AlarmsHandler(requireContext())
        return sheet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = SheetScheduleBinding.inflate(inflater, container, false)

        val scheduleDB = ScheduleDatabase.getInstance(requireContext(), SchedulerActivityX.SCHEDULES_DB_NAME).scheduleDao
        val blacklistDB = BlacklistDatabase.getInstance(requireContext()).blacklistDao

        val viewModelFactory = ScheduleViewModelFactory(id, scheduleDB, blacklistDB, requireActivity().application)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ScheduleViewModel::class.java)

        viewModel.schedule.observe(viewLifecycleOwner, {
            binding.schedMode.check(modeToId(it.mode.value))
            binding.schedSubMode.check(subModeToId(it.subMode.value))
            binding.enableCheckbox.isChecked = it.enabled
            when {
                it.customList.isNotEmpty() -> binding.customListButton.setTextColor(requireContext().getColor(R.color.app_accent))
                else -> binding.customListButton.setTextColor(requireContext().getColor(R.color.app_secondary))
            }
            setTimeLeft(it, System.currentTimeMillis())
            binding.timeOfDay.text = LocalTime.of(it.timeHour, it.timeMinute).toString()
            binding.intervalDays.text = java.lang.String.valueOf(it.interval)
            toggleSecondaryButtons(binding.schedMode)
        })
        viewModel.blacklist.observe(viewLifecycleOwner, {
            if (it.isNotEmpty())
                binding.blacklistButton.setTextColor(requireContext().getColor(R.color.app_accent))
            else
                binding.blacklistButton.setTextColor(requireContext().getColor(R.color.app_secondary))
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
            }.show(requireActivity().supportFragmentManager, "INTERVALDAYS_DIALOG")
        }
        binding.excludeSystem.setOnCheckedChangeListener { _, isChecked ->
            viewModel.schedule.value?.excludeSystem = isChecked
            refresh()
        }
        binding.customListButton.setOnClickListener {
            val args = Bundle()
            args.putInt("listId", viewModel.id.toInt())
            val customList = viewModel.schedule.value?.customList?.toCollection(ArrayList())
            args.putStringArrayList("selectedPackages", customList)

            val customListDialogFragment = CustomListDialogFragment(idToMode(binding.schedMode.checkedChipId), this)
            customListDialogFragment.arguments = args
            customListDialogFragment.show(requireActivity().supportFragmentManager, "CUSTOMLIST_DIALOG")
        }
        binding.blacklistButton.setOnClickListener {
            val args = Bundle()
            args.putInt(Constants.BLACKLIST_ARGS_ID, viewModel.id.toInt())
            val blacklistedPackages = viewModel.blacklist.value ?: arrayListOf()
            args.putStringArrayList(Constants.BLACKLIST_ARGS_PACKAGES, blacklistedPackages as ArrayList<String>)

            val blacklistDialogFragment = BlacklistDialogFragment(this)
            blacklistDialogFragment.arguments = args
            blacklistDialogFragment.show(requireActivity().supportFragmentManager, "BLACKLIST_DIALOG")
        }
        binding.enableCheckbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.schedule.value?.enabled = isChecked
            if (!isChecked) alarmsHandler.cancelAlarm(id.toInt())
            refresh()
        }
        binding.removeButton.setOnClickListener {
            viewModel.deleteSchedule()
            alarmsHandler.cancelAlarm(id.toInt())
            viewModel.deleteBlacklist()
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
            val timeDiff = AlarmsHandler.timeUntilNextEvent(schedule.interval,
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
            binding.excludeSystem.isChecked = viewModel.schedule.value?.excludeSystem ?: false
            if (binding.excludeSystem.visibility != View.GONE) return
            binding.excludeSystem.visibility = View.VISIBLE
        } else {
            binding.excludeSystem.visibility = View.GONE
            viewModel.schedule.value?.excludeSystem = false
        }
    }

    private fun refresh() {
        Thread(UpdateRunnable(viewModel.schedule.value, SchedulerActivityX.BLACKLIST_DB_NAME,
                requireActivity() as SchedulerActivityX))
                .start()
    }

    override fun onCustomListChanged(newList: Set<String>, blacklistId: Int) {
        viewModel.schedule.value?.customList = newList
        refresh()
    }

    override fun onBlacklistChanged(newList: Set<String>, blacklistId: Int) {
        viewModel.updateBlacklist(newList)
    }

    private fun startSchedule() {
        viewModel.schedule.value?.let {
            val message = StringBuilder()
            message.append("\n${getSubModeString(it.subMode, requireContext())}")
            message.append("\n${getModeString(it.mode, requireContext())}")
            if (it.mode == Schedule.Mode.NEW_UPDATED) message.append("\n${getString(R.string.sched_excludeSystemCheckBox)}: ${it.excludeSystem}")
            message.append("\n${getString(R.string.customListTitle)}: ${it.enableCustomList}")
            AlertDialog.Builder(requireActivity())
                    .setTitle("${getString(R.string.sched_activateButton)}?")
                    .setMessage(message)
                    .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                        StartSchedule(requireContext(), ScheduledBackupsHandler(requireContext()),
                                id, SchedulerActivityX.BLACKLIST_DB_NAME)
                                .execute()
                    }
                    .setNegativeButton(R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                    .show()
        }
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

    internal class StartSchedule(context: Context, scheduledBackupsHandler: ScheduledBackupsHandler,
                                 private val id: Long, private val databaseName: String)
        : Command {
        private val contextReference = WeakReference(context)
        private val handleScheduledBackupsReference = WeakReference(scheduledBackupsHandler)

        override fun execute() {
            val t = Thread {
                val context = contextReference.get()
                if (context != null) {
                    val scheduleDatabase = ScheduleDatabase.getInstance(context, databaseName)
                    val scheduleDao = scheduleDatabase.scheduleDao
                    val schedule = scheduleDao.getSchedule(id)
                    val handleScheduledBackups = handleScheduledBackupsReference.get()
                    handleScheduledBackups?.initiateBackup(id.toInt(), schedule?.mode, schedule?.subMode,
                            schedule?.excludeSystem == true, schedule?.customList ?: setOf())
                }
            }
            t.start()
        }
    }

    companion object {
        private fun getSubModeString(mode: Schedule.SubMode, context: Context): String {
            return when (mode) {
                Schedule.SubMode.APK -> context.getString(R.string.handleApk)
                Schedule.SubMode.DATA -> context.getString(R.string.handleData)
                Schedule.SubMode.BOTH -> context.getString(R.string.handleBoth)
            }
        }

        private fun getModeString(mode: Schedule.Mode, context: Context): String {
            return when (mode) {
                Schedule.Mode.ALL -> context.getString(R.string.radio_all)
                Schedule.Mode.SYSTEM -> context.getString(R.string.radio_system)
                Schedule.Mode.USER -> context.getString(R.string.radio_user)
                Schedule.Mode.NEW_UPDATED -> context.getString(R.string.showNewAndUpdated)
            }
        }
    }
}