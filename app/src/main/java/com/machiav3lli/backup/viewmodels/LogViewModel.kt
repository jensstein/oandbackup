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
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.LogsHandler.Companion.share
import com.machiav3lli.backup.items.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel(private val appContext: Application) : AndroidViewModel(appContext) {

    var logsList = mutableStateListOf<Log>()

    fun refreshList() {
        viewModelScope.launch {
            try {
                //beginBusy("Log refreshList")        // don't, it can be prevented by compose
                logsList.apply {
                    clear()
                    addAll(recreateLogsList())
                }
            } catch (e: Throwable) {
                LogsHandler.logException(e, backTrace = true)
            } finally {
                //endBusy("Log refreshList")        // don't, it can be prevented by compose
            }
        }
    }

    private suspend fun recreateLogsList(): MutableList<Log> = withContext(Dispatchers.IO) {
        LogsHandler.readLogs()
    }

    fun shareLog(log: Log, asFile: Boolean = true) {
        viewModelScope.launch {
            share(log, asFile)
        }
    }

    fun deleteLog(log: Log) {
        viewModelScope.launch {
            delete(log)
            logsList.remove(log)
            //refreshList()
        }
    }

    private suspend fun delete(log: Log) {
        withContext(Dispatchers.IO) {
            log.delete()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LogViewModel::class.java)) {
                return LogViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}