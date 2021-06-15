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

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

/**
 * Backup item for either a newly created backup or the original item of the restored backup.
 * Can be null, if succeeded is set to false
 */
class ActionResult(
    val app: AppInfo?,
    val backupProperties: BackupProperties?,
    val message: String,
    val succeeded: Boolean
) {
    private val occurrence: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "${timeFormat.format(occurrence)}: ${app ?: "NoApp"}${if (message.isEmpty()) "" else " $message"}"
    }

    companion object {
        val timeFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.ENGLISH)
    }
}