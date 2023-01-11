package com.machiav3lli.backup.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Phosphor

val Phosphor.`Align-center-vertical`: ImageVector
    get() {
        if (`_align-center-vertical` != null) {
            return `_align-center-vertical`!!
        }
        `_align-center-vertical` = Builder(
            name = "Align-center-vertical", defaultWidth = 256.0.dp,
            defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(224.0f, 120.0f)
                lineTo(208.0f, 120.0f)
                lineTo(208.0f, 72.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(152.0f, 56.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, 16.0f)
                verticalLineToRelative(48.0f)
                lineTo(120.0f, 120.0f)
                lineTo(120.0f, 48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(64.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 48.0f, 48.0f)
                verticalLineToRelative(72.0f)
                lineTo(32.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(48.0f, 136.0f)
                verticalLineToRelative(72.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(120.0f, 136.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(208.0f, 136.0f)
                horizontalLineToRelative(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(104.0f, 208.0f)
                lineTo(64.0f, 208.0f)
                lineTo(64.0f, 128.1f)
                horizontalLineToRelative(0.0f)
                lineTo(64.0f, 48.0f)
                horizontalLineToRelative(40.0f)
                close()
                moveTo(192.0f, 184.0f)
                lineTo(152.0f, 184.0f)
                lineTo(152.0f, 128.1f)
                horizontalLineToRelative(0.0f)
                lineTo(152.0f, 72.0f)
                horizontalLineToRelative(40.0f)
                close()
            }
        }
            .build()
        return `_align-center-vertical`!!
    }

private var `_align-center-vertical`: ImageVector? = null
