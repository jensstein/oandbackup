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
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.beginBusy
import com.machiav3lli.backup.OABX.Companion.endBusy
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewModel(private val appContext: Application) : AndroidViewModel(appContext) {

    var logsList = mutableStateListOf<Log>()

    init {
        refreshList()
    }

    fun refreshList() {
        viewModelScope.launch {
            beginBusy("Log refreshList")
            logsList.apply {
                clear()
                addAll(recreateLogsList())
            }
            endBusy("Log refreshList")
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

    private suspend fun share(log: Log, asFile: Boolean = true) {
        withContext(Dispatchers.IO) {
            try {
                LogsHandler.getLogFile(log.logDate)?.let { log ->
                    val text = if (!asFile) log.readText() else ""
                    if (!asFile and text.isEmpty())
                        throw Exception("${log.name} is empty or cannot be read")
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/*"
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        putExtra(Intent.EXTRA_SUBJECT, "[NeoBackup] ${log.name}")
                        if (asFile)
                            putExtra(Intent.EXTRA_STREAM, log.uri)  // send as file
                        else
                            putExtra(Intent.EXTRA_TEXT, text)       // send as text
                    }
                    val shareIntent = Intent.createChooser(sendIntent, log.name)
                    OABX.activity?.startActivity(shareIntent)
                }
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e)
            }
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