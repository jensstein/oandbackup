package com.machiav3lli.backup.data.repository

import com.machiav3lli.backup.data.dbs.dao.ScheduleDao
import com.machiav3lli.backup.data.dbs.entity.Schedule
import com.machiav3lli.backup.data.entity.StorageFile
import com.machiav3lli.backup.manager.handler.ExportsHandler
import com.machiav3lli.backup.utils.SystemUtils

class ExportsRepository(
    val handler: ExportsHandler,
    private val dao: ScheduleDao,
) {
    suspend fun recreateExports(): List<Pair<Schedule, StorageFile>> = handler.readExports()
    suspend fun exportSchedules() = handler.exportSchedules()

    suspend fun import(schedule: Schedule, onFinish: (String, Int) -> Unit) {
        dao.insert(
            Schedule.Builder() // Set id to 0 to make the database generate a new id
                .withId(0)
                .import(schedule)
                .build()
        )
        onFinish(schedule.name, SystemUtils.now.toInt())
    }

    suspend fun delete(exportFile: StorageFile): Boolean = exportFile.delete()
}