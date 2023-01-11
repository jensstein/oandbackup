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

val Phosphor.`Align-left-simple`: ImageVector
    get() {
        if (`_align-left-simple` != null) {
            return `_align-left-simple`!!
        }
        `_align-left-simple` = Builder(
            name = "Align-left-simple", defaultWidth = 256.0.dp,
            defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(40.0f, 56.0f)
                lineTo(40.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(24.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
                moveTo(240.0f, 96.0f)
                verticalLineToRelative(64.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(72.0f, 176.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(56.0f, 96.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 72.0f, 80.0f)
                lineTo(224.0f, 80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 240.0f, 96.0f)
                close()
                moveTo(224.0f, 160.0f)
                lineTo(224.0f, 96.0f)
                lineTo(72.0f, 96.0f)
                verticalLineToRelative(64.0f)
                close()
            }
        }
            .build()
        return `_align-left-simple`!!
    }

private var `_align-left-simple`: ImageVector? = null
