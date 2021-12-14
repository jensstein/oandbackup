package tests

import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.handler.ignoredPackages
import org.junit.Assert
import org.junit.Test


class Test_ignoredPackages {

    @Test
    fun test_failsOnOthers() {

        for (packageName in
            """
            com.google.android.gm
            """.trimIndent().split("""\n+""".toRegex()).map { it.trim() }
        ) {
            Assert.assertFalse(
                "wrong match: $packageName",
                packageName.matches(ignoredPackages)
            )
        }
    }

    @Test
    fun test_matchesOwnPackage() {
        val packageName = BuildConfig.APPLICATION_ID
        Assert.assertTrue(
            "does not match: $packageName",
            packageName.matches(ignoredPackages)
        )
    }

    @Test
    fun test_matchesOnIgnoredPackages() {

        for (packageName in
        """
            com.android.externalstorage
            com.android.providers.downloads.ui
            android
            com.android.providers.media
            com.android.providers.media.module
            com.android.mtp
            com.google.android.gms
            com.google.android.gsf
            com.android.shell
            com.android.systemui
            """.trimIndent().split("""\n+""".toRegex()).map { it.trim() }
        ) {
            Assert.assertTrue(
                "does not match: $packageName",
                packageName.matches(ignoredPackages)
            )
        }
    }

}