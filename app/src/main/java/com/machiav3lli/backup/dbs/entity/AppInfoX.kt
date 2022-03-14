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
package com.machiav3lli.backup.dbs.entity

import android.content.Context
import android.content.pm.PackageInfo
import androidx.room.Entity

@Entity
open class AppInfoX : PackageInfoX {
    var enabled: Boolean = true
    var installed: Boolean = false
    var apkDir: String? = ""
    var dataDir: String? = ""
    var deDataDir: String? = ""

    constructor(
        packageName: String,
        packageLabel: String?,
        versionName: String?,
        versionCode: Int,
        profileId: Int,
        sourceDir: String?,
        splitSourceDirs: Array<String> = arrayOf(),
        isSystem: Boolean
    ) : super(
        packageName,
        packageLabel,
        versionName,
        versionCode,
        profileId,
        sourceDir,
        splitSourceDirs,
        isSystem
    )

    constructor(context: Context, pi: PackageInfo) : super(context, pi) {
        this.installed = true
        this.enabled = pi.applicationInfo.enabled
        this.apkDir = pi.applicationInfo.sourceDir
        this.dataDir = pi.applicationInfo.dataDir
        this.deDataDir = pi.applicationInfo.deviceProtectedDataDir
    }
}