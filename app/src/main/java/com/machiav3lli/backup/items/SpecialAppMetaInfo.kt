package com.machiav3lli.backup.items

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException

/**
 * This class is used to describe special backup files that use a hardcoded list of file paths
 */
open class SpecialAppMetaInfo : AppMetaInfo, Parcelable {
    @SerializedName("specialFiles")
    var fileList: Array<String?>

    constructor(
        packageName: String?,
        label: String?,
        versionName: String?,
        versionCode: Int,
        fileList: Array<String?>
    )
            : super(packageName, label, versionName, versionCode, 0, null, arrayOf(), true) {
        this.fileList = fileList
    }

    override val isSpecial: Boolean
        get() = true

    protected constructor(source: Parcel) : super(source) {
        fileList = arrayOfNulls(source.readInt())
        source.readStringArray(fileList)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(fileList.size)
        parcel.writeStringArray(fileList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val CREATOR: Parcelable.Creator<SpecialAppMetaInfo?> =
            object : Parcelable.Creator<SpecialAppMetaInfo?> {
                override fun createFromParcel(source: Parcel): SpecialAppMetaInfo? {
                    return SpecialAppMetaInfo(source)
                }

                override fun newArray(size: Int): Array<SpecialAppMetaInfo?> {
                    return arrayOfNulls(size)
                }
            }
        private val specialPackages: MutableList<AppInfo> = mutableListOf()

        /**
         * Returns the list of special (virtual) packages
         *
         * @param context Context object
         * @return a list of of virtual packages
         * @throws BackupLocationIsAccessibleException   when the backup location cannot be read for any reason
         * @throws StorageLocationNotConfiguredException when the backup location is not set in the configuration
         */
        @Throws(
            BackupLocationIsAccessibleException::class,
            StorageLocationNotConfiguredException::class
        )
        fun getSpecialPackages(context: Context): List<AppInfo> {
            // Careful: It is possible to specify whole directories, but there are two rules:
            // 1. Directories must end with a slash e.g. "/data/system/netstats/"
            // 2. The name of the directory must be unique:
            //      when "/data/system/netstats/" is added, "/my/netstats/" may not be added
            //      As result the backup procedure would put the contents of both directories into
            //      the same directory in the archive and the restore would do the same but in reverse.
            // Documentation note: This could be outdated, make sure the logic in BackupSpecialAction and
            // RestoreSpecialAction hasn't changed!
            if (specialPackages.size == 0) {
                // caching this prevents recreating AppInfo-objects all the time and at wrong times
                val userId = ShellCommands.currentUser
                val userDir = "/data/system/users/$userId"
                val specPrefix = "$ "

                specialPackages
                    .add(
                        AppInfo(
                            context, SpecialAppMetaInfo(
                                "special.accounts",
                                specPrefix + context.getString(R.string.spec_accounts),
                                Build.VERSION.RELEASE,
                                Build.VERSION.SDK_INT, arrayOf(
                                    "/data/system_ce/$userId/accounts_ce.db"
                                )
                            )
                        )
                    )
                specialPackages
                    .add(
                        AppInfo(
                            context, SpecialAppMetaInfo(
                                "special.appwidgets",
                                specPrefix + context.getString(R.string.spec_appwidgets),
                                Build.VERSION.RELEASE,
                                Build.VERSION.SDK_INT, arrayOf(
                                    "$userDir/appwidgets.xml"
                                )
                            )
                        )
                    )
                specialPackages
                    .add(
                        AppInfo(
                            context, SpecialAppMetaInfo(
                                "special.bluetooth",
                                specPrefix + context.getString(R.string.spec_bluetooth),
                                Build.VERSION.RELEASE,
                                Build.VERSION.SDK_INT, arrayOf(
                                    "/data/misc/bluedroid/"
                                )
                            )
                        )
                    )
                specialPackages
                    .add(
                        AppInfo(
                            context, SpecialAppMetaInfo(
                                "special.data.usage.policy",
                                specPrefix + context.getString(R.string.spec_data),
                                Build.VERSION.RELEASE,
                                Build.VERSION.SDK_INT, arrayOf(
                                    "/data/system/netpolicy.xml",
                                    "/data/system/netstats/"
                                )
                            )
                        )
                    )
                specialPackages
                    .add(
                        AppInfo(
                            context, SpecialAppMetaInfo(
                                "special.wallpaper",
                                specPrefix + context.getString(R.string.spec_wallpaper),
                                Build.VERSION.RELEASE,
                                Build.VERSION.SDK_INT, arrayOf(
                                    "$userDir/wallpaper",
                                    "$userDir/wallpaper_info.xml"
                                )
                            )
                        )
                    )
                specialPackages
                    .add(
                        AppInfo(
                            context, SpecialAppMetaInfo(
                                "special.wifi.access.points",
                                specPrefix + context.getString(R.string.spec_wifiAccessPoints),
                                Build.VERSION.RELEASE,
                                Build.VERSION.SDK_INT, arrayOf(
                                    "/data/misc/wifi/WifiConfigStore.xml"
                                )
                            )
                        )
                    )
            }
            return specialPackages
        }
    }
}