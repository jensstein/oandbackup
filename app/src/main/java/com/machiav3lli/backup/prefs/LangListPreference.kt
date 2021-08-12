package com.machiav3lli.backup.prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.PREFS_LANGUAGES_DEFAULT
import com.machiav3lli.backup.R
import com.machiav3lli.backup.utils.getLocaleOfCode
import java.util.*

class LangListPreference : ListPreference {

    constructor(context: Context) : super(context) {
        loadLangs(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        loadLangs(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        loadLangs(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        loadLangs(context)
    }

    private fun loadLangs(context: Context) {
        setDefaultValue(PREFS_LANGUAGES_DEFAULT)

        val locales: MutableList<String> = ArrayList()
        val languagesRaw = BuildConfig.DETECTED_LOCALES.sorted()
        for (localeCode in languagesRaw) {
            val locale = context.getLocaleOfCode(localeCode)
            locales.add("${translateLocale(locale)};$localeCode")
        }

        val entries = arrayOfNulls<String>(locales.size + 1)
        val entryVals = arrayOfNulls<String>(locales.size + 1)
        locales.forEachIndexed { i, locale ->
            entries[i + 1] = locale.split(";")[0]
            entryVals[i + 1] = locale.split(";")[1]
        }
        entryVals[0] = PREFS_LANGUAGES_DEFAULT
        entries[0] = context.resources.getString(R.string.prefs_language_system)
        setEntries(entries)
        entryValues = entryVals
    }

    private fun translateLocale(locale: Locale): String {
        val country = locale.getDisplayCountry(locale)
        val language = locale.getDisplayLanguage(locale)
        return (language.replaceFirstChar { it.uppercase(Locale.getDefault()) }
                + (if (country.isNotEmpty() && country.compareTo(language, true) != 0)
            "($country)" else ""))
    }

    override fun getSummary(): CharSequence = translateLocale(context.getLocaleOfCode(value))
}