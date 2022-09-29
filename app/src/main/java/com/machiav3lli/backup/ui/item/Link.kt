package com.machiav3lli.backup.ui.item

import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.HELP_CHANGELOG
import com.machiav3lli.backup.HELP_FAQ
import com.machiav3lli.backup.HELP_ISSUES
import com.machiav3lli.backup.HELP_LICENSE
import com.machiav3lli.backup.HELP_MATRIX
import com.machiav3lli.backup.HELP_TELEGRAM
import com.machiav3lli.backup.R
import com.machiav3lli.backup.ui.compose.icons.Icon
import com.machiav3lli.backup.ui.compose.icons.icon.IcChangelog
import com.machiav3lli.backup.ui.compose.icons.icon.IcElement
import com.machiav3lli.backup.ui.compose.icons.icon.IcFaq
import com.machiav3lli.backup.ui.compose.icons.icon.IcInfo
import com.machiav3lli.backup.ui.compose.icons.icon.IcIssue
import com.machiav3lli.backup.ui.compose.icons.icon.IcTelegram

data class Link(
    val nameId: Int,
    val icon: ImageVector,
    val iconColorId: Int,
    val uri: String,
) {

    companion object {
        val Changelog = Link(
            R.string.help_changelog,
            Icon.IcChangelog,
            R.color.ic_updated,
            HELP_CHANGELOG
        )
        val Telegram = Link(
            R.string.help_group_telegram,
            Icon.IcTelegram,
            R.color.ic_system,
            HELP_TELEGRAM
        )
        val Matrix = Link(
            R.string.help_group_matrix,
            Icon.IcElement,
            R.color.ic_apk,
            HELP_MATRIX
        )
        val License = Link(
            R.string.help_license,
            Icon.IcInfo,
            R.color.ic_ext_data,
            HELP_LICENSE
        )
        val Issues = Link(
            R.string.help_issues,
            Icon.IcIssue,
            R.color.ic_de_data,
            HELP_ISSUES
        )
        val FAQ = Link(
            R.string.help_faq,
            Icon.IcFaq,
            R.color.ic_special,
            HELP_FAQ
        )
    }
}