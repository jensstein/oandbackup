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

public val Icon.Exodus: ImageVector
    get() {
        if (_Exodus != null) {
            return _Exodus!!
        }
        _Exodus = Builder(
            name = "IcExodusSimple",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 612.0f,
            viewportHeight = 612.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 0.589891f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(254.523f, 296.365f)
                curveTo(202.337f, 279.395f, 176.245f, 253.696f, 176.245f, 219.269f)
                curveToRelative(-0.0f, -27.153f, 13.841f, -48.851f, 41.523f, -65.095f)
                curveToRelative(27.682f, -16.243f, 62.053f, -24.365f, 103.113f, -24.365f)
                curveToRelative(37.351f, 0.0f, 67.02f, 5.637f, 89.007f, 16.91f)
                curveToRelative(21.986f, 11.274f, 32.98f, 24.79f, 32.98f, 40.548f)
                curveToRelative(-0.0f, 8.243f, -3.444f, 15.456f, -10.331f, 21.638f)
                curveToRelative(-6.888f, 6.182f, -14.835f, 9.274f, -23.841f, 9.273f)
                curveToRelative(-14.835f, 0.0f, -27.02f, -9.455f, -36.556f, -28.365f)
                curveToRelative(-13.245f, -26.183f, -33.113f, -39.275f, -59.603f, -39.275f)
                curveToRelative(-20.927f, 0.0f, -45.086f, 6.304f, -58.596f, 18.91f)
                curveToRelative(-13.51f, 12.607f, -25.799f, 22.309f, -25.799f, 44.856f)
                curveToRelative(-0.0f, 44.367f, 26.63f, 63.657f, 76.696f, 63.657f)
                curveToRelative(0.0f, 0.0f, 23.526f, -7.111f, 30.413f, -8.081f)
                curveToRelative(11.92f, -1.454f, 22.9f, -0.595f, 29.523f, -0.595f)
                curveToRelative(18.816f, 5.546f, 21.272f, 20.049f, 21.272f, 28.534f)
                curveToRelative(-0.0f, 9.455f, -8.212f, 14.183f, -24.636f, 14.183f)
                curveToRelative(-5.828f, 0.0f, -14.57f, -0.848f, -26.225f, -2.546f)
                curveToRelative(-8.742f, -1.454f, -15.497f, -2.182f, -20.265f, -2.182f)
                curveToRelative(-52.98f, 0.0f, -79.47f, 24.729f, -79.47f, 74.186f)
                curveToRelative(-0.0f, 24.002f, -0.632f, 38.304f, 21.06f, 58.004f)
                curveToRelative(21.384f, 11.777f, 34.832f, 11.476f, 59.998f, 11.476f)
                curveToRelative(31.523f, 0.0f, 51.26f, -4.385f, 61.592f, -34.205f)
                curveToRelative(5.298f, -15.758f, 11.192f, -26.668f, 17.682f, -32.729f)
                curveToRelative(6.49f, -6.061f, 15.165f, -9.091f, 26.027f, -9.091f)
                curveToRelative(9.006f, 0.0f, 17.152f, 2.97f, 24.437f, 8.91f)
                curveToRelative(7.284f, 5.94f, 10.927f, 13.516f, 10.927f, 22.729f)
                curveToRelative(-0.0f, 22.062f, -13.51f, 40.306f, -40.53f, 54.731f)
                curveToRelative(-27.02f, 14.425f, -59.735f, 21.638f, -98.146f, 21.638f)
                curveToRelative(-42.119f, 0.0f, -79.537f, -9.213f, -112.252f, -27.638f)
                curveToRelative(-32.715f, -18.425f, -49.073f, -43.033f, -49.073f, -73.823f)
                curveToRelative(-0.0f, -38.548f, 32.45f, -66.913f, 97.351f, -85.096f)
                close()
            }
        }
            .build()
        return _Exodus!!
    }

private var _Exodus: ImageVector? = null
