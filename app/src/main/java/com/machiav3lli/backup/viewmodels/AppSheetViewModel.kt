package com.machiav3lli.backup.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.fragments.AppSheet
import com.machiav3lli.backup.handler.NotificationHelper.showNotification
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.BackupItem
import com.machiav3lli.backup.utils.LogUtils.Companion.logErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSheetViewModel(val context: Context, app: AppInfo, var shellCommands: ShellCommands?, application: Application)
    : AndroidViewModel(application) {

    var appInfo = MediatorLiveData<AppInfo>()

    private var notificationId: Int

    val refreshNow = MutableLiveData<Boolean>()

    init {
        appInfo.value = app
        notificationId = System.currentTimeMillis().toInt()
    }

    fun uninstallApp() {
        viewModelScope.launch {
            uninstall()
            refreshNow.value = true
        }
    }

    private suspend fun uninstall() {
        withContext(Dispatchers.IO) {
            appInfo.value?.let {
                Log.i(AppSheet.TAG, "uninstalling " + appInfo.value?.packageLabel)
                try {
                    shellCommands?.uninstall(appInfo.value?.packageName, appInfo.value?.getApkPath(),
                            appInfo.value?.getDataPath(), appInfo.value?.isSystem == true)
                    showNotification(context, MainActivityX::class.java, notificationId++, appInfo.value?.packageLabel,
                            context.getString(com.machiav3lli.backup.R.string.uninstallSuccess), true)
                    it.packageInfo = null
                } catch (e: ShellCommands.ShellActionFailedException) {
                    showNotification(context, MainActivityX::class.java, notificationId++, appInfo.value?.packageLabel,
                            context.getString(com.machiav3lli.backup.R.string.uninstallFailure), true)
                    logErrors(context, e.message)
                }
            }
        }
    }

    fun enableDisableApp(users: MutableList<String>, enable: Boolean) {
        viewModelScope.launch {
            enableDisable(users, enable)
            refreshNow.value = true
        }
    }

    @Throws(ShellCommands.ShellActionFailedException::class)
    private suspend fun enableDisable(users: MutableList<String>, enable: Boolean) {
        withContext(Dispatchers.IO) {
            shellCommands!!.enableDisablePackage(appInfo.value?.packageName, users, enable)
        }
    }

    fun getUsers(): Array<String> {
        return shellCommands?.getUsers()?.toTypedArray() ?: arrayOf()
    }

    fun deleteBackup(backup: BackupItem) {
        viewModelScope.launch {
            delete(backup)
            refreshNow.value = true
        }
    }

    private suspend fun delete(backup: BackupItem) {
        withContext(Dispatchers.IO) {
            appInfo.value?.let {
                if (it.backupHistory.size > 1) {
                    it.delete(context, backup)
                } else {
                    it.deleteAllBackups(context)
                }
            }
        }
    }

    fun deleteAllBackups() {
        viewModelScope.launch {
            deleteAll()
            refreshNow.value = true
        }
    }

    private suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            appInfo.value?.deleteAllBackups(context)
        }
    }
}