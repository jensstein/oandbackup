package tests.tests

import com.machiav3lli.backup.manager.actions.BaseAppAction.Companion.doNotStop
import com.machiav3lli.backup.manager.actions.BaseAppAction.Companion.ignoredPackages
import com.machiav3lli.backup.utils.SystemUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class Test_ignoredPackages {

    @Test
    fun test_ignoredPackages_doesNotMatchSimilar() {

        for (packageName in
            """
            com.android.externalstorage.foobar
            com.android.providers.downloads.foobar
            android.foobar
            foobar.android
            com.android.mtp.foobar
            com.google.android.gm
            com.google.android.gms.foobar
            com.google.android.gmsfoobar
            com.google.android.gsffoobar
            com.android.shellfoobar
            com.android.systemui.foobar
            """.trimIndent().split("\n").map { it.trim() }
        ) {
            assertFalse(
                "wrong match: $packageName",
                packageName.matches(ignoredPackages)
            )
            assertFalse(
                "wrong match (contains): $packageName",
                packageName.contains(ignoredPackages)
            )
        }
    }

    @Test
    fun test_doNotStop_matchesOwnPackage() {
        val packageName = SystemUtils.packageName  // use explicit BuildConfig
        assertTrue(
            "does not match: $packageName",
            packageName.matches(doNotStop)
        )
        assertTrue(
            "does not match (contains): $packageName",
            packageName.contains(doNotStop)
        )
    }

    @Test
    fun test_ignoredPackages_matches() {

        for (packageName in
        """
            com.android.externalstorage
            com.android.providers.downloads.ui
            android
            com.android.providers.media
            com.android.providers.media.module
            com.android.providers.media.foobar
            com.android.mtp
            com.google.android.gms
            com.google.android.gsf
            com.android.shell
            com.android.systemui
            """.trimIndent().split("\n").map { it.trim() }
        ) {
            assertTrue(
                "does not match: $packageName",
                packageName.matches(ignoredPackages)
            )
            assertTrue(
                "does not match (contains): $packageName",
                packageName.contains(ignoredPackages)
            )
        }
    }

}