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
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.machiav3lli.backup.BLACKLIST_ARGS_ID
import com.machiav3lli.backup.BLACKLIST_ARGS_PACKAGES
import com.machiav3lli.backup.R
import com.machiav3lli.backup.actions.BaseAppAction
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.databinding.SheetScheduleBinding
import com.machiav3lli.backup.dbs.BlacklistDatabase
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.dbs.ScheduleDatabase
import com.machiav3lli.backup.dialogs.BlacklistDialogFragment
import com.machiav3lli.backup.dialogs.CustomListDialogFragment
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog
import com.machiav3lli.backup.handler.ScheduleJobsHandler
import com.machiav3lli.backup.services.ScheduleJobService
import com.machiav3lli.backup.utils.*
import com.machiav3lli.backup.viewmodels.ScheduleViewModel
import com.machiav3lli.backup.viewmodels.ScheduleViewModelFactory
import java.lang.ref.WeakReference
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ScheduleSheet(private val scheduleId: Long) : BottomSheetDialogFragment(),
        BlacklistDialogFragment.BlacklistListener, CustomListDialogFragment.CustomListListener {
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
        val blacklistDB = BlacklistDatabase.getInstance(requireContext()).blacklistDao

        val viewModelFactory = ScheduleViewModelFactory(scheduleId, scheduleDB, blacklistDB, requireActivity().application)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ScheduleViewModel::class.java)

        viewModel.schedule.observe(viewLifecycleOwner, {
            binding.schedFilter.check(filterToId(it.filter.value))
            binding.schedMode.check(modeToId(it.mode))
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
        binding.schedFilter.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            viewModel.schedule.value?.filter = idToFilter(checkedId)
            refresh(false)
            toggleSecondaryButtons(binding.schedMode)
        }
        binding.schedMode.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            viewModel.schedule.value?.mode = idToMode(checkedId)
            refresh(false)
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
            val args = Bundle()
            args.putInt("listId", viewModel.id.toInt())
            val customList = viewModel.schedule.value?.customList?.toCollection(ArrayList())
            args.putStringArrayList("selectedPackages", customList)

            val customListDialogFragment = CustomListDialogFragment(idToFilter(binding.schedMode.checkedChipId), this)
            customListDialogFragment.arguments = args
            customListDialogFragment.show(requireActivity().supportFragmentManager, "CUSTOMLIST_DIALOG")
        }
        binding.blacklistButton.setOnClickListener {
            val args = Bundle()
            args.putInt(BLACKLIST_ARGS_ID, viewModel.id.toInt())
            val blacklistedPackages = viewModel.blacklist.value ?: arrayListOf()
            args.putStringArrayList(BLACKLIST_ARGS_PACKAGES, blacklistedPackages as ArrayList<String>)

            val blacklistDialogFragment = BlacklistDialogFragment(this)
            blacklistDialogFragment.arguments = args
            blacklistDialogFragment.show(requireActivity().supportFragmentManager, "BLACKLIST_DIALOG")
        }
        binding.enableCheckbox.setOnClickListener {
            viewModel.schedule.value?.enabled = (it as AppCompatCheckBox).isChecked
            refresh(true)
        }
        binding.removeButton.setOnClickListener {
            viewModel.deleteSchedule()
            ScheduleJobsHandler.cancelJob(requireContext(), scheduleId.toInt())
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
            val timeDiff = ScheduleJobsHandler.timeUntilNextEvent(schedule, now)
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

    private fun refresh(rescheduleBoolean: Boolean) {
        Thread(UpdateRunnable(viewModel.schedule.value, requireActivity() as SchedulerActivityX, rescheduleBoolean))
                .start()
    }

    override fun onCustomListChanged(newList: Set<String>, blacklistId: Long) {
        viewModel.schedule.value?.customList = newList
        refresh(false)
    }

    override fun onBlacklistChanged(newList: Set<String>, blacklistId: Long) {
        viewModel.updateBlacklist(newList)
    }

    private fun startSchedule() {
        viewModel.schedule.value?.let {
            val message = StringBuilder()
            message.append("\n${getModeString(it.mode, requireContext())}")
            message.append("\n${getFilterString(it.filter, requireContext())}")
            if (it.filter == Schedule.Filter.NEW_UPDATED) message.append("\n${getString(R.string.sched_excludeSystemCheckBox)}: ${it.excludeSystem}")
            message.append("\n${getString(R.string.customListTitle)}: ${it.enableCustomList}")
            AlertDialog.Builder(requireActivity())
                    .setTitle("${getString(R.string.sched_activateButton)}?")
                    .setMessage(message)
                    .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                        StartSchedule(requireContext(), scheduleId)
                                .execute()
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
                    if (it.enabled) ScheduleJobsHandler.scheduleJob(scheduler, it.id, rescheduleBoolean)
                    else ScheduleJobsHandler.cancelJob(scheduler, it.id.toInt())
                }
            }
        }
    }

    internal class StartSchedule(val context: Context, private val scheduleId: Long)
        : Command {

        override fun execute() {
            Thread {
                val extras = PersistableBundle()
                extras.putLong("scheduleId", scheduleId)
                val jobScheduler = context.getSystemService(JobService.JOB_SCHEDULER_SERVICE) as JobScheduler
                val jobInfoBuilder: JobInfo.Builder = JobInfo.Builder(scheduleId.toInt(), ComponentName(context, ScheduleJobService::class.java))
                        .setPersisted(true)
                        .setRequiresDeviceIdle(false)
                        .setRequiresCharging(false)
                        .setExtras(PersistableBundle(extras))
                        .setOverrideDeadline(1000) // crashes on <= SDK28 when having no constraints
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    jobInfoBuilder.setImportantWhileForeground(true)
                }
                jobScheduler.schedule(jobInfoBuilder.build())
            }.start()
        }
    }

    companion object {
        private fun getModeString(mode: Int, context: Context): String {
            return when (mode) {
                BaseAppAction.MODE_APK -> context.getString(R.string.handleApk)
                BaseAppAction.MODE_DATA -> context.getString(R.string.handleData)
                else -> context.getString(R.string.handleBoth)
            }
        }

        private fun getFilterString(filter: Schedule.Filter, context: Context): String {
            return when (filter) {
                Schedule.Filter.ALL -> context.getString(R.string.radio_all)
                Schedule.Filter.SYSTEM -> context.getString(R.string.radio_system)
                Schedule.Filter.USER -> context.getString(R.string.radio_user)
                Schedule.Filter.NEW_UPDATED -> context.getString(R.string.showNewAndUpdated)
            }
        }
    }
}