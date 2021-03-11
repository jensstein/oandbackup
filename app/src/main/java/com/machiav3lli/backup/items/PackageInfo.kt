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

import android.content.pm.PackageInfo
import android.os.Parcel
import android.os.Parcelable

open class PackageInfo : Parcelable {
    var enabled: Boolean
    var apkDir: String
    var dataDir: String
    var deDataDir: String

    constructor(packageInfo: PackageInfo) {
        this.enabled = packageInfo.applicationInfo.enabled
        this.apkDir = packageInfo.applicationInfo.sourceDir
        this.dataDir = packageInfo.applicationInfo.dataDir
        this.deDataDir = packageInfo.applicationInfo.deviceProtectedDataDir
    }

    constructor(parcel: Parcel) {
        this.enabled = parcel.readByte() != 0.toByte()
        this.apkDir = parcel.readString().toString()
        this.dataDir = parcel.readString().toString()
        this.deDataDir = parcel.readString().toString()
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + enabled.hashCode()
        hash = 31 * hash + apkDir.hashCode()
        hash = 31 * hash + dataDir.hashCode()
        hash = 31 * hash + deDataDir.hashCode()
        return hash
    }

    override fun toString(): String {
        return "{$enabled, $apkDir, $dataDir, $deDataDir}"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (enabled) 1 else 0)
        parcel.writeString(apkDir)
        parcel.writeString(dataDir)
        parcel.writeString(deDataDir)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is com.machiav3lli.backup.items.PackageInfo) return false
        if (enabled != other.enabled) return false
        if (apkDir != other.apkDir) return false
        if (dataDir != other.dataDir) return false
        if (deDataDir != other.deDataDir) return false
        return true
    }

    companion object CREATOR : Parcelable.Creator<com.machiav3lli.backup.items.PackageInfo> {
        override fun createFromParcel(parcel: Parcel): com.machiav3lli.backup.items.PackageInfo {
            return PackageInfo(parcel)
        }

        override fun newArray(size: Int): Array<com.machiav3lli.backup.items.PackageInfo?> {
            return arrayOfNulls(size)
        }
    }
}