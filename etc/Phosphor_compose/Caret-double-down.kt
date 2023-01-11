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

val Phosphor.`Caret-double-down`: ImageVector
    get() {
        if (`_caret-double-down` != null) {
            return `_caret-double-down`!!
        }
        `_caret-double-down` = Builder(
            name = "Caret-double-down", defaultWidth = 256.0.dp,
            defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(213.7f, 122.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                lineToRelative(-80.0f, 80.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-80.0f, -80.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(128.0f, 196.7f)
                lineToRelative(74.3f, -74.4f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 213.7f, 122.3f)
                close()
                moveTo(122.3f, 133.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineToRelative(80.0f, -80.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, -11.4f)
                lineTo(128.0f, 116.7f)
                lineTo(53.7f, 42.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 42.3f, 53.7f)
                close()
            }
        }
            .build()
        return `_caret-double-down`!!
    }

private var `_caret-double-down`: ImageVector? = null
