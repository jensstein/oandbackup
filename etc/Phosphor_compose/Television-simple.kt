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

val Phosphor.`Television-simple`: ImageVector
    get() {
        if (`_television-simple` != null) {
            return `_television-simple`!!
        }
        `_television-simple` = Builder(
            name = "Television-simple", defaultWidth = 256.0.dp,
            defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(216.0f, 64.0f)
                lineTo(147.3f, 64.0f)
                lineToRelative(34.4f, -34.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, -11.4f)
                lineTo(128.0f, 60.7f)
                lineTo(85.7f, 18.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 74.3f, 29.7f)
                lineTo(108.7f, 64.0f)
                lineTo(40.0f, 64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 24.0f, 80.0f)
                lineTo(24.0f, 200.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(216.0f, 216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(232.0f, 80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 216.0f, 64.0f)
                close()
                moveTo(216.0f, 200.0f)
                lineTo(40.0f, 200.0f)
                lineTo(40.0f, 80.0f)
                lineTo(216.0f, 80.0f)
                lineTo(216.0f, 200.0f)
                close()
            }
        }
            .build()
        return `_television-simple`!!
    }

private var `_television-simple`: ImageVector? = null
