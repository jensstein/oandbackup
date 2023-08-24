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
package com.machiav3lli.backup.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.MutableComposableFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppSheetViewModel(
    app: Package?,
    private val database: ODatabase,
    private var shellCommands: ShellCommands,
) : AndroidViewModel(OABX.NB) {

    var thePackage = flow<Package?> { app }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        app
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    var appExtras = database.getAppExtrasDao().getFlow(app?.packageName).mapLatest {
        it ?: AppExtras(app?.packageName ?: "")
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppExtras(app?.packageName ?: "")
    )

    val snackbarText = MutableComposableFlow(
        "",
        viewModelScope,
        "snackBarText"
    )

    private var notificationId: Int = System.currentTimeMillis().toInt()
    val refreshNow = mutableStateOf(true)
    val dismissNow = mutableStateOf(false)

    fun uninstallApp() {
        viewModelScope.launch {
            uninstall()
            refreshNow.value = true
        }
    }

    private suspend fun uninstall() {
        withContext(Dispatchers.IO) {
            thePackage.value?.let { mPackage ->
                Timber.i("uninstalling: ${mPackage.packageLabel}")
                try {
                    shellCommands.uninstall(
                        mPackage.packageName, mPackage.apkPath,
                        mPackage.dataPath, mPackage.isSystem
                    )
                    if (mPackage.backupList.isEmpty()) {
                        database.getAppInfoDao().deleteAllOf(mPackage.packageName)
                        dismissNow.value = true
                    }
                    showNotification(
                        OABX.NB,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
                        OABX.NB.getString(com.machiav3lli.backup.R.string.uninstallSuccess),
                        true
                    )
                } catch (e: ShellCommands.ShellActionFailedException) {
                    showNotification(
                        OABX.NB,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
                        OABX.NB.getString(com.machiav3lli.backup.R.string.uninstallFailure),
                        true
                    )
                    e.message?.let { message -> LogsHandler.logErrors(message) }
                }
            }
        }
    }

    fun enableDisableApp(users: List<String>, enable: Boolean) {
        viewModelScope.launch {
            enableDisable(users, enable)
            refreshNow.value = true
        }
    }

    @Throws(ShellCommands.ShellActionFailedException::class)
    private suspend fun enableDisable(users: List<String>, enable: Boolean) {
        withContext(Dispatchers.IO) {
            shellCommands.enableDisablePackage(thePackage.value?.packageName, users, enable)
        }
    }

    fun getUsers(): Array<String> {
        return shellCommands.getUsers().toTypedArray()
    }

    fun deleteBackup(backup: Backup) {              //TODO hg42 launchDeleteBackup ?
        viewModelScope.launch {
            delete(backup)
        }
    }

    private suspend fun delete(backup: Backup) {
        withContext(Dispatchers.IO) {
            thePackage.value?.let { pkg ->
                pkg.deleteBackup(backup)
                if (!pkg.isInstalled && pkg.backupList.isEmpty()) {
                    database.getAppInfoDao().deleteAllOf(pkg.packageName)
                    dismissNow.value = true
                }
            }
        }
    }

    fun deleteAllBackups() {
        viewModelScope.launch {
            deleteAll()
        }
    }

    private suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            thePackage.value?.let { pkg ->
                pkg.deleteAllBackups()
                if (!pkg.isInstalled && pkg.backupList.isEmpty()) {
                    database.getAppInfoDao().deleteAllOf(pkg.packageName)
                    dismissNow.value = true
                }
            }
        }
    }

    fun setExtras(appExtras: AppExtras?) {
        viewModelScope.launch {
            replaceExtras(appExtras)
            refreshNow.value = true
        }
    }

    private suspend fun replaceExtras(appExtras: AppExtras?) {
        withContext(Dispatchers.IO) {
            if (appExtras != null)
                database.getAppExtrasDao().replaceInsert(appExtras)
            else
                thePackage.value?.let { database.getAppExtrasDao().deleteByPackageName(it.packageName) }
        }
    }

    fun rewriteBackup(backup: Backup, changedBackup: Backup) {
        viewModelScope.launch {
            rewriteBackupSuspendable(backup, changedBackup)
        }
    }

    private suspend fun rewriteBackupSuspendable(backup: Backup, changedBackup: Backup) {
        withContext(Dispatchers.IO) {
            thePackage.value?.rewriteBackup(backup, changedBackup)
        }
    }

    class Factory(
        private val packageInfo: Package?,
        private val database: ODatabase,
        private val shellCommands: ShellCommands,
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppSheetViewModel::class.java)) {
                return AppSheetViewModel(packageInfo, database, shellCommands) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}