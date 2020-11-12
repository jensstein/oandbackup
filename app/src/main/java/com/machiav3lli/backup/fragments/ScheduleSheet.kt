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
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.ChipGroup
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.databinding.SheetScheduleBinding
import com.machiav3lli.backup.dialogs.IntervalInDaysDialog
import com.machiav3lli.backup.items.SchedulerItemX
import com.machiav3lli.backup.schedules.BlacklistsDBHelper
import com.machiav3lli.backup.schedules.CustomPackageList.showList
import com.machiav3lli.backup.schedules.HandleAlarms
import com.machiav3lli.backup.schedules.HandleAlarms.Companion.timeUntilNextEvent
import com.machiav3lli.backup.schedules.HandleScheduledBackups
import com.machiav3lli.backup.schedules.db.Schedule
import com.machiav3lli.backup.schedules.db.Schedule.Mode
import com.machiav3lli.backup.schedules.db.Schedule.SubMode
import com.machiav3lli.backup.schedules.db.ScheduleDatabase.Companion.getInstance
import com.machiav3lli.backup.tasks.RefreshSchedulesTask
import com.machiav3lli.backup.tasks.RemoveScheduleTask
import com.machiav3lli.backup.tasks.ScheduleExcludeSystemSetTask
import com.machiav3lli.backup.utils.CommandUtils.Command
import java.lang.ref.WeakReference
import java.time.LocalTime

class ScheduleSheet(item: SchedulerItemX) : BottomSheetDialogFragment(), TimePickerDialog.OnTimeSetListener, IntervalInDaysDialog.ConfirmListener {
    private val schedule: Schedule = item.schedule
    private var handleAlarms: HandleAlarms? = null
    private var idNumber: Long = 0
    private var binding: SheetScheduleBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        sheet.setOnShowListener { d: DialogInterface ->
            val bottomSheetDialog = d as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }
        handleAlarms = HandleAlarms(requireContext())
        return sheet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = SheetScheduleBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClicks()
        setupChips()
        setupScheduleInfo()
    }

    private fun setupChips() {
        binding!!.schedMode.check(modeToId(schedule.mode.value))
        binding!!.schedMode.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            changeScheduleMode(idToMode(checkedId), idNumber)
            refreshSheet()
            toggleSecondaryButtons(binding!!.schedMode, idNumber)
        }
        binding!!.schedSubMode.check(subModeToId(schedule.subMode.value))
        binding!!.schedSubMode.setOnCheckedChangeListener { _: ChipGroup?, checkedId: Int ->
            changeScheduleSubmode(idToSubMode(checkedId), idNumber)
            refreshSheet()
        }
    }

    private fun modeToId(mode: Int): Int {
        return when (mode) {
            1 -> R.id.schedUser
            2 -> R.id.schedSystem
            3 -> R.id.schedNewUpdated
            else -> R.id.schedAll
        }
    }

    private fun idToMode(mode: Int): Mode {
        return when (mode) {
            R.id.schedUser -> Mode.USER
            R.id.schedSystem -> Mode.SYSTEM
            R.id.schedNewUpdated -> Mode.NEW_UPDATED
            else -> Mode.ALL
        }
    }

    private fun subModeToId(subMode: Int): Int {
        return when (subMode) {
            1 -> R.id.schedApk
            2 -> R.id.schedData
            else -> R.id.schedBoth
        }
    }

    private fun idToSubMode(subMode: Int): SubMode {
        return when (subMode) {
            R.id.schedApk -> SubMode.APK
            R.id.schedData -> SubMode.DATA
            else -> SubMode.BOTH
        }
    }

    private fun setupScheduleInfo() {
        binding!!.timeOfDay.text = LocalTime.of(schedule.timeHour, schedule.timeMinute).toString()
        binding!!.intervalDays.text = java.lang.String.valueOf(schedule.interval)
        binding!!.enableCheckbox.isChecked = schedule.enabled
        binding!!.enableCustomList.isChecked = schedule.enableCustomList
        setTimeLeft(schedule, System.currentTimeMillis())
        idNumber = schedule.id
        toggleSecondaryButtons(binding!!.schedMode, idNumber)
        binding!!.removeButton.tag = idNumber
        binding!!.activateButton.tag = idNumber
        binding!!.enableCheckbox.tag = idNumber
        binding!!.enableCustomList.tag = idNumber
        binding!!.customListUpdate.tag = idNumber
    }

    private fun refreshSheet() {
        updateScheduleData(getScheduleDataFromView(schedule.id.toInt()))
        RefreshSchedulesTask(requireActivity() as SchedulerActivityX).execute()
    }

    private fun setTimeLeft(schedule: Schedule, now: Long) {
        if (!schedule.enabled) {
            binding!!.timeLeft.text = ""
            binding!!.timeLeftLine.visibility = View.GONE
        } else {
            val timeDiff = timeUntilNextEvent(schedule.interval,
                    schedule.timeHour, schedule.timeMinute, schedule.timePlaced, now)
            binding!!.timeLeft.text = DateUtils.formatElapsedTime(timeDiff / 1000L)
            binding!!.timeLeftLine.visibility = View.VISIBLE
        }
    }

    private fun setupOnClicks() {
        binding!!.dismiss.setOnClickListener { dismissAllowingStateLoss() }
        binding!!.timeOfDay.setOnClickListener {
            TimePickerDialog(requireContext(), this,
                    schedule.timeHour, schedule.timeMinute, true).show()
        }
        binding!!.intervalDays.setOnClickListener {
            IntervalInDaysDialog(this, binding!!.intervalDays.text)
                    .show(requireActivity().supportFragmentManager, "DialogFragment")
        }
        binding!!.excludeSystem.setOnClickListener { refreshSheet() }
        binding!!.enableCustomList.setOnClickListener { refreshSheet() }
        binding!!.customListUpdate.setOnClickListener {
            showList(requireActivity(), idNumber.toInt(), idToMode(binding!!.schedMode.checkedChipId))
        }
        binding!!.enableCheckbox.setOnClickListener {
            val id = schedule.id
            val schedule = getScheduleDataFromView(id.toInt())
            val updateScheduleRunnable = UpdateScheduleRunnable(requireActivity() as SchedulerActivityX,
                    BlacklistsDBHelper.DATABASE_NAME, schedule)
            Thread(updateScheduleRunnable).start()
            if (!schedule.enabled) {
                handleAlarms!!.cancelAlarm(id.toInt())
            }
            setTimeLeft(schedule, System.currentTimeMillis())
            RefreshSchedulesTask(requireActivity() as SchedulerActivityX).execute()
        }
        binding!!.removeButton.setOnClickListener {
            RemoveScheduleTask(requireActivity() as SchedulerActivityX).execute(schedule)
            RefreshSchedulesTask(requireActivity() as SchedulerActivityX).execute()
            dismissAllowingStateLoss()
        }
        binding!!.activateButton.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                    .setMessage(getString(com.machiav3lli.backup.R.string.sched_activateButton))
                    .setPositiveButton(com.machiav3lli.backup.R.string.dialogOK) { _: DialogInterface?, _: Int ->
                        StartSchedule(requireContext(),
                                HandleScheduledBackups(requireContext()), idNumber, BlacklistsDBHelper.DATABASE_NAME).execute()
                    }
                    .setNegativeButton(com.machiav3lli.backup.R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
                    .show()
        }
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        binding!!.timeOfDay.text = "${if (hourOfDay < 10) "0$hourOfDay" else hourOfDay}:${if (minute < 10) "0$minute" else minute}"
        refreshSheet()
    }

    override fun onIntervalConfirmed(intervalInDays: Int) {
        binding!!.intervalDays.text = intervalInDays.toString()
        refreshSheet()
    }

    private fun changeScheduleMode(mode: Mode, id: Long) {
        val modeChangerRunnable = ModeChangerRunnable(requireActivity() as SchedulerActivityX, id, mode)
        Thread(modeChangerRunnable).start()
    }

    private fun changeScheduleSubmode(subMode: SubMode, id: Long) {
        val modeChangerRunnable = ModeChangerRunnable(requireActivity() as SchedulerActivityX, id, subMode)
        Thread(modeChangerRunnable).start()
    }

    private fun updateScheduleData(schedule: Schedule) {
        val updateScheduleRunnable = UpdateScheduleRunnable(requireActivity() as SchedulerActivityX,
                BlacklistsDBHelper.DATABASE_NAME, schedule)
        Thread(updateScheduleRunnable).start()
        setTimeLeft(schedule, System.currentTimeMillis())
    }

    private fun getScheduleDataFromView(id: Int): Schedule {
        val enableCustomList = binding!!.enableCustomList.isChecked
        val excludeSystemPackages = binding!!.excludeSystem.isChecked
        val enabled = binding!!.enableCheckbox.isChecked
        val time = binding!!.timeOfDay.text.toString().split(":").toTypedArray()
        val timeHour = time[0].toInt()
        val timeMinute = time[1].toInt()
        val interval = binding!!.intervalDays.text.toString().toInt()
        if (enabled) handleAlarms!!.setAlarm(id, interval, timeHour, timeMinute)
        return Schedule.Builder()
                .withId(id)
                .withTimeHour(timeHour)
                .withTimeMinute(timeMinute)
                .withInterval(interval)
                .withMode(idToMode(binding!!.schedMode.checkedChipId))
                .withSubmode(idToSubMode(binding!!.schedSubMode.checkedChipId))
                .withTimePlaced(System.currentTimeMillis())
                .withEnabled(enabled)
                .withExcludeSystem(excludeSystemPackages)
                .withEnableCustomList(enableCustomList)
                .build()
    }

    private fun toggleSecondaryButtons(chipGroup: ChipGroup, number: Long) {
        if (chipGroup.checkedChipId == R.id.schedNewUpdated) {
            if (binding!!.excludeSystem.visibility != View.GONE) return
            binding!!.excludeSystem.visibility = View.VISIBLE
            binding!!.excludeSystem.tag = number
            ScheduleExcludeSystemSetTask(requireActivity() as SchedulerActivityX,
                    number, binding!!.excludeSystem).execute()
        } else {
            binding!!.excludeSystem.visibility = View.GONE
        }
    }

    internal class ModeChangerRunnable : Runnable {
        private val activityReference: WeakReference<SchedulerActivityX?>
        private val id: Long
        private val mode: Mode?
        private val subMode: SubMode?
        private val databaseName: String

        constructor(scheduler: SchedulerActivityX?, id: Long, mode: Mode, databaseName: String = BlacklistsDBHelper.DATABASE_NAME) {
            activityReference = WeakReference(scheduler)
            this.id = id
            this.mode = mode
            this.subMode = null
            this.databaseName = databaseName
        }

        constructor(scheduler: SchedulerActivityX?, id: Long, subMode: SubMode, databaseName: String = BlacklistsDBHelper.DATABASE_NAME) {
            activityReference = WeakReference(scheduler)
            this.id = id
            this.subMode = subMode
            this.mode = null
            this.databaseName = databaseName
        }

        override fun run() {
            val scheduler = activityReference.get()
            if (scheduler != null && !scheduler.isFinishing) {
                val scheduleDatabase = getInstance(scheduler, databaseName)
                val scheduleDao = scheduleDatabase.scheduleDao
                val schedule = scheduleDao.getSchedule(id)
                if (schedule != null) {
                    if (this.mode != null) schedule.mode = this.mode
                    if (this.subMode != null) schedule.subMode = this.subMode
                    scheduleDao.update(schedule)
                } else {
                    val schedules = scheduleDao.all
                    Log.e(TAG, "Unable to change mode for $id, couldn't get schedule " +
                            "from database. Persisted schedules: $schedules")
                    scheduler.runOnUiThread(Runnable {
                        val state = if (mode != null) "mode" else "subMode"
                        Toast.makeText(scheduler,
                                "${scheduler.getString(com.machiav3lli.backup.R.string.error_updating_schedule_mode)}$state$id",
                                Toast.LENGTH_LONG).show()
                    })
                }
            }
        }
    }

    class UpdateScheduleRunnable(scheduler: SchedulerActivityX?, private val databaseName: String, private val schedule: Schedule)
        : Runnable {
        private val activityReference: WeakReference<SchedulerActivityX?> = WeakReference(scheduler)

        override fun run() {
            val scheduler = activityReference.get()
            if (scheduler != null && !scheduler.isFinishing) {
                val scheduleDatabase = getInstance(scheduler, databaseName)
                val scheduleDao = scheduleDatabase.scheduleDao
                scheduleDao.update(schedule)
            }
        }

    }

    // TODO: this class should ideally just implement Runnable but the
    //  confirmation dialog needs to accept those also
    internal class StartSchedule(context: Context, handleScheduledBackups: HandleScheduledBackups,
                                 private val id: Long, private val databaseName: String)
        : Command {
        private val contextReference: WeakReference<Context> = WeakReference(context)
        private val handleScheduledBackupsReference: WeakReference<HandleScheduledBackups> = WeakReference(handleScheduledBackups)

        override fun execute() {
            val t = Thread {
                val context = contextReference.get()
                if (context != null) {
                    val scheduleDatabase = getInstance(context, databaseName)
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