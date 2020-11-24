package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.items.AppInfo
import com.topjohnwu.superuser.internal.Utils.context

class AppSheetViewModelFactory(private val app: AppInfo, private val shellCommands: ShellCommands?, private val application: Application)
    : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppSheetViewModel::class.java)) {
            return AppSheetViewModel(context, app, shellCommands, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}