package com.machiav3lli.backup.data.repository

import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.data.dbs.dao.BlocklistDao
import com.machiav3lli.backup.data.dbs.entity.Blocklist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class BlocklistRepository(
    private val dao: BlocklistDao
) {
    fun getBlocklist(): Flow<Set<String>> =
        dao.getBlocklistedPackagesFlow().mapLatest { it.toSet() }

    fun getGlobalBlocklist(): Flow<Set<String>> =
        dao.getGlobalBlocklistedPackagesFlow().mapLatest { it.toSet() }

    suspend fun loadGlobalBlocklistOf() =
        dao.getGlobalBlocklistedPackages()

    suspend fun addToGlobalBlocklist(packageName: String) {
        dao.insert(
            Blocklist(
                id = 0,
                packageName = packageName,
                blocklistId = PACKAGES_LIST_GLOBAL_ID
            )
        )
    }

    suspend fun updateGlobalBlocklist(packages: Set<String>) {
        dao.deleteById(PACKAGES_LIST_GLOBAL_ID)
        dao.insert(
            *packages.map { packageName ->
                Blocklist(
                    id = 0,
                    packageName = packageName,
                    blocklistId = PACKAGES_LIST_GLOBAL_ID
                )
            }.toTypedArray()
        )
    }
}