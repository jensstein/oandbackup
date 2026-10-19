package com.machiav3lli.backup.data.repository

import com.machiav3lli.backup.data.dbs.dao.AppExtrasDao
import com.machiav3lli.backup.data.dbs.entity.AppExtras
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class AppExtrasRepository(
    private val dao: AppExtrasDao
) {
    fun getAllFlow() = dao.getAllFlow()

    fun getTagsMapFlow() = dao.getTagsMapFlow()

    suspend fun getAll() = dao.getAll()

    fun getFlow(packageName: Flow<String?>) = packageName.flatMapLatest {
        dao.getFlow(it)
    }

    suspend fun replaceExtras(packageName: String, appExtras: AppExtras?) {
        if (appExtras != null) dao.upsert(appExtras)
        else dao.deleteByPackageName(packageName)
    }
}