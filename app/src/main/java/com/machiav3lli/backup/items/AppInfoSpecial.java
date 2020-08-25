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
package com.machiav3lli.backup.items;

import android.os.Parcel;
import android.os.Parcelable;

public class AppInfoSpecial extends AppInfo implements Parcelable {
    public static final Parcelable.Creator<AppInfoSpecial> CREATOR = new Parcelable.Creator<AppInfoSpecial>() {
        public AppInfoSpecial createFromParcel(Parcel in) {
            return new AppInfoSpecial(in);
        }

        public AppInfoSpecial[] newArray(int size) {
            return new AppInfoSpecial[size];
        }
    };
    String[] files;

    public AppInfoSpecial(String packageName, String label, String versionName, int versionCode) {
        super(packageName, label, versionName, versionCode, "", null, "", "", true, true);
    }

    protected AppInfoSpecial(Parcel in) {
        super(in);
        files = in.createStringArray();
    }

    @Override
    public String[] getFilesList() {
        return files;
    }

    public void setFilesList(String file) {
        files = new String[]{file};
    }

    public void setFilesList(String... files) {
        this.files = files;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeStringArray(files);
    }
}
