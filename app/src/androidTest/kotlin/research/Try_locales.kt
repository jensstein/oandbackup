package research

import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_LANGUAGES_SYSTEM
import com.machiav3lli.backup.utils.getLocaleOfCode
import org.junit.Test
import java.util.*

class Try_locales {

    @Test
    fun test_language() {

        val appConfig = OABX.context.resources.configuration

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = context.resources.configuration

        fun showLocales() {
            println("Locale.getDefault: ${Locale.getDefault()}")
            println("appLocale: ${appConfig.locales[0]}")
            println("locale: ${config.locales[0]}")
        }

        var sysLocale: Locale? = null

        fun setLanguage(language: String): Configuration {

            val config = context.resources.configuration

            println("----- setLanguage '$language' $config")

            if (sysLocale == null)
                sysLocale = config.locales[0]

            var setLocalCode = language
            if (setLocalCode == PREFS_LANGUAGES_SYSTEM) {
                setLocalCode = sysLocale?.language ?: Locale.US.language
            }

            val newLocale = context.getLocaleOfCode(setLocalCode)
            config.setLocale(newLocale)
            Locale.setDefault(newLocale)

            return config
        }

        showLocales()

        setLanguage("de_DE")

        showLocales()

        setLanguage(PREFS_LANGUAGES_SYSTEM)

        showLocales()

        setLanguage("en_US")

        showLocales()

        setLanguage("en_DE")

        showLocales()
    }
}