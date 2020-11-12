package com.machiav3lli.backup.tasks

import androidx.appcompat.widget.AppCompatCheckBox
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.schedules.db.ScheduleDatabase
import java.lang.ref.WeakReference

class ScheduleExcludeSystemSetTask(scheduler: SchedulerActivityX, private val id: Long, checkBox: AppCompatCheckBox)
    : CoroutinesAsyncTask<Void?, Void?, SchedulerActivityX.ResultHolder<Boolean>>() {
    private val activityReference: WeakReference<SchedulerActivityX> = WeakReference(scheduler)
    private val checkBoxReference: WeakReference<AppCompatCheckBox> = WeakReference(checkBox)
    override val TAG = Constants.classTag(".ScheduleExcludeSystemSetTask")

    override fun doInBackground(vararg params: Void?): SchedulerActivityX.ResultHolder<Boolean>? {
        val scheduler = activityReference.get()
        if (scheduler != null && !scheduler.isFinishing) {
            val scheduleDao = ScheduleDatabase.getInstance(scheduler, SchedulerActivityX.DATABASE_NAME).scheduleDao
            val schedule = scheduleDao.getSchedule(id)
            return SchedulerActivityX.ResultHolder(schedule!!.excludeSystem)
        }
        return SchedulerActivityX.ResultHolder()
    }

    override fun onPostExecute(result: SchedulerActivityX.ResultHolder<Boolean>?) {
        val scheduler = activityReference.get()
        val checkBox = checkBoxReference.get()
        if (scheduler != null && !scheduler.isFinishing && checkBox != null && result?.artifact != null) {
            checkBox.isChecked = result.artifact
        }
    }
}