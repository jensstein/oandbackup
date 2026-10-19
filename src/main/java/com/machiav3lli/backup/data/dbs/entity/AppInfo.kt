/*
 * Neo Backup: open-source apps backup and restore app.
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
package com.machiav3lli.backup.data.dbs.entity

import android.content.Context
import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.backup.FIELD_IS_SYSTEM
import com.machiav3lli.backup.FIELD_PACKAGE_NAME
import com.machiav3lli.backup.utils.extensions.grantedPermissions

@Entity(
    indices = [
        Index(FIELD_PACKAGE_NAME, unique = true),
        Index(FIELD_IS_SYSTEM),
    ]
)
open class AppInfo : PackageInfo {
    var enabled: Boolean = true
    var installed: Boolean = false
    var apkDir: String? = ""
    var dataDir: String? = ""
    var deDataDir: String? = ""
    var permissions: List<String> = listOf()

    constructor(
        packageName: String,
        packageLabel: String,
        versionName: String?,
        versionCode: Int,
        profileId: Int,
        sourceDir: String?,
        splitSourceDirs: Array<String> = arrayOf(),
        isSystem: Boolean,
        permissions: List<String>
    ) : super(
        packageName,
        packageLabel,
        versionName,
        versionCode,
        profileId,
        sourceDir,
        splitSourceDirs,
        isSystem,
    ) {
        this.permissions = permissions
    }

    constructor(context: Context, pi: android.content.pm.PackageInfo) : super(context, pi) {
        this.installed = true
        this.enabled = pi.applicationInfo?.enabled ?: true
        this.apkDir = pi.applicationInfo?.sourceDir
        this.dataDir = pi.applicationInfo?.dataDir
        this.deDataDir = pi.applicationInfo?.deviceProtectedDataDir
        permissions = pi.grantedPermissions
    }
}