package com.machiav3lli.backup.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.topjohnwu.superuser.internal.Utils.context

class LogViewModelFactory(private val application: Application)
    : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogViewModel::class.java)) {
            return LogViewModel(context, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}