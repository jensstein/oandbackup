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

val Phosphor.AsteriskSimple: ImageVector
    get() {
        if (_asterisk_simple != null) {
            return _asterisk_simple!!
        }
        _asterisk_simple = Builder(
            name = "Asterisk-simple",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(214.2f, 108.4f)
                lineToRelative(-73.3f, 23.8f)
                lineToRelative(45.3f, 62.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -12.9f, 9.4f)
                lineTo(128.0f, 141.6f)
                lineTo(82.7f, 203.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -12.9f, -9.4f)
                lineToRelative(45.3f, -62.3f)
                lineTo(41.8f, 108.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 5.0f, -15.2f)
                lineTo(120.0f, 117.0f)
                verticalLineTo(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(77.0f)
                lineToRelative(73.2f, -23.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 5.0f, 15.2f)
                close()
            }
        }
            .build()
        return _asterisk_simple!!
    }

private var _asterisk_simple: ImageVector? = null
