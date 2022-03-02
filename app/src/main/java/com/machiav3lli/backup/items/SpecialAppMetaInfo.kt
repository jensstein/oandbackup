package com.machiav3lli.backup.items

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.machiav3lli.backup.R
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.utils.FileUtils.BackupLocationInAccessibleException
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import timber.log.Timber

/**
 * This class is used to describe special backup files that use a hardcoded list of file paths
 */
open class SpecialAppMetaInfo : AppMetaInfo, Parcelable {
    var specialFiles: Array<String>

    constructor(
        packageName: String?,
        label: String?,
        versionName: String?,
        versionCode: Int,
        fileList: Array<String>
    )
            : super(packageName, label, versionName, versionCode, 0, null, arrayOf(), true) {
        this.specialFiles = fileList
    }

    override val isSpecial: Boolean
        get() = true

    protected constructor(source: Parcel) : super(source) {
        val expectedItems = source.readInt()
        val temporaryFileList: Array<String?> = arrayOfNulls(expectedItems)
        specialFiles = Array(expectedItems) { "" }
        source.readStringArray(temporaryFileList)
        temporaryFileList.forEachIndexed { index, value ->
            if (value != null) {
                specialFiles[index] = value
            } else {
                throw IllegalArgumentException("SpecialAppMetaInfo parcel contained a null value")
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(specialFiles.size)
        parcel.writeStringArray(specialFiles)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SpecialAppMetaInfo?> =
            object : Parcelable.Creator<SpecialAppMetaInfo?> {
                override fun createFromParcel(source: Parcel): SpecialAppMetaInfo? =
                    SpecialAppMetaInfo(source)

                override fun newArray(size: Int): Array<SpecialAppMetaInfo?> = arrayOfNulls(size)
            }
        private val specialPackages: MutableList<AppInfo> = mutableListOf()

        /**
         * Returns the list of special (virtual) packages
         *
         * @param context Context object
         * @return a list of of virtual packages
         * @throws BackupLocationInAccessibleException   when the backup location cannot be read for any reason
         * @throws StorageLocationNotConfiguredException when the backup location is not set in the configuration
         */
        var threadCount = 0
        var locked = false
        @Throws(
            BackupLocationInAccessibleException::class,
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
            if (locked) {
                synchronized(threadCount) {
                    threadCount++
                    Timber.d("################################################################### locked: $locked threads: $threadCount")
                }
            }
            synchronized(specialPackages) { // if n calls run in parallel we may have n duplicates
                                            // because there is some time between asking for the size and the first add
                locked = true
                if (specialPackages.size == 0) {
                    // caching this prevents recreating AppInfo-objects all the time and at wrong times
                    val userId = ShellCommands.currentUser
                    val miscDir = "/data/misc"
                    val systemDir = "/data/system"
                    val userDir = "$systemDir/users/$userId"
                    val systemCeDir = "/data/system_ce/$userId"
                    val vendorDeDir = "/data/vendor_de/$userId"
                    val specPrefix = "$ "

                    if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                        specialPackages
                            .add(
                                AppInfo(
                                    context, SpecialAppMetaInfo(
                                        "special.smsmms.json",
                                        specPrefix + context.getString(R.string.spec_smsmmsjson),
                                        Build.VERSION.RELEASE,
                                        Build.VERSION.SDK_INT, arrayOf(
                                            "${context.cacheDir.absolutePath}/special.smsmms.json.json"
                                        )
                                    )
                                )
                            )
                        specialPackages
                            .add(
                                AppInfo(
                                    context, SpecialAppMetaInfo(
                                        "special.calllogs.json",
                                        specPrefix + context.getString(R.string.spec_calllogsjson),
                                        Build.VERSION.RELEASE,
                                        Build.VERSION.SDK_INT, arrayOf(
                                            "${context.cacheDir.absolutePath}/special.calllogs.json.json"
                                        )
                                    )
                                )
                            )
                    }
                    specialPackages
                        .add(
                            AppInfo(
                                context, SpecialAppMetaInfo(
                                    "special.accounts",
                                    specPrefix + context.getString(R.string.spec_accounts),
                                    Build.VERSION.RELEASE,
                                    Build.VERSION.SDK_INT, arrayOf(
                                        "$systemCeDir/accounts_ce.db"
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
                                        "$miscDir/bluedroid/bt_config.conf"
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
                                        "$systemDir/netpolicy.xml",
                                        "$systemDir/netstats/"
                                    )
                                )
                            )
                        )
                    specialPackages
                        .add(
                            AppInfo(
                                context, SpecialAppMetaInfo(
                                    "special.fingerprint",
                                    specPrefix + context.getString(R.string.spec_fingerprint),
                                    Build.VERSION.RELEASE,
                                    Build.VERSION.SDK_INT, arrayOf(
                                        "$userDir/settings_fingerprint.xml",
                                        "$vendorDeDir/fpdata/"
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
                    // Location of the WifiConfigStore had been moved with Android R
                    val wifiConfigLocation = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        "$miscDir/wifi/WifiConfigStore.xml"
                    } else {
                        "$miscDir/apexdata/com.android.wifi/WifiConfigStore.xml"
                    }
                    specialPackages
                        .add(
                            AppInfo(
                                context, SpecialAppMetaInfo(
                                    "special.wifi.access.points",
                                    specPrefix + context.getString(R.string.spec_wifiAccessPoints),
                                    Build.VERSION.RELEASE,
                                    Build.VERSION.SDK_INT, arrayOf(
                                        wifiConfigLocation
                                    )
                                )
                            )
                        )
                }
                locked = false
            }
            return specialPackages
        }
    }
}