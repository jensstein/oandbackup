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
package com.machiav3lli.backup.items

import android.content.Context
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.utils.GsonUtils.instance
import com.machiav3lli.backup.utils.openFileForReading
import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime

open class LogItem {
    @SerializedName("logDate")
    @Expose
    var logDate: LocalDateTime
        private set

    @SerializedName("deviceName")
    @Expose
    val deviceName: String?

    @SerializedName("sdkVersion")
    @Expose
    val sdkCodename: String?

    @SerializedName("cpuArch")
    @Expose
    val cpuArch: String?

    @SerializedName("logText")
    @Expose
    val logText: String?

    constructor(text: String, date: LocalDateTime) {
        this.logDate = date
        this.deviceName = android.os.Build.DEVICE
        this.sdkCodename = android.os.Build.VERSION.RELEASE
        this.cpuArch = android.os.Build.SUPPORTED_ABIS[0]
        this.logText = text
    }

    constructor(context: Context, logFile: StorageFile) {
        try {
            logFile.uri.openFileForReading(context).use { reader ->
                val item = fromGson(IOUtils.toString(reader))
                this.logDate = item.logDate
                this.deviceName = item.deviceName
                this.sdkCodename = item.sdkCodename
                this.cpuArch = item.cpuArch
                this.logText = item.logText
            }
        } catch (e: FileNotFoundException) {
            throw BackupItem.BrokenBackupException(
                "Cannot open ${logFile.name} at URI ${logFile.uri}",
                e
            )
        } catch (e: IOException) {
            throw BackupItem.BrokenBackupException(
                "Cannot read ${logFile.name} at URI ${logFile.uri}",
                e
            )
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, logFile.uri)
            throw BackupItem.BrokenBackupException("Unable to process ${logFile.name} at URI ${logFile.uri}. [${e.javaClass.canonicalName}] $e")
        }
    }

    fun toGson(): String {
        return instance!!.toJson(this)
    }

    override fun toString(): String {
        return "LogItem{" +
                "logDate=$logDate" +
                ", deviceName='$deviceName'" +
                ", sdkCodename='$sdkCodename'" +
                ", cpuArch='$cpuArch'" +
                ", logText:\n$logText" +
                '}'
    }

    fun delete(context: Context): Boolean? {
        val logFile = LogsHandler(context).getLogFile(this.logDate)
        return logFile?.delete()
    }

    companion object {
        fun fromGson(gson: String?): LogItem {
            return instance!!.fromJson(gson, LogItem::class.java)
        }
    }
}