package com.machiav3lli.backup.ui.item

import com.machiav3lli.backup.HELP_CHANGELOG
import com.machiav3lli.backup.HELP_ELEMENT
import com.machiav3lli.backup.HELP_FAQ
import com.machiav3lli.backup.HELP_ISSUES
import com.machiav3lli.backup.HELP_LICENSE
import com.machiav3lli.backup.HELP_TELEGRAM
import com.machiav3lli.backup.R

data class Link(
    val nameId: Int,
    val iconId: Int,
    val iconColorId: Int,
    val uri: String,
) {

    companion object {
        val Changelog = Link(
            R.string.help_changelog,
            R.drawable.ic_changelog,
            R.color.ic_updated,
            HELP_CHANGELOG
        )
        val Telegram = Link(
            R.string.help_group_telegram,
            R.drawable.ic_telegram,
            R.color.ic_system,
            HELP_TELEGRAM
        )
        val Matrix = Link(
            R.string.help_group_matrix,
            R.drawable.ic_element,
            R.color.ic_apk,
            HELP_ELEMENT
        )
        val License = Link(
            R.string.help_license,
            R.drawable.ic_info,
            R.color.ic_ext_data,
            HELP_LICENSE
        )
        val Issues = Link(
            R.string.help_issues,
            R.drawable.ic_issue,
            R.color.ic_de_data,
            HELP_ISSUES
        )
        val FAQ = Link(
            R.string.help_faq,
            R.drawable.ic_faq,
            R.color.ic_special,
            HELP_FAQ
        )
    }
}