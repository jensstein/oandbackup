package com.machiav3lli.backup.items;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;

import java.util.Arrays;
import java.util.List;

/**
 * This class is used to describe special backup files that use a hardcoded list of file paths
 */
public class SpecialAppMetaInfo extends AppMetaInfo implements Parcelable {

    @SerializedName("specialFiles")
    String[] files;

    public SpecialAppMetaInfo(String packageName, String label, String versionName, int versionCode, String[] fileList) {
        super(packageName, label, versionName, versionCode, 0, null, null, true);
        this.files = fileList;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public String[] getFileList() {
        return this.files;
    }

    protected SpecialAppMetaInfo(Parcel in) {
        super(in);
        this.files = new String[in.readInt()];
        in.readStringArray(this.files);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.files.length);
        dest.writeStringArray(this.files);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SpecialAppMetaInfo> CREATOR = new Creator<SpecialAppMetaInfo>() {
        @Override
        public SpecialAppMetaInfo createFromParcel(Parcel in) {
            return new SpecialAppMetaInfo(in);
        }

        @Override
        public SpecialAppMetaInfo[] newArray(int size) {
            return new SpecialAppMetaInfo[size];
        }
    };


    /**
     * Returns the list of special (virtual) packages
     * @param context Context object
     * @return a list of of virtual packages
     * @throws FileUtils.BackupLocationInAccessibleException when the backup location cannot be read for any reason
     * @throws PrefUtils.StorageLocationNotConfiguredException when the backup location is not set in the configuration
     */
    public static List<AppInfoV2> getSpecialPackages(Context context) throws FileUtils.BackupLocationInAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        final int userId = ShellCommands.getCurrentUser();
        final String userDir = "/data/system/users/" + userId;
        // Careful: It is possible to specify whole directories, but there are two rules:
        // 1. Directories must end with a slash e.g. "/data/system/netstats/"
        // 2. The name of the directory must be unique:
        //      when "/data/system/netstats/" is added, "/my/netstats/" may not be added
        //      As result the backup procedure would put the contents of both directories into
        //      the same directory in the archive and the restore would do the same but in reverse.
        // Documentation note: This could be outdated, make sure the logic in BackupSpecialAction and
        // RestoreSpecialAction hasn't changed!
        AppInfoV2[] result = {
                new AppInfoV2(context, new SpecialAppMetaInfo(
                        "special.accounts",
                        context.getString(R.string.spec_accounts),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        new String[]{
                                "/data/system_ce/" + userId + "/accounts_ce.db"
                        })),
                new AppInfoV2(context, new SpecialAppMetaInfo(
                        "special.appwidgets",
                        context.getString(R.string.spec_appwidgets),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        new String[]{
                                userDir + "/appwidgets.xml"
                        })),
                new AppInfoV2(context, new SpecialAppMetaInfo(
                        "special.bluetooth",
                        context.getString(R.string.spec_bluetooth),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        new String[]{
                                "/data/misc/bluedroid/"
                        })),
                new AppInfoV2(context, new SpecialAppMetaInfo(
                        "special.data.usage.policy",
                        context.getString(R.string.spec_data),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        new String[]{
                                "/data/system/netpolicy.xml",
                                "/data/system/netstats/"
                        })),
                new AppInfoV2(context, new SpecialAppMetaInfo(
                        "special.wallpaper",
                        context.getString(R.string.spec_wallpaper),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        new String[]{
                                userDir + "/wallpaper",
                                userDir + "/wallpaper_info.xml"
                        })),
                new AppInfoV2(context, new SpecialAppMetaInfo(
                        "special.wifi.access.points",
                        context.getString(R.string.spec_wifiAccessPoints),
                        Build.VERSION.RELEASE,
                        Build.VERSION.SDK_INT,
                        new String[]{
                                "/data/misc/wifi/WifiConfigStore.xml",
                                "/data/misc/wifi/WifiConfigStore.xml.encrypted-checksum"
                        })),
        };
        return Arrays.asList(result);
    }
}
