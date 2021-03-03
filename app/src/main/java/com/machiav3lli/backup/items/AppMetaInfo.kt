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
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.File

open class AppMetaInfo : Parcelable {
    @SerializedName("packageName")
    @Expose
    var packageName: String? = null
        private set

    @SerializedName("packageLabel")
    @Expose
    var packageLabel: String? = null
        private set

    @SerializedName("versionName")
    @Expose
    var versionName: String? = null
        private set

    @SerializedName("versionCode")
    @Expose
    var versionCode = 0
        private set

    @SerializedName("profileId")
    @Expose
    var profileId = 0
        private set

    @SerializedName("sourceDir")
    @Expose
    var sourceDir: String? = null
        private set

    @SerializedName("splitSourceDirs")
    @Expose
    var splitSourceDirs: Array<String> = arrayOf()
        private set

    @SerializedName("isSystem")
    @Expose
    var isSystem = false
        private set

    @SerializedName("icon")
    @Expose
    var applicationIcon: Drawable? = null

    constructor()

    constructor(context: Context, pi: PackageInfo) {
        this.packageName = pi.packageName
        this.packageLabel = pi.applicationInfo.loadLabel(context.packageManager).toString()
        this.versionName = pi.versionName
        this.versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt()
        else pi.versionCode
        // Don't have access to UserManager service; using a cheap workaround to figure out
        // who is running by parsing it from the data path: /data/user/0/org.example.app
        try {
            this.profileId = File(pi.applicationInfo.dataDir).parentFile?.name?.toInt() ?: -1
        } catch (e: NumberFormatException) {
            // Android System "App" points to /data/system
            this.profileId = -1
        }
        this.sourceDir = pi.applicationInfo.sourceDir
        this.splitSourceDirs = pi.applicationInfo.splitSourceDirs ?: arrayOf()
        // Boolean arithmetic to check if FLAG_SYSTEM is set
        this.isSystem = pi.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
        this.applicationIcon = context.packageManager.getApplicationIcon(pi.applicationInfo)
    }

    constructor(packageName: String?, packageLabel: String?, versionName: String?, versionCode: Int,
                profileId: Int, sourceDir: String?, splitSourceDirs: Array<String>, isSystem: Boolean) {
        this.packageName = packageName
        this.packageLabel = packageLabel
        this.versionName = versionName
        this.versionCode = versionCode
        this.profileId = profileId
        this.sourceDir = sourceDir
        this.splitSourceDirs = splitSourceDirs
        this.isSystem = isSystem
    }

    protected constructor(source: Parcel) {
        this.packageName = source.readString()
        this.packageLabel = source.readString()
        this.versionName = source.readString()
        this.versionCode = source.readInt()
        this.profileId = source.readInt()
        this.sourceDir = source.readString()
        this.splitSourceDirs = source.createStringArray() ?: arrayOf()
        this.isSystem = source.readByte().toInt() != 0
    }

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