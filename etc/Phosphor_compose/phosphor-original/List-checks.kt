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

val Phosphor.`List-checks`: ImageVector
    get() {
        if (`_list-checks` != null) {
            return `_list-checks`!!
        }
        `_list-checks` = Builder(
            name = "List-checks", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(224.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(128.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(88.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 224.0f, 128.0f)
                close()
                moveTo(128.0f, 72.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(128.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                close()
                moveTo(216.0f, 184.0f)
                lineTo(128.0f, 184.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(86.6f, 42.1f)
                lineToRelative(-29.3f, 27.0f)
                lineToRelative(-11.9f, -11.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 34.6f, 69.9f)
                lineToRelative(17.3f, 16.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 57.3f, 88.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 5.5f, -2.1f)
                lineToRelative(34.6f, -32.0f)
                arcTo(8.0f, 8.0f, 0.0f, true, false, 86.6f, 42.1f)
                close()
                moveTo(86.6f, 106.1f)
                lineTo(57.3f, 133.1f)
                lineTo(45.4f, 122.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -10.8f, 11.8f)
                lineToRelative(17.3f, 16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 5.4f, 2.1f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 5.5f, -2.1f)
                lineToRelative(34.6f, -32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -10.8f, -11.8f)
                close()
                moveTo(86.6f, 170.1f)
                lineTo(57.3f, 197.1f)
                lineTo(45.4f, 186.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -10.8f, 11.8f)
                lineToRelative(17.3f, 16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 5.4f, 2.1f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 5.5f, -2.1f)
                lineToRelative(34.6f, -32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -10.8f, -11.8f)
                close()
            }
        }
            .build()
        return `_list-checks`!!
    }

private var `_list-checks`: ImageVector? = null
