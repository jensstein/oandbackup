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

val Icon.IcOld: ImageVector
    get() {
        if (_icOld != null) {
            return _icOld!!
        }
        _icOld = Builder(
            name = "IcOld", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(128.0f, 230.0f)
                curveTo(86.745f, 230.0f, 49.552f, 205.148f, 33.765f, 167.034f)
                curveTo(17.978f, 128.92f, 26.707f, 85.05f, 55.879f, 55.879f)
                curveTo(85.05f, 26.707f, 128.92f, 17.978f, 167.034f, 33.765f)
                curveTo(205.148f, 49.552f, 230.0f, 86.745f, 230.0f, 128.0f)
                curveToRelative(-0.064f, 56.307f, -45.693f, 101.936f, -102.0f, 102.0f)
                close()
                moveTo(128.104f, 42.117f)
                curveToRelative(-36.402f, 0.0f, -66.122f, 20.029f, -80.051f, 53.659f)
                curveToRelative(-13.929f, 33.63f, -5.603f, 67.97f, 20.137f, 93.71f)
                curveToRelative(25.74f, 25.74f, 59.145f, 30.506f, 92.775f, 16.577f)
                curveToRelative(33.63f, -13.929f, 53.086f, -41.612f, 53.086f, -78.014f)
                curveToRelative(-0.056f, -49.682f, -36.263f, -85.876f, -85.946f, -85.932f)
                close()
                moveTo(190.0f, 128.0f)
                curveToRelative(0.0f, -3.314f, -2.686f, -6.0f, -6.0f, -6.0f)
                lineTo(134.0f, 122.0f)
                lineTo(134.0f, 72.0f)
                curveToRelative(0.0f, -3.314f, -2.686f, -6.0f, -6.0f, -6.0f)
                curveToRelative(-3.314f, 0.0f, -6.0f, 2.686f, -6.0f, 6.0f)
                verticalLineToRelative(56.0f)
                curveToRelative(0.0f, 3.314f, 2.686f, 6.0f, 6.0f, 6.0f)
                horizontalLineToRelative(56.0f)
                curveToRelative(3.314f, 0.0f, 6.0f, -2.686f, 6.0f, -6.0f)
                close()
            }
        }
            .build()
        return _icOld!!
    }

private var _icOld: ImageVector? = null
