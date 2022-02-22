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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
open class AppMetaInfo(
    var packageName: String? = null,
    var packageLabel: String? = null,
    var versionName: String? = null,
    var versionCode: Int = 0,
    var profileId: Int = 0,
    var sourceDir: String? = null,
    var splitSourceDirs: Array<String> = arrayOf(),
    var isSystem: Boolean = false,
) : Parcelable {
    @Transient
    @Contextual
    var applicationIcon: Drawable? = null

    constructor(context: Context, pi: PackageInfo) : this(
        packageName = pi.packageName,
        packageLabel = pi.applicationInfo.loadLabel(context.packageManager).toString(),
        versionName = pi.versionName,
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt()
        else pi.versionCode,
        // Don't have access to UserManager service; using a cheap workaround to figure out
        // who is running by parsing it from the data path: /data/user/0/org.example.app
        profileId = try {
            File(pi.applicationInfo.dataDir).parentFile?.name?.toInt() ?: -1
        } catch (e: NumberFormatException) {
            // Android System "App" points to /data/system
            -1
        },
        sourceDir = pi.applicationInfo.sourceDir,
        splitSourceDirs = pi.applicationInfo.splitSourceDirs ?: arrayOf(),
        isSystem = pi.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
    ) {
        this.applicationIcon = context.packageManager.getApplicationIcon(pi.applicationInfo)
    }

    protected constructor(source: Parcel) : this(
        packageName = source.readString(),
        packageLabel = source.readString(),
        versionName = source.readString(),
        versionCode = source.readInt(),
        profileId = source.readInt(),
        sourceDir = source.readString(),
        splitSourceDirs = source.createStringArray() ?: arrayOf(),
        isSystem = source.readByte().toInt() != 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(packageLabel)
        parcel.writeString(versionName)
        parcel.writeInt(versionCode)
        parcel.writeInt(profileId)
        parcel.writeString(sourceDir)
        parcel.writeStringArray(splitSourceDirs)
        parcel.writeByte((if (isSystem) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    open val isSpecial: Boolean
        get() = false

    fun hasIcon(): Boolean {
        return applicationIcon != null
    }

    companion object {
        val CREATOR: Parcelable.Creator<AppMetaInfo?> = object : Parcelable.Creator<AppMetaInfo?> {
            override fun createFromParcel(source: Parcel): AppMetaInfo? {
                return AppMetaInfo(source)
            }

            override fun newArray(size: Int): Array<AppMetaInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}