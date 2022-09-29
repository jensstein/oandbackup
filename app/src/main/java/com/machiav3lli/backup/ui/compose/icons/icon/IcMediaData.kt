package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcMediaData: ImageVector
    get() {
        if (_icMediaData != null) {
            return _icMediaData!!
        }
        _icMediaData = Builder(
            name = "IcMediaData", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 32.0f, viewportHeight = 32.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.0f, 25.0f)
                arcToRelative(6.9908f, 6.9908f, 0.0f, false, true, -5.833f, -3.1287f)
                lineToRelative(1.666f, -1.1074f)
                arcToRelative(5.0007f, 5.0007f, 0.0f, false, false, 8.334f, 0.0f)
                lineToRelative(1.666f, 1.1074f)
                arcTo(6.9908f, 6.9908f, 0.0f, false, true, 20.0f, 25.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(24.0f, 14.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, false, 2.0f, 2.0f)
                arcTo(1.9806f, 1.9806f, 0.0f, false, false, 24.0f, 14.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(16.0f, 14.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, false, 2.0f, 2.0f)
                arcTo(1.9806f, 1.9806f, 0.0f, false, false, 16.0f, 14.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(28.0f, 8.0f)
                lineTo(22.0f, 8.0f)
                lineTo(22.0f, 4.0f)
                arcToRelative(2.0023f, 2.0023f, 0.0f, false, false, -2.0f, -2.0f)
                lineTo(4.0f, 2.0f)
                arcTo(2.0023f, 2.0023f, 0.0f, false, false, 2.0f, 4.0f)
                lineTo(2.0f, 14.0f)
                arcToRelative(10.01f, 10.01f, 0.0f, false, false, 8.8027f, 9.9214f)
                arcTo(9.9989f, 9.9989f, 0.0f, false, false, 30.0f, 20.0f)
                lineTo(30.0f, 10.0f)
                arcTo(2.0023f, 2.0023f, 0.0f, false, false, 28.0f, 8.0f)
                close()
                moveTo(4.0f, 14.0f)
                lineTo(4.0f, 4.0f)
                lineTo(20.0f, 4.0f)
                lineTo(20.0f, 8.0f)
                lineTo(12.0f, 8.0f)
                arcToRelative(2.0023f, 2.0023f, 0.0f, false, false, -2.0f, 2.0f)
                lineTo(10.0f, 20.0f)
                arcToRelative(9.9628f, 9.9628f, 0.0f, false, false, 0.168f, 1.78f)
                arcTo(8.0081f, 8.0081f, 0.0f, false, true, 4.0f, 14.0f)
                close()
                moveTo(28.0f, 20.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(12.0f, 10.0f)
                lineTo(28.0f, 10.0f)
                close()
            }
        }
            .build()
        return _icMediaData!!
    }

private var _icMediaData: ImageVector? = null
