package com.machiav3lli.backup.utils

import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.Schedule.Mode
import com.machiav3lli.backup.dbs.Schedule.SubMode
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.items.AppMetaInfo
import java.time.LocalDateTime

fun modeToId(mode: Int): Int {
    return when (mode) {
        1 -> R.id.schedUser
        2 -> R.id.schedSystem
        3 -> R.id.schedNewUpdated
        else -> R.id.schedAll
    }
}

fun idToMode(mode: Int): Mode {
    return when (mode) {
        R.id.schedUser -> Mode.USER
        R.id.schedSystem -> Mode.SYSTEM
        R.id.schedNewUpdated -> Mode.NEW_UPDATED
        else -> Mode.ALL
    }
}

fun subModeToId(subMode: Int): Int {
    return when (subMode) {
        1 -> R.id.schedApk
        2 -> R.id.schedData
        else -> R.id.schedBoth
    }
}

fun idToSubMode(subMode: Int): SubMode {
    return when (subMode) {
        R.id.schedApk -> SubMode.APK
        R.id.schedData -> SubMode.DATA
        else -> SubMode.BOTH
    }
}