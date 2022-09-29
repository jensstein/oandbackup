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

val Icon.IcSystem: ImageVector
    get() {
        if (_icSystem != null) {
            return _icSystem!!
        }
        _icSystem = Builder(
            name = "IcSystem", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 512.0f, viewportHeight = 512.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(256.0f, 0.0f)
                curveTo(114.618f, 0.0f, 0.0f, 114.618f, 0.0f, 256.0f)
                reflectiveCurveToRelative(114.618f, 256.0f, 256.0f, 256.0f)
                reflectiveCurveToRelative(256.0f, -114.618f, 256.0f, -256.0f)
                reflectiveCurveTo(397.382f, 0.0f, 256.0f, 0.0f)
                close()
                moveTo(106.571f, 341.328f)
                curveToRelative(0.032f, 0.0f, 0.063f, 0.005f, 0.095f, 0.005f)
                curveToRelative(0.029f, 0.0f, 0.057f, -0.004f, 0.085f, -0.004f)
                curveToRelative(47.081f, 0.045f, 85.25f, 38.24f, 85.25f, 85.331f)
                curveToRelative(0.0f, 10.576f, -2.027f, 20.897f, -5.9f, 30.654f)
                curveToRelative(-52.251f, -18.113f, -95.521f, -55.951f, -120.423f, -105.073f)
                curveTo(78.184f, 345.155f, 92.105f, 341.346f, 106.571f, 341.328f)
                close()
                moveTo(227.808f, 467.476f)
                curveToRelative(4.488f, -13.041f, 6.861f, -26.763f, 6.861f, -40.816f)
                curveToRelative(0.0f, -27.64f, -8.765f, -53.235f, -23.665f, -74.159f)
                lineToRelative(16.344f, -16.344f)
                curveToRelative(8.331f, -8.331f, 8.331f, -21.839f, 0.0f, -30.17f)
                curveToRelative(-8.331f, -8.331f, -21.839f, -8.331f, -30.17f, 0.0f)
                lineToRelative(-16.343f, 16.343f)
                curveToRelative(-15.423f, -10.985f, -33.387f, -18.627f, -52.835f, -21.891f)
                verticalLineToRelative(-23.107f)
                curveToRelative(0.0f, -11.782f, -9.551f, -21.333f, -21.333f, -21.333f)
                curveToRelative(-11.782f, 0.0f, -21.333f, 9.551f, -21.333f, 21.333f)
                verticalLineToRelative(23.168f)
                curveToRelative(-12.273f, 2.13f, -24.091f, 6.08f, -35.176f, 11.673f)
                curveToRelative(-4.874f, -17.9f, -7.49f, -36.73f, -7.49f, -56.174f)
                curveToRelative(0.0f, -117.818f, 95.515f, -213.333f, 213.333f, -213.333f)
                reflectiveCurveTo(469.333f, 138.182f, 469.333f, 256.0f)
                curveToRelative(0.0f, 19.444f, -2.617f, 38.274f, -7.49f, 56.174f)
                curveToRelative(-11.085f, -5.593f, -22.903f, -9.543f, -35.176f, -11.673f)
                verticalLineToRelative(-23.168f)
                curveToRelative(0.0f, -11.782f, -9.551f, -21.333f, -21.333f, -21.333f)
                curveTo(393.551f, 256.0f, 384.0f, 265.551f, 384.0f, 277.333f)
                verticalLineToRelative(23.107f)
                curveToRelative(-19.448f, 3.264f, -37.412f, 10.906f, -52.835f, 21.891f)
                lineToRelative(-16.343f, -16.343f)
                curveToRelative(-8.331f, -8.331f, -21.839f, -8.331f, -30.17f, 0.0f)
                curveToRelative(-8.331f, 8.331f, -8.331f, 21.839f, 0.0f, 30.17f)
                lineToRelative(16.344f, 16.344f)
                curveToRelative(-14.9f, 20.924f, -23.665f, 46.519f, -23.665f, 74.159f)
                curveToRelative(0.0f, 14.053f, 2.373f, 27.776f, 6.861f, 40.816f)
                curveToRelative(-9.226f, 1.218f, -18.634f, 1.857f, -28.192f, 1.857f)
                reflectiveCurveTo(237.034f, 468.695f, 227.808f, 467.476f)
                close()
                moveTo(405.248f, 341.329f)
                curveToRelative(0.029f, 0.0f, 0.056f, 0.004f, 0.085f, 0.004f)
                curveToRelative(0.032f, 0.0f, 0.063f, -0.005f, 0.095f, -0.005f)
                curveToRelative(14.467f, 0.017f, 28.388f, 3.827f, 40.892f, 10.912f)
                curveToRelative(-24.902f, 49.123f, -68.172f, 86.961f, -120.423f, 105.073f)
                curveToRelative(-3.873f, -9.757f, -5.9f, -20.078f, -5.9f, -30.654f)
                curveTo(319.998f, 379.569f, 358.167f, 341.374f, 405.248f, 341.329f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(341.333f, 234.667f)
                curveToRelative(0.0f, 11.782f, 9.551f, 21.333f, 21.333f, 21.333f)
                reflectiveCurveTo(384.0f, 246.449f, 384.0f, 234.667f)
                curveToRelative(0.0f, -27.641f, -8.766f, -53.237f, -23.667f, -74.161f)
                lineToRelative(16.348f, -16.348f)
                curveToRelative(8.331f, -8.331f, 8.331f, -21.839f, 0.0f, -30.17f)
                curveToRelative(-8.331f, -8.331f, -21.839f, -8.331f, -30.17f, 0.0f)
                lineToRelative(-16.347f, 16.347f)
                curveToRelative(-15.422f, -10.983f, -33.384f, -18.624f, -52.83f, -21.888f)
                verticalLineTo(85.333f)
                curveTo(277.333f, 73.551f, 267.782f, 64.0f, 256.0f, 64.0f)
                reflectiveCurveToRelative(-21.333f, 9.551f, -21.333f, 21.333f)
                verticalLineToRelative(23.114f)
                curveToRelative(-19.446f, 3.264f, -37.408f, 10.905f, -52.83f, 21.888f)
                lineToRelative(-16.347f, -16.347f)
                curveToRelative(-8.331f, -8.331f, -21.839f, -8.331f, -30.17f, 0.0f)
                curveToRelative(-8.331f, 8.331f, -8.331f, 21.839f, 0.0f, 30.17f)
                lineToRelative(16.348f, 16.348f)
                curveTo(136.766f, 181.43f, 128.0f, 207.026f, 128.0f, 234.667f)
                curveToRelative(0.0f, 11.782f, 9.551f, 21.333f, 21.333f, 21.333f)
                curveToRelative(11.782f, 0.0f, 21.333f, -9.551f, 21.333f, -21.333f)
                curveToRelative(0.0f, -47.119f, 38.214f, -85.333f, 85.333f, -85.333f)
                reflectiveCurveTo(341.333f, 187.547f, 341.333f, 234.667f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(256.0f, 213.333f)
                curveToRelative(-11.776f, 0.0f, -21.333f, 9.557f, -21.333f, 21.333f)
                reflectiveCurveTo(244.224f, 256.0f, 256.0f, 256.0f)
                reflectiveCurveToRelative(21.333f, -9.557f, 21.333f, -21.333f)
                reflectiveCurveTo(267.776f, 213.333f, 256.0f, 213.333f)
                close()
            }
        }
            .build()
        return _icSystem!!
    }

private var _icSystem: ImageVector? = null
