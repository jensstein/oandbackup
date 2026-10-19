package com.machiav3lli.backup.viewmodels

import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT_WITHOUT_SPECIAL
import com.machiav3lli.backup.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.backup.data.preferences.NeoPrefs
import com.machiav3lli.backup.data.repository.BlocklistRepository
import com.machiav3lli.backup.data.repository.PackageRepository
import com.machiav3lli.backup.manager.tasks.RefreshBackupsWorker
import com.machiav3lli.backup.utils.extensions.NeoViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivityVM(
    private val packageRepository: PackageRepository,
    private val blocklistRepository: BlocklistRepository,
    private val prefs: NeoPrefs,
) : NeoViewModel() {
    val blockList = blocklistRepository.getBlocklist()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptySet()
        )

    fun onEnableSpecials(enable: Boolean) {
        viewModelScope.launch {
            val filter = if (enable) MAIN_FILTER_DEFAULT
            else MAIN_FILTER_DEFAULT_WITHOUT_SPECIAL
            prefs.mainFilterHome.set(prefs.mainFilterHome.value and filter)
            prefs.mainFilterBackup.set(prefs.mainFilterBackup.value and filter)
            prefs.mainFilterRestore.set(prefs.mainFilterRestore.value and filter)
        }
    }

    fun updateBlocklist(packages: Set<String>) {
        viewModelScope.launch {
            blocklistRepository.updateGlobalBlocklist(packages)
        }
    }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            packageRepository.updatePackage(packageName)
        }
    }

    fun refreshBackups() {
        RefreshBackupsWorker.enqueueFullRefresh()
    }

    fun refreshPackageBackups(packageName: String) {
        RefreshBackupsWorker.enqueueSinglePackageRefresh(packageName)
    }

    fun cancelRefresh() {
        RefreshBackupsWorker.cancelAllRefresh()
    }
}