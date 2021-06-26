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

import android.animation.Animator
import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.machiav3lli.backup.R
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.AppMetaInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@BindingAdapter("exists")
fun View.setExists(rightMode: Boolean) {
    visibility = when {
        rightMode -> View.VISIBLE
        else -> View.GONE
    }
}

@BindingAdapter("visible")
fun View.setVisible(rightMode: Boolean) {
    visibility = when {
        rightMode -> View.VISIBLE
        else -> View.INVISIBLE
    }
}

fun View.changeVisibility(nVisibility: Int, withAnimation: Boolean) =
    animate().alpha(if (nVisibility == View.VISIBLE) 1.0f else 0.0f)
        .setDuration(if (withAnimation) 600 else 1.toLong())
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                if (nVisibility == View.VISIBLE && visibility == View.GONE)
                    visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                visibility = nVisibility
            }

            override fun onAnimationCancel(animation: Animator) {
                // not relevant
            }

            override fun onAnimationRepeat(animation: Animator) {
                // not relevant
            }
        })

fun AppCompatImageView.setIcon(metaInfo: AppMetaInfo?) = when {
    metaInfo?.hasIcon() == true -> setImageDrawable(metaInfo.applicationIcon)
    else -> setImageResource(R.drawable.ic_placeholder)
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
    val color =
        ColorStateList.valueOf(
            if (theList.isNotEmpty()) context.colorAccent
            else context.colorSecondary
        )
    this.setTextColor(color)
    this.chipStrokeColor = color
    this.chipIconTint = color
    this.rippleColor = color
}

fun LocalDateTime.getFormattedDate(withTime: Boolean): String? {
    val dtf = if (withTime) DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
    else DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    return format(dtf)
}
