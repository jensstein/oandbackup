package com.machiav3lli.backup.tasks

import android.util.Log
import android.widget.Toast
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.schedules.SchedulingException
import com.machiav3lli.backup.schedules.db.Schedule
import com.machiav3lli.backup.schedules.db.ScheduleDatabase
import java.lang.ref.WeakReference
import kotlin.math.max

class RefreshSchedulesTask(scheduler: SchedulerActivityX)
    : CoroutinesAsyncTask<Void?, Void?, SchedulerActivityX.ResultHolder<ArrayList<Schedule>>>() {
    private val activityReference: WeakReference<SchedulerActivityX> = WeakReference(scheduler)
    override val TAG = Constants.classTag(".RefreshScheduleTask")

    override fun doInBackground(vararg params: Void?): SchedulerActivityX.ResultHolder<ArrayList<Schedule>>? {
        val scheduler = activityReference.get()
        if (scheduler == null || scheduler.isFinishing) {
            return SchedulerActivityX.ResultHolder()
        }
        val preferences = scheduler.getSharedPreferences(Constants.PREFS_SCHEDULES, 0)
        if (preferences.contains(Constants.PREFS_SCHEDULES_TOTAL)) {
            scheduler.totalSchedules = preferences.getInt(Constants.PREFS_SCHEDULES_TOTAL, 0)
            // set to zero so there is always at least one schedule on activity start
            scheduler.totalSchedules = max(scheduler.totalSchedules, 0)
            try {
                scheduler.migrateSchedulesToDatabase(preferences)
                preferences.edit().remove(Constants.PREFS_SCHEDULES_TOTAL).apply()
            } catch (e: SchedulingException) {
                return SchedulerActivityX.ResultHolder(e)
            }
        }
        val scheduleDao = ScheduleDatabase.getInstance(scheduler, SchedulerActivityX.DATABASE_NAME)
                .scheduleDao
        val arrayList = ArrayList(scheduleDao.all)
        return SchedulerActivityX.ResultHolder(arrayList)
    }

    override fun onPostExecute(result: SchedulerActivityX.ResultHolder<ArrayList<Schedule>>?) {
        val scheduler = activityReference.get()
        if (scheduler != null && !scheduler.isFinishing) {
            if (result?.error != null) {
                val message = "Unable to migrate schedules to database: ${result.error}"
                Log.e(TAG, message)
                Toast.makeText(scheduler, message, Toast.LENGTH_LONG).show()
            }
            if (result?.artifact != null) {
                scheduler.refresh(result.artifact)
            }
        }
    }
}