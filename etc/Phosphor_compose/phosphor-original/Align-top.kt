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

val Phosphor.`Align-top`: ImageVector
    get() {
        if (`_align-top` != null) {
            return `_align-top`!!
        }
        `_align-top` = Builder(
            name = "Align-top", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(224.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(40.0f, 48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                lineTo(216.0f, 32.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 224.0f, 40.0f)
                close()
                moveTo(208.0f, 80.0f)
                verticalLineToRelative(96.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(152.0f, 192.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(136.0f, 80.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                horizontalLineToRelative(40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 208.0f, 80.0f)
                close()
                moveTo(192.0f, 80.0f)
                lineTo(152.0f, 80.0f)
                verticalLineToRelative(96.0f)
                horizontalLineToRelative(40.0f)
                close()
                moveTo(120.0f, 80.0f)
                lineTo(120.0f, 216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(64.0f, 232.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(48.0f, 80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 64.0f, 64.0f)
                horizontalLineToRelative(40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 120.0f, 80.0f)
                close()
                moveTo(104.0f, 80.0f)
                lineTo(64.0f, 80.0f)
                lineTo(64.0f, 216.0f)
                horizontalLineToRelative(40.0f)
                close()
            }
        }
            .build()
        return `_align-top`!!
    }

private var `_align-top`: ImageVector? = null
