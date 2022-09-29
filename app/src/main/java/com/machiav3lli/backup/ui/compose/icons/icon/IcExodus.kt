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

val Icon.IcExodus: ImageVector
    get() {
        if (_icExodus != null) {
            return _icExodus!!
        }
        _icExodus = Builder(
            name = "IcExodus", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 612.0f, viewportHeight = 612.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 0.507349f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(258.027f, 288.55f)
                curveTo(213.144f, 273.954f, 190.702f, 251.852f, 190.702f, 222.242f)
                curveToRelative(-0.0f, -23.354f, 11.904f, -42.016f, 35.713f, -55.986f)
                curveToRelative(23.809f, -13.97f, 53.37f, -20.956f, 88.684f, -20.956f)
                curveToRelative(32.124f, 0.0f, 57.642f, 4.848f, 76.552f, 14.544f)
                curveToRelative(18.91f, 9.696f, 28.365f, 21.321f, 28.365f, 34.874f)
                curveToRelative(-0.0f, 7.09f, -2.962f, 13.293f, -8.886f, 18.61f)
                curveToRelative(-5.924f, 5.317f, -12.759f, 7.976f, -20.505f, 7.976f)
                curveToRelative(-12.759f, 0.0f, -23.239f, -8.132f, -31.441f, -24.396f)
                curveToRelative(-11.392f, -22.519f, -28.479f, -33.779f, -51.263f, -33.78f)
                curveToRelative(-17.999f, 0.0f, -38.777f, 5.422f, -50.397f, 16.264f)
                curveToRelative(-11.62f, 10.843f, -22.189f, 19.188f, -22.189f, 38.579f)
                curveToRelative(-0.0f, 38.159f, 22.904f, 54.75f, 65.964f, 54.75f)
                curveToRelative(0.0f, 0.0f, 20.234f, -6.116f, 26.158f, -6.951f)
                curveToRelative(10.252f, -1.251f, 19.696f, -0.511f, 25.392f, -0.512f)
                curveToRelative(16.183f, 4.77f, 18.296f, 17.244f, 18.296f, 24.542f)
                curveToRelative(-0.0f, 8.132f, -7.063f, 12.198f, -21.189f, 12.198f)
                curveToRelative(-5.013f, 0.0f, -12.531f, -0.73f, -22.556f, -2.189f)
                curveToRelative(-7.519f, -1.251f, -13.328f, -1.876f, -17.429f, -1.877f)
                curveToRelative(-45.567f, 0.0f, -68.35f, 21.269f, -68.35f, 63.806f)
                curveToRelative(-0.0f, 20.643f, -0.543f, 32.944f, 18.113f, 49.887f)
                curveToRelative(18.392f, 10.129f, 29.958f, 9.87f, 51.602f, 9.87f)
                curveToRelative(27.112f, 0.0f, 44.088f, -3.771f, 52.973f, -29.419f)
                curveToRelative(4.556f, -13.553f, 9.626f, -22.937f, 15.208f, -28.15f)
                curveToRelative(5.582f, -5.213f, 13.043f, -7.819f, 22.385f, -7.819f)
                curveToRelative(7.746f, 0.0f, 14.752f, 2.554f, 21.018f, 7.663f)
                curveToRelative(6.265f, 5.109f, 9.398f, 11.625f, 9.398f, 19.548f)
                curveToRelative(-0.0f, 18.975f, -11.62f, 34.666f, -34.859f, 47.072f)
                curveToRelative(-23.239f, 12.407f, -51.377f, 18.61f, -84.412f, 18.61f)
                curveToRelative(-36.226f, 0.0f, -68.407f, -7.924f, -96.545f, -23.771f)
                curveToRelative(-28.138f, -15.847f, -42.206f, -37.011f, -42.206f, -63.493f)
                curveToRelative(-0.0f, -33.154f, 27.91f, -57.55f, 83.729f, -73.189f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                fillAlpha = 0.998901f, strokeLineWidth = 32.0f, strokeLineCap = Butt,
                strokeLineJoin = Miter, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(76.738f, 297.152f)
                arcToRelative(228.824f, 226.571f, 90.0f, true, false, 453.141f, 0.0f)
                arcToRelative(228.824f, 226.571f, 90.0f, true, false, -453.141f, 0.0f)
                close()
            }
        }
            .build()
        return _icExodus!!
    }

private var _icExodus: ImageVector? = null
