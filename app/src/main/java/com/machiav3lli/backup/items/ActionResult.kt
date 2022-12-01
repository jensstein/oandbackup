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

import com.machiav3lli.backup.ISO_DATE_TIME_FORMAT
import com.machiav3lli.backup.dbs.entity.Backup
import java.time.LocalDateTime

/**
 * Backup item for either a newly created backup or the original item of the restored backup.
 * Can be null, if succeeded is set to false
 */
class ActionResult(
    val app: Package?,
    val backup: Backup?,
    val message: String,
    val succeeded: Boolean
) {
    private val occurrence: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "${ISO_DATE_TIME_FORMAT.format(occurrence)}: ${app ?: "NoApp"}${if (message.isEmpty()) "" else " $message"}"
    }
}