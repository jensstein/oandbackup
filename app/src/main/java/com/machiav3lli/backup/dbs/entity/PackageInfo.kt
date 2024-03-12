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
import android.os.Build
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity
open class PackageInfo(
    @PrimaryKey
    var packageName: String,
    var packageLabel: String = "",
    @ColumnInfo(defaultValue = "-")
    var versionName: String? = "-",
    var versionCode: Int = 0,
    var profileId: Int = 0,
    var sourceDir: String? = null,
    var splitSourceDirs: Array<String> = arrayOf(),
    var isSystem: Boolean = false,
    var icon: Int = -1,
) {
    open val isSpecial: Boolean
        get() = false

    constructor(context: Context, pi: android.content.pm.PackageInfo) : this(
        packageName = pi.packageName,
        packageLabel = pi.applicationInfo.loadLabel(context.packageManager).toString(),
        versionName = pi.versionName ?: "",
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt()
        else pi.versionCode,
        profileId = try {
            File(pi.applicationInfo.dataDir).parentFile?.name?.toInt() ?: -1
        } catch (e: NumberFormatException) {
            -1 // Android System "App" points to /data/system
        },
        sourceDir = pi.applicationInfo.sourceDir,
        splitSourceDirs = pi.applicationInfo.splitSourceDirs ?: arrayOf(),
        isSystem = pi.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM == android.content.pm.ApplicationInfo.FLAG_SYSTEM,
        icon = pi.applicationInfo.icon
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val pkg = other as PackageInfo
        return packageName == pkg.packageName
                && this.packageLabel == pkg.packageLabel
                && this.versionName == pkg.versionName
                && this.versionCode == pkg.versionCode
                && this.profileId == pkg.profileId
                //&& this.sourceDir == pkg.sourceDir
                //&& this.splitSourceDirs == pkg.splitSourceDirs
                && this.isSystem == pkg.isSystem
                && this.icon == pkg.icon
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + packageName.hashCode()
        hash = 31 * hash + packageLabel.hashCode()
        hash = 31 * hash + versionName.hashCode()
        hash = 31 * hash + versionCode.hashCode()
        hash = 31 * hash + profileId.hashCode()
        hash = 31 * hash + isSystem.hashCode()
        hash = 31 * hash + icon.hashCode()
        return hash
    }
}