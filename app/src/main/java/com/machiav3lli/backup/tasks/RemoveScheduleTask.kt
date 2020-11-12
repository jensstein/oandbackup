package com.machiav3lli.backup.tasks

import android.util.Log
import android.widget.Toast
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.schedules.BlacklistsDBHelper
import com.machiav3lli.backup.schedules.db.Schedule
import com.machiav3lli.backup.schedules.db.ScheduleDatabase
import java.lang.ref.WeakReference

class RemoveScheduleTask(scheduler: SchedulerActivityX?)
    : CoroutinesAsyncTask<Schedule?, Void?, SchedulerActivityX.ResultHolder<Schedule>>() {
    private val activityReference: WeakReference<SchedulerActivityX?> = WeakReference(scheduler)
    override val TAG = classTag(".RemoveScheduleTask")

    override fun doInBackground(vararg params: Schedule?): SchedulerActivityX.ResultHolder<Schedule>? {
        val scheduler = activityReference.get()
        if (scheduler == null || scheduler.isFinishing) return SchedulerActivityX.ResultHolder()
        if (params.isEmpty()) {
            val error = IllegalStateException("No id supplied to the schedule removing task")
            return SchedulerActivityX.ResultHolder(error)
        }
        val scheduleDatabase = ScheduleDatabase.getInstance(scheduler, BlacklistsDBHelper.DATABASE_NAME)
        val scheduleDao = scheduleDatabase.scheduleDao
        scheduleDao.delete(params[0]!!)
        return SchedulerActivityX.ResultHolder(params[0]!!)
    }

    override fun onPostExecute(result: SchedulerActivityX.ResultHolder<Schedule>?) {
        val scheduler = activityReference.get()
        if (scheduler != null && !scheduler.isFinishing) {
            if (result?.error != null) {
                val message = "Unable to remove schedule: ${result.error}"
                Log.e(TAG, message)
                Toast.makeText(scheduler, message, Toast.LENGTH_LONG).show()
            }
            if (result?.artifact != null) {
                remove(scheduler, result.artifact)
            }
        }
    }

    companion object {
        private fun remove(scheduler: SchedulerActivityX, schedule: Schedule) {
            scheduler.handleAlarms!!.cancelAlarm(schedule.id.toInt())
            scheduler.schedulerItemAdapter.clear()
            scheduler.schedulerItemAdapter.add(scheduler.list!!)
        }
    }
}