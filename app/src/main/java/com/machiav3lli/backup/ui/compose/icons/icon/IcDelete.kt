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

val Icon.IcDelete: ImageVector
    get() {
        if (_icDelete != null) {
            return _icDelete!!
        }
        _icDelete = Builder(
            name = "IcDelete", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 284.011f, viewportHeight = 284.011f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(235.732f, 66.214f)
                lineToRelative(-28.006f, -13.301f)
                lineToRelative(1.452f, -3.057f)
                curveToRelative(6.354f, -13.379f, 0.639f, -29.434f, -12.74f, -35.789f)
                lineTo(172.316f, 2.611f)
                curveToRelative(-6.48f, -3.079f, -13.771f, -3.447f, -20.532f, -1.042f)
                curveToRelative(-6.76f, 2.406f, -12.178f, 7.301f, -15.256f, 13.782f)
                lineToRelative(-1.452f, 3.057f)
                lineTo(107.07f, 5.106f)
                curveToRelative(-14.653f, -6.958f, -32.239f, -0.698f, -39.2f, 13.955f)
                lineTo(60.7f, 34.155f)
                curveToRelative(-1.138f, 2.396f, -1.277f, 5.146f, -0.388f, 7.644f)
                curveToRelative(0.89f, 2.499f, 2.735f, 4.542f, 5.131f, 5.68f)
                lineToRelative(74.218f, 35.25f)
                horizontalLineToRelative(-98.18f)
                curveToRelative(-2.797f, 0.0f, -5.465f, 1.171f, -7.358f, 3.229f)
                curveToRelative(-1.894f, 2.059f, -2.839f, 4.815f, -2.607f, 7.602f)
                lineToRelative(13.143f, 157.706f)
                curveToRelative(1.53f, 18.362f, 17.162f, 32.745f, 35.588f, 32.745f)
                horizontalLineToRelative(73.54f)
                curveToRelative(18.425f, 0.0f, 34.057f, -14.383f, 35.587f, -32.745f)
                lineToRelative(11.618f, -139.408f)
                lineToRelative(28.205f, 13.396f)
                curveToRelative(1.385f, 0.658f, 2.845f, 0.969f, 4.283f, 0.969f)
                curveToRelative(3.74f, 0.0f, 7.328f, -2.108f, 9.04f, -5.712f)
                lineToRelative(7.169f, -15.093f)
                curveTo(256.646f, 90.761f, 250.386f, 73.175f, 235.732f, 66.214f)
                close()
                moveTo(154.594f, 23.931f)
                curveToRelative(0.786f, -1.655f, 2.17f, -2.905f, 3.896f, -3.521f)
                curveToRelative(1.729f, -0.614f, 3.59f, -0.521f, 5.245f, 0.267f)
                lineToRelative(24.121f, 11.455f)
                curveToRelative(3.418f, 1.624f, 4.878f, 5.726f, 3.255f, 9.144f)
                lineToRelative(-1.452f, 3.057f)
                lineToRelative(-36.518f, -17.344f)
                lineTo(154.594f, 23.931f)
                close()
                moveTo(169.441f, 249.604f)
                curveToRelative(-0.673f, 8.077f, -7.55f, 14.405f, -15.655f, 14.405f)
                horizontalLineToRelative(-73.54f)
                curveToRelative(-8.106f, 0.0f, -14.983f, -6.328f, -15.656f, -14.405f)
                lineTo(52.35f, 102.728f)
                horizontalLineToRelative(129.332f)
                lineTo(169.441f, 249.604f)
                close()
                moveTo(231.62f, 96.835f)
                lineToRelative(-2.878f, 6.06f)
                lineTo(83.057f, 33.701f)
                lineToRelative(2.879f, -6.061f)
                curveToRelative(2.229f, -4.695f, 7.863f, -6.698f, 12.554f, -4.469f)
                lineToRelative(128.661f, 61.108f)
                curveTo(231.845f, 86.509f, 233.85f, 92.142f, 231.62f, 96.835f)
                close()
            }
        }
            .build()
        return _icDelete!!
    }

private var _icDelete: ImageVector? = null
