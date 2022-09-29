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

val Icon.IcDeData: ImageVector
    get() {
        if (_icDeData != null) {
            return _icDeData!!
        }
        _icDeData = Builder(
            name = "IcDeData", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 511.999f, viewportHeight = 511.999f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(446.653f, 67.763f)
                curveToRelative(-72.685f, -5.511f, -132.36f, -26.769f, -182.435f, -64.986f)
                curveToRelative(-4.855f, -3.703f, -11.584f, -3.703f, -16.435f, 0.0f)
                curveToRelative(-50.074f, 38.217f, -109.75f, 59.475f, -182.436f, 64.986f)
                curveToRelative(-7.065f, 0.535f, -12.522f, 6.423f, -12.522f, 13.506f)
                verticalLineToRelative(196.402f)
                curveToRelative(0.0f, 44.627f, 33.398f, 97.509f, 99.266f, 157.179f)
                curveToRelative(47.672f, 43.185f, 94.574f, 73.697f, 96.547f, 74.974f)
                curveToRelative(2.239f, 1.451f, 4.8f, 2.175f, 7.362f, 2.175f)
                reflectiveCurveToRelative(5.121f, -0.725f, 7.363f, -2.175f)
                curveToRelative(1.972f, -1.277f, 48.874f, -31.789f, 96.546f, -74.974f)
                curveToRelative(65.868f, -59.67f, 99.266f, -112.552f, 99.266f, -157.179f)
                verticalLineTo(81.269f)
                curveTo(459.174f, 74.187f, 453.717f, 68.299f, 446.653f, 67.763f)
                close()
                moveTo(432.084f, 277.671f)
                curveToRelative(0.0f, 70.936f, -120.775f, 167.222f, -176.085f, 204.54f)
                curveToRelative(-55.31f, -37.318f, -176.085f, -133.604f, -176.085f, -204.54f)
                verticalLineTo(93.667f)
                curveTo(148.564f, 86.702f, 206.374f, 65.925f, 256.0f, 30.387f)
                curveToRelative(49.626f, 35.537f, 107.437f, 56.315f, 176.085f, 63.279f)
                verticalLineTo(277.671f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(248.974f, 67.48f)
                curveToRelative(-39.165f, 23.759f, -81.948f, 39.534f, -130.797f, 48.226f)
                curveToRelative(-6.464f, 1.151f, -11.172f, 6.771f, -11.172f, 13.336f)
                verticalLineToRelative(126.957f)
                curveToRelative(0.0f, 7.481f, 6.064f, 13.545f, 13.545f, 13.545f)
                horizontalLineToRelative(121.905f)
                verticalLineTo(432.26f)
                curveToRelative(0.0f, 7.481f, 6.078f, 13.559f, 13.559f, 13.559f)
                curveToRelative(2.899f, 0.0f, 5.72f, -0.931f, 8.051f, -2.652f)
                curveToRelative(21.526f, -15.911f, 41.53f, -32.217f, 59.461f, -48.465f)
                curveToRelative(51.774f, -46.908f, 81.469f, -89.563f, 81.469f, -117.03f)
                verticalLineToRelative(-21.672f)
                curveToRelative(0.0f, -7.481f, -6.065f, -13.545f, -13.545f, -13.545f)
                horizontalLineTo(269.545f)
                verticalLineTo(79.061f)
                curveToRelative(0.0f, -4.891f, -2.637f, -9.403f, -6.9f, -11.803f)
                curveTo(258.382f, 64.859f, 253.158f, 64.943f, 248.974f, 67.48f)
                close()
                moveTo(242.455f, 242.454f)
                horizontalLineToRelative(-108.36f)
                verticalLineTo(140.253f)
                curveToRelative(39.367f, -7.975f, 75.092f, -20.488f, 108.36f, -37.977f)
                verticalLineTo(242.454f)
                close()
                moveTo(269.545f, 269.544f)
                horizontalLineToRelative(108.36f)
                verticalLineToRelative(8.127f)
                curveToRelative(0.0f, 11.592f, -15.283f, 45.056f, -72.567f, 96.955f)
                curveToRelative(-11.133f, 10.087f, -23.112f, 20.209f, -35.793f, 30.25f)
                verticalLineTo(269.544f)
                close()
            }
        }
            .build()
        return _icDeData!!
    }

private var _icDeData: ImageVector? = null
