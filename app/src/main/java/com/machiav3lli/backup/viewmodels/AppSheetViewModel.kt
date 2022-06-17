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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
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
    private val database: ODatabase,
    private var shellCommands: ShellCommands,
    private val appContext: Application
) : AndroidViewModel(appContext) {

    var thePackage = MutableLiveData(app)
    var appExtras = MediatorLiveData<AppExtras>()

    var snackbarText = MutableLiveData("")

    private var notificationId: Int = System.currentTimeMillis().toInt()
    var refreshNow by mutableStateOf(false)

    init {
        appExtras.addSource(database.appExtrasDao.getLive(app.packageName)) {
            appExtras.value = it ?: AppExtras(app.packageName)
        }
    }

    fun uninstallApp() {
        viewModelScope.launch {
            uninstall()
            refreshNow = true
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
                    showNotification(
                        appContext,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
                        appContext.getString(com.machiav3lli.backup.R.string.uninstallSuccess),
                        true
                    )
                } catch (e: ShellCommands.ShellActionFailedException) {
                    showNotification(
                        appContext,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
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
            shellCommands.enableDisablePackage(thePackage.value?.packageName, users, enable)
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
            thePackage.value?.deleteBackup(backup)
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
            thePackage.value?.deleteAllBackups()
        }
    }

    fun setExtras(appExtras: AppExtras?) {
        viewModelScope.launch {
            replaceExtras(appExtras)
            refreshNow = true
        }
    }

    private suspend fun replaceExtras(appExtras: AppExtras?) {
        withContext(Dispatchers.IO) {
            if (appExtras != null)
                database.appExtrasDao.replaceInsert(appExtras)
            else
                thePackage.value?.let { database.appExtrasDao.deleteByPackageName(it.packageName) }
        }
    }

    class Factory(
        private val packageInfo: Package,
        private val database: ODatabase,
        private val shellCommands: ShellCommands,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppSheetViewModel::class.java)) {
                return AppSheetViewModel(packageInfo, database, shellCommands, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}