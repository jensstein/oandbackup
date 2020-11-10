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
package com.machiav3lli.backup.activities

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.SQLException
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.machiav3lli.backup.BlacklistListener
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ActivitySchedulerXBinding
import com.machiav3lli.backup.dialogs.BlacklistDialogFragment
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.fragments.ScheduleSheet
import com.machiav3lli.backup.fragments.ScheduleSheet.RemoveScheduleTask
import com.machiav3lli.backup.fragments.ScheduleSheet.UpdateScheduleRunnable
import com.machiav3lli.backup.items.SchedulerItemX
import com.machiav3lli.backup.schedules.BlacklistContract
import com.machiav3lli.backup.schedules.BlacklistsDBHelper
import com.machiav3lli.backup.schedules.HandleAlarms
import com.machiav3lli.backup.schedules.SchedulingException
import com.machiav3lli.backup.schedules.db.Schedule
import com.machiav3lli.backup.schedules.db.ScheduleDatabase.Companion.getInstance
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import java.lang.ref.WeakReference

class SchedulerActivityX : BaseActivity(), BlacklistListener {
    var list: ArrayList<SchedulerItemX>? = null
    private var totalSchedules = 0
    var handleAlarms: HandleAlarms? = null
        private set
    private var sheetSchedule: ScheduleSheet? = null
    val schedulerItemAdapter: ItemAdapter<SchedulerItemX> = ItemAdapter()
    private var schedulerFastAdapter: FastAdapter<SchedulerItemX>? = null
    private var binding: ActivitySchedulerXBinding? = null
    private var blacklistsDBHelper: BlacklistsDBHelper? = null
    private var sheetHelp: HelpSheet? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchedulerXBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        handleAlarms = HandleAlarms(this)
        list = ArrayList()
        blacklistsDBHelper = BlacklistsDBHelper(this)
        setupViews()
        setupOnClicks()
    }

    private fun setupViews() {
        schedulerFastAdapter = FastAdapter.with(schedulerItemAdapter)!!
        schedulerFastAdapter!!.setHasStableIds(true)
        binding!!.recyclerView.adapter = schedulerFastAdapter
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this)
        schedulerItemAdapter.add(list!!)
    }

    private fun setupOnClicks() {
        binding!!.backButton.setOnClickListener { finish() }
        schedulerFastAdapter!!.onClickListener = { _: View?, _: IAdapter<SchedulerItemX>, item: SchedulerItemX?, _: Int? ->
            if (sheetSchedule != null) sheetSchedule!!.dismissAllowingStateLoss()
            sheetSchedule = ScheduleSheet(item!!)
            sheetSchedule!!.showNow(supportFragmentManager, "SCHEDULESHEET")
            false
        }
        binding!!.blacklistButton.setOnClickListener {
            Thread {
                val args = Bundle()
                args.putInt(Constants.BLACKLIST_ARGS_ID, GLOBALBLACKLISTID)
                val db = blacklistsDBHelper!!.readableDatabase
                val blacklistedPackages = blacklistsDBHelper!!
                        .getBlacklistedPackages(db, GLOBALBLACKLISTID) as ArrayList<String>
                args.putStringArrayList(Constants.BLACKLIST_ARGS_PACKAGES,
                        blacklistedPackages)
                val blacklistDialogFragment = BlacklistDialogFragment()
                blacklistDialogFragment.arguments = args
                blacklistDialogFragment.addBlacklistListener(this)
                blacklistDialogFragment.show(supportFragmentManager, "blacklistDialog")
            }.start()
        }
        binding!!.fabAddSchedule.setOnClickListener {
            AddScheduleTask(this).execute()
            refreshTask(this).execute()
        }
        binding!!.helpButton.setOnClickListener {
            if (sheetHelp == null) sheetHelp = HelpSheet()
            sheetHelp!!.showNow(this@SchedulerActivityX.supportFragmentManager, "SCHEDULESHEET")
        }
        schedulerFastAdapter!!.addEventHook(OnDeleteClickHook())
        schedulerFastAdapter!!.addEventHook(OnEnableClickHook())
    }

    override fun onPause() {
        super.onPause()
        if (sheetSchedule != null) sheetSchedule!!.dismissAllowingStateLoss()
    }

    public override fun onResume() {
        super.onResume()
        refreshTask(this).execute()
    }

    public override fun onDestroy() {
        blacklistsDBHelper!!.close()
        super.onDestroy()
    }

    override fun onBlacklistChanged(blacklist: Array<CharSequence>, id: Int) {
        Thread {
            val db = blacklistsDBHelper!!.writableDatabase
            blacklistsDBHelper!!.deleteBlacklistFromId(db, id)
            for (packageName in blacklist) {
                val values = ContentValues()
                values.put(BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME, packageName as String)
                values.put(BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID, id.toString())
                db.insert(BlacklistContract.BlacklistEntry.TABLE_NAME, null, values)
            }
        }.start()
    }

    fun getList(): List<SchedulerItemX>? {
        return list
    }

    @Throws(SchedulingException::class)
    fun migrateSchedulesToDatabase(preferences: SharedPreferences) {
        val scheduleDao = getInstance(this, DATABASE_NAME).scheduleDao
        for (i in 0 until totalSchedules) {
            val schedule = Schedule.fromPreferences(preferences, i.toLong())
            // The database is one-indexed so in order to preserve the
            // order of the inserted schedules we have to increment the id.
            schedule.id = i + 1L
            try {
                val ids = scheduleDao.insert(schedule)
                // TODO: throw an exception if renaming failed. This requires
                //  the renaming logic to propagate errors properly.
                removePreferenceEntries(preferences, i)
                if (schedule.enabled) {
                    handleAlarms!!.cancelAlarm(i)
                    handleAlarms!!.setAlarm(ids!![0].toInt(),
                            schedule.interval, schedule.timeHour, schedule.timeMinute)
                }
            } catch (e: SQLException) {
                throw SchedulingException(
                        "Unable to migrate schedules to database", e)
            }
        }
    }

    private fun removePreferenceEntries(preferences: SharedPreferences, number: Int) {
        preferences.edit()
                .remove(Constants.PREFS_SCHEDULES_ENABLED + number)
                .remove(Constants.PREFS_SCHEDULES_EXCLUDESYSTEM + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEHOUR + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEMINUTE + number)
                .remove(Constants.PREFS_SCHEDULES_INTERVAL + number)
                .remove(Constants.PREFS_SCHEDULES_MODE + number)
                .remove(Constants.PREFS_SCHEDULES_SUBMODE + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEPLACED + number)
                .remove(Constants.PREFS_SCHEDULES_TIMEUNTILNEXTEVENT + number)
                .remove(Constants.PREFS_SCHEDULES_ENABLECUSTOMLIST + number)
                .remove(Constants.PREFS_SCHEDULES_CUSTOMLIST + number)
                .apply()
    }

    private fun refresh(schedules: List<Schedule>) {
        list = ArrayList()
        if (schedules.isNotEmpty()) for (schedule in schedules) list!!.add(SchedulerItemX(schedule))
        schedulerItemAdapter.clear()
        schedulerItemAdapter.add(list!!)
    }

    // TODO rebase those Tasks, as AsyncTask is deprecated
    internal class AddScheduleTask(scheduler: SchedulerActivityX) : AsyncTask<Void?, Void?, ResultHolder<Schedule>>() {
        private val activityReference: WeakReference<SchedulerActivityX> = WeakReference(scheduler)
        private val databasename: String = DATABASE_NAME

        override fun doInBackground(vararg params: Void?): ResultHolder<Schedule>? {
            val scheduler = activityReference.get()
            return if (scheduler == null || scheduler.isFinishing) ResultHolder() else ResultHolder(insertNewSchedule(databasename, scheduler))
        }

        public override fun onPostExecute(resultHolder: ResultHolder<Schedule>) {
            val scheduler = activityReference.get()
            if (scheduler != null && !scheduler.isFinishing && resultHolder.artifact != null) {
                scheduler.list!!.add(SchedulerItemX(resultHolder.artifact))
            }
        }

        private fun insertNewSchedule(databaseName: String, context: Context): Schedule {
            val schedule = Schedule.Builder() // Set id to 0 to make the database generate a new id
                    .withId(0)
                    .build()
            val scheduleDatabase = getInstance(context, databaseName)
            val scheduleDao = scheduleDatabase.scheduleDao
            val ids = scheduleDao.insert(schedule)
            // update schedule id with one generated by the database
            schedule.id = ids!![0]
            return schedule
        }
    }

    // TODO rebase those Tasks, as AsyncTask is deprecated
    class refreshTask(scheduler: SchedulerActivityX) : AsyncTask<Void?, Void?, ResultHolder<ArrayList<Schedule>>>() {
        private val activityReference: WeakReference<SchedulerActivityX> = WeakReference(scheduler)

        override fun doInBackground(vararg params: Void?): ResultHolder<ArrayList<Schedule>>? {
            val scheduler = activityReference.get()
            if (scheduler == null || scheduler.isFinishing) {
                return ResultHolder()
            }
            val preferences = scheduler.getSharedPreferences(Constants.PREFS_SCHEDULES, 0)
            if (preferences.contains(Constants.PREFS_SCHEDULES_TOTAL)) {
                scheduler.totalSchedules = preferences.getInt(Constants.PREFS_SCHEDULES_TOTAL, 0)
                // set to zero so there is always at least one schedule on activity start
                scheduler.totalSchedules = Math.max(scheduler.totalSchedules, 0)
                try {
                    scheduler.migrateSchedulesToDatabase(preferences)
                    preferences.edit().remove(Constants.PREFS_SCHEDULES_TOTAL).apply()
                } catch (e: SchedulingException) {
                    return ResultHolder(e)
                }
            }
            val scheduleDao = getInstance(scheduler, DATABASE_NAME)
                    .scheduleDao
            val arrayList = ArrayList(scheduleDao.all)
            return ResultHolder(arrayList)
        }

        public override fun onPostExecute(resultHolder: ResultHolder<ArrayList<Schedule>>) {
            val scheduler = activityReference.get()
            if (scheduler != null && !scheduler.isFinishing) {
                if (resultHolder.error != null) {
                    val message = "Unable to migrate schedules to database: ${resultHolder.error}"
                    Log.e(TAG, message)
                    Toast.makeText(scheduler, message, Toast.LENGTH_LONG).show()
                }
                if (resultHolder.artifact != null) {
                    scheduler.refresh(resultHolder.artifact)
                }
            }
        }
    }

    // TODO rebase those Tasks, as AsyncTask is deprecated
    class SystemExcludeCheckboxSetTask(scheduler: SchedulerActivityX, id: Long, checkBox: AppCompatCheckBox) : AsyncTask<Void?, Void?, ResultHolder<Boolean>>() {
        private val activityReference: WeakReference<SchedulerActivityX> = WeakReference(scheduler)
        private val checkBoxReference: WeakReference<AppCompatCheckBox> = WeakReference(checkBox)
        private val id: Long = id

        override fun doInBackground(vararg params: Void?): ResultHolder<Boolean>? {
            val scheduler = activityReference.get()
            if (scheduler != null && !scheduler.isFinishing) {
                val scheduleDao = getInstance(scheduler, DATABASE_NAME).scheduleDao
                val schedule = scheduleDao.getSchedule(id)
                return ResultHolder(schedule!!.excludeSystem)
            }
            return ResultHolder()
        }

        public override fun onPostExecute(resultHolder: ResultHolder<Boolean>) {
            val scheduler = activityReference.get()
            val checkBox = checkBoxReference.get()
            if (scheduler != null && !scheduler.isFinishing && checkBox != null && resultHolder.artifact != null) {
                checkBox.isChecked = resultHolder.artifact
            }
        }
    }

    inner class OnDeleteClickHook : ClickEventHook<SchedulerItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.delete)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<SchedulerItemX>, item: SchedulerItemX) {
            RemoveScheduleTask(this@SchedulerActivityX).execute(item.schedule)
            refreshTask(this@SchedulerActivityX).execute()
        }
    }

    inner class OnEnableClickHook : ClickEventHook<SchedulerItemX>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            return viewHolder.itemView.findViewById(R.id.enableCheckbox)
        }

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<SchedulerItemX>, item: SchedulerItemX) {
            item.schedule.enabled = (v as AppCompatCheckBox).isChecked
            val updateScheduleRunnable = UpdateScheduleRunnable(this@SchedulerActivityX, BlacklistsDBHelper.DATABASE_NAME, item.schedule)
            Thread(updateScheduleRunnable).start()
            if (!item.schedule.enabled) {
                handleAlarms!!.cancelAlarm(item.schedule.id.toInt())
            }
            schedulerFastAdapter!!.notifyAdapterDataSetChanged()
        }
    }

    class ResultHolder<T> {
        val artifact: T?
        val error: Throwable?

        internal constructor() {
            artifact = null
            error = null
        }

        internal constructor(artifact: T) {
            this.artifact = artifact
            error = null
        }

        internal constructor(error: Throwable) {
            this.error = error
            artifact = null
        }
    }

    companion object {
        private val TAG = classTag(".SchedulerActivityX")
        const val GLOBALBLACKLISTID = -1
        const val DATABASE_NAME = "schedules.db"
    }
}