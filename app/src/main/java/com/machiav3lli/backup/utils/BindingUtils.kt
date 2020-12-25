package com.machiav3lli.backup.utils

import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
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
        else -> R.id.schedAll
    }
}

fun idToFilter(mode: Int): Int {
    return when (mode) {
        R.id.schedUser -> SCHED_FILTER_USER
        R.id.schedSystem -> SCHED_FILTER_SYSTEM
        R.id.schedNewUpdated -> SCHED_FILTER_NEW_UPDATED
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
            setImageResource(R.drawable.ic_special_24)
        }
        appInfo.isSystem -> {
            color = ColorStateList.valueOf(COLOR_SYSTEM)
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_system_24)
        }
        else -> {
            color = ColorStateList.valueOf(COLOR_USER)
            visibility = View.VISIBLE
            setImageResource(R.drawable.ic_user_24)
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

fun getFormattedDate(lastUpdate: LocalDateTime?, withTime: Boolean): String? {
    lastUpdate?.let {
        val dtf = if (withTime) DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) else DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return lastUpdate.format(dtf)
    }
    return null
}
