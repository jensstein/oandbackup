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

import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.utils.LocalDateTimeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime

@Serializable
open class Log {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable(with = LocalDateTimeSerializer::class)
    var logDate: LocalDateTime = LocalDateTime.parse("2020-01-01T00:00:00")
        private set

    var deviceName: String = ""

    var sdkCodename: String = ""

    var cpuArch: String = ""

    var logText: String = ""

    constructor(text: String, date: LocalDateTime) {
        this.logDate = date
        this.deviceName = android.os.Build.DEVICE
        this.sdkCodename = android.os.Build.VERSION.RELEASE
        this.cpuArch = android.os.Build.SUPPORTED_ABIS[0]
        this.logText = text
    }

    constructor(logFile: StorageFile) {
        try {
            logFile.inputStream()!!.use { inputStream ->
                val text = inputStream.reader().readText()
                //initFromSerialized(text) ||
                initFromText(text) ||
                        throw Backup.BrokenBackupException(
                            "$logFile is neither ${OABX.propsSerializer.javaClass.simpleName} nor text header format"
                        )
            }
        } catch (e: FileNotFoundException) {
            throw Backup.BrokenBackupException(
                "Cannot open $logFile",
                e
            )
        } catch (e: IOException) {
            throw Backup.BrokenBackupException(
                "Cannot read $logFile",
                e
            )
        } catch (e: Throwable) {
            LogsHandler.unexpectedException(e, logFile)
            throw Backup.BrokenBackupException("Unable to process $logFile. (${e.javaClass.canonicalName}) $e")
        }
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

    fun delete(): Boolean? {
        val logFile = LogsHandler.getLogFile(this.logDate)
        return logFile?.delete()
    }

    fun initFromText(text: String): Boolean {
        return try {
            var valid = false
            val lines = text.lines().toMutableList()
            while (lines.isNotEmpty()) {
                val line = lines.removeAt(0)
                if (line.isBlank()) {
                    this.logText = lines.joinToString("\n")
                    lines.clear()
                } else {
                    try {
                        val (field, value) = line.split(Regex(""":\s*"""), limit = 2)
                        when (field) {
                            "logDate"     -> {  // minimum data we need
                                this.logDate = LocalDateTime.parse(value)
                                valid = true
                            }
                            "deviceName"  -> {
                                this.deviceName = value
                            }
                            "sdkCodename" -> {
                                this.sdkCodename = value
                            }
                            "cpuArch"     -> {
                                this.cpuArch = value
                            }
                        }
                    } catch (e: Throwable) {
                        if (valid) {
                            // be tolerant, first non-header line, read remaining lines as text
                            this.logText = lines.joinToString("\n")
                            lines.clear()
                        }
                    }
                }
            }
            valid
        } catch (e: Throwable) {
            LogsHandler.unexpectedException(e)
            false
        }
    }

    fun toSerialized() = """
        logDate: $logDate
        deviceName: $deviceName
        sdkCodename: $sdkCodename
        cpuArch: $cpuArch
    """.trimIndent() + "\n\n" + logText

    //fun initFromSerialized(text: String): Boolean {
    //    return fromSerialized(text)?.let { item ->
    //        this.logDate = item.logDate
    //        this.deviceName = item.deviceName
    //        this.sdkCodename = item.sdkCodename
    //        this.cpuArch = item.cpuArch
    //        this.logText = item.logText
    //        true
    //    } ?: false
    //}
    //
    //fun toSerialized() = OABX.propsSerializer.encodeToString(this)
    //
    //companion object {
    //    fun fromSerialized(serialized: String) = runCatching { OABX.propsSerializer.decodeFromString<Log>(serialized) }.getOrNull()
    //}
}