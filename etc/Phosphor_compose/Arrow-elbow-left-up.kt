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

val Phosphor.`Arrow-elbow-left-up`: ImageVector
    get() {
        if (`_arrow-elbow-left-up` != null) {
            return `_arrow-elbow-left-up`!!
        }
        `_arrow-elbow-left-up` = Builder(
            name = "Arrow-elbow-left-up", defaultWidth = 256.0.dp,
            defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(232.0f, 192.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, -8.0f)
                verticalLineTo(67.3f)
                lineTo(37.7f, 101.7f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 26.3f, 90.3f)
                lineToRelative(48.0f, -48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 0.0f)
                lineToRelative(48.0f, 48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineTo(88.0f, 67.3f)
                verticalLineTo(184.0f)
                horizontalLineTo(224.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 232.0f, 192.0f)
                close()
            }
        }
            .build()
        return `_arrow-elbow-left-up`!!
    }

private var `_arrow-elbow-left-up`: ImageVector? = null
