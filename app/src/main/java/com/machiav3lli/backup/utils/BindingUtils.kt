/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.machiav3lli.backup.*
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.AppMetaInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun filterToId(mode: Int): Int {
    return when (mode) {
        SCHED_FILTER_USER -> R.id.schedUser
        SCHED_FILTER_SYSTEM -> R.id.schedSystem
        SCHED_FILTER_NEW_UPDATED -> R.id.schedNewUpdated
        SCHED_FILTER_LAUNCHABLE -> R.id.schedLaunchable
        else -> R.id.schedAll
    }
}

fun idToFilter(mode: Int): Int {
    return when (mode) {
        R.id.schedUser -> SCHED_FILTER_USER
        R.id.schedSystem -> SCHED_FILTER_SYSTEM
        R.id.schedNewUpdated -> SCHED_FILTER_NEW_UPDATED
        R.id.schedLaunchable -> SCHED_FILTER_LAUNCHABLE
        else -> SCHED_FILTER_ALL
    }
}

fun modeToId(subMode: Int): Int {
    return when (subMode) {
        1 -> R.id.schedApk
        2 -> R.id.schedData
        else -> R.id.schedBoth
    }
}

fun idToMode(subMode: Int): Int {
    return when (subMode) {
        R.id.schedApk -> MODE_APK
        R.id.schedData -> MODE_DATA
        else -> MODE_BOTH
    }
}

@BindingAdapter("exists")
fun View.setExists(rightMode: Boolean) {
    visibility = if (rightMode) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

@BindingAdapter("visible")
fun View.setVisible(rightMode: Boolean) {
    visibility = if (rightMode) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

fun AppCompatImageView.setIcon(metaInfo: AppMetaInfo?) {
    if (metaInfo?.hasIcon() == true) {
        setImageDrawable(metaInfo.applicationIcon)
    } else {
        setImageResource(R.drawable.ic_placeholder)
    }
}

fun AppCompatImageView.setAppType(appInfo: AppInfo) {
    var color: ColorStateList
    when {
        appInfo.isSpecial -> {
            color = ColorStateList.valueOf(COLOR_SPECIAL)
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_special)
        }
        appInfo.isSystem -> {
            color = ColorStateList.valueOf(COLOR_SYSTEM)
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_system)
        }
        else -> {
            color = ColorStateList.valueOf(COLOR_USER)
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_user)
        }
    }
    if (!appInfo.isSpecial) {
        if (appInfo.isDisabled) {
            color = ColorStateList.valueOf(COLOR_DISABLED)
        }
        if (!appInfo.isInstalled) {
            color = ColorStateList.valueOf(COLOR_UNINSTALLED)
        }
    }
    imageTintList = color
}

fun Chip.setColor(theList: Set<String>) {
    when {
        theList.isNotEmpty() -> {
            this.setTextColor(this.context.getColor(R.color.app_accent))
            this.setChipStrokeColorResource(R.color.app_accent)
            this.setChipIconTintResource(R.color.app_accent)
            this.setRippleColorResource(R.color.app_accent)
        }
        else -> {
            this.setTextColor(this.context.getColor(R.color.app_secondary))
            this.setChipStrokeColorResource(R.color.app_secondary)
            this.setChipIconTintResource(R.color.app_secondary)
            this.setRippleColorResource(R.color.app_secondary)
        }
    }
}

fun getFormattedDate(lastUpdate: LocalDateTime?, withTime: Boolean): String? {
    lastUpdate?.let {
        val dtf = if (withTime) DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) else DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return lastUpdate.format(dtf)
    }
    return null
}
