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

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.Package
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppSheetViewModel(
    app: Package,
    var shellCommands: ShellCommands,
    private val appContext: Application
) : AndroidViewModel(appContext) {

    var appInfo by mutableStateOf(app) //MediatorLiveData<Package>()

    private var notificationId: Int

    var refreshNow by mutableStateOf(false)
    var snackbarText by mutableStateOf("")

    init {
        notificationId = System.currentTimeMillis().toInt()
        //refreshNow.observeForever {
        //    appInfo.value?.let {
        //        invalidateCacheForPackage(it.packageName)
        //    }
        //}
    }

    fun uninstallApp() {
        viewModelScope.launch {
            uninstall()
            refreshNow = true
        }
    }

    private suspend fun uninstall() {
        withContext(Dispatchers.IO) {
            appInfo.let {
                Timber.i("uninstalling: ${appInfo.packageLabel}")
                try {
                    shellCommands.uninstall(
                        appInfo.packageName, appInfo.apkPath,
                        appInfo.dataPath, appInfo.isSystem == true
                    )
                    showNotification(
                        appContext,
                        MainActivityX::class.java,
                        notificationId++,
                        appInfo.packageLabel,
                        appContext.getString(com.machiav3lli.backup.R.string.uninstallSuccess),
                        true
                    )
                } catch (e: ShellCommands.ShellActionFailedException) {
                    showNotification(
                        appContext,
                        MainActivityX::class.java,
                        notificationId++,
                        appInfo.packageLabel,
                        appContext.getString(com.machiav3lli.backup.R.string.uninstallFailure),
                        true
                    )
                    e.message?.let { message -> LogsHandler.logErrors(appContext, message) }
                }
            }
        }
    }

    fun enableDisableApp(users: MutableList<String>, enable: Boolean) {
        viewModelScope.launch {
            enableDisable(users, enable)
            refreshNow = true
        }
    }

    @Throws(ShellCommands.ShellActionFailedException::class)
    private suspend fun enableDisable(users: MutableList<String>, enable: Boolean) {
        withContext(Dispatchers.IO) {
            shellCommands.enableDisablePackage(appInfo.packageName, users, enable)
        }
    }

    fun getUsers(): Array<String> {
        return shellCommands.getUsers()?.toTypedArray() ?: arrayOf()
    }

    fun deleteBackup(backup: Backup) {
        viewModelScope.launch {
            delete(backup)
            refreshNow = true
        }
    }

    private suspend fun delete(backup: Backup) {
        withContext(Dispatchers.IO) {
            appInfo.deleteBackup(backup)
        }
    }

    fun deleteAllBackups() {
        viewModelScope.launch {
            deleteAll()
            refreshNow = true
        }
    }

    private suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            appInfo.deleteAllBackups()
        }
    }

    class Factory(
        private val app: Package,
        private val shellCommands: ShellCommands,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppSheetViewModel::class.java)) {
                return AppSheetViewModel(app, shellCommands, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}