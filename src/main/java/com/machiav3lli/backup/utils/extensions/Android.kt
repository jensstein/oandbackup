package com.machiav3lli.backup.utils.extensions

import android.os.Build

object Android {
    val sdk: Int
        get() = Build.VERSION.SDK_INT

    val name: String
        get() = Build.VERSION.RELEASE

    val deviceName: String
        get() = Build.DEVICE

    val platforms: Set<String>
        get() = Build.SUPPORTED_ABIS.toSet()

    val mainPlatform: String
        get() = Build.SUPPORTED_ABIS.firstOrNull() ?: Build.UNKNOWN

    val primaryPlatform: String
        get() = Build.SUPPORTED_64_BIT_ABIS?.firstOrNull()
            ?: Build.SUPPORTED_32_BIT_ABIS?.firstOrNull()
            ?: Build.UNKNOWN

    fun minSDK(min: Int): Boolean {
        return Build.VERSION.SDK_INT >= min
    }

    object PackageManager {
        // GET_SIGNATURES should always present for getPackageArchiveInfo
        val signaturesFlag: Int
            get() = (if (minSDK(Build.VERSION_CODES.P)) android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES else 0) or
                    @Suppress("DEPRECATION") android.content.pm.PackageManager.GET_SIGNATURES
    }
}