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

val Icon.IcSpecial: ImageVector
    get() {
        if (_icSpecial != null) {
            return _icSpecial!!
        }
        _icSpecial = Builder(
            name = "IcSpecial", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 512.0f, viewportHeight = 512.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(256.0f, 0.0f)
                curveTo(114.842f, 0.0f, 0.0f, 114.842f, 0.0f, 256.0f)
                reflectiveCurveToRelative(114.842f, 256.0f, 256.0f, 256.0f)
                reflectiveCurveToRelative(256.0f, -114.842f, 256.0f, -256.0f)
                reflectiveCurveTo(397.158f, 0.0f, 256.0f, 0.0f)
                close()
                moveTo(256.0f, 478.609f)
                curveToRelative(-122.746f, 0.0f, -222.609f, -99.862f, -222.609f, -222.609f)
                reflectiveCurveTo(133.254f, 33.391f, 256.0f, 33.391f)
                reflectiveCurveTo(478.609f, 133.254f, 478.609f, 256.0f)
                reflectiveCurveTo(378.746f, 478.609f, 256.0f, 478.609f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(385.429f, 198.204f)
                lineToRelative(-79.088f, -11.492f)
                lineToRelative(-35.369f, -71.667f)
                curveToRelative(-6.114f, -12.386f, -23.828f, -12.388f, -29.943f, 0.0f)
                lineToRelative(-35.369f, 71.667f)
                lineToRelative(-79.088f, 11.492f)
                curveToRelative(-13.665f, 1.983f, -19.148f, 18.833f, -9.253f, 28.477f)
                lineToRelative(57.228f, 55.785f)
                lineToRelative(-13.51f, 78.768f)
                curveToRelative(-2.333f, 13.61f, 11.992f, 24.029f, 24.225f, 17.601f)
                lineTo(256.0f, 341.644f)
                lineToRelative(70.738f, 37.19f)
                curveToRelative(2.443f, 1.283f, 5.111f, 1.918f, 7.768f, 1.918f)
                curveToRelative(10.277f, 0.0f, 18.217f, -9.257f, 16.457f, -19.518f)
                lineToRelative(-13.51f, -78.768f)
                lineToRelative(57.228f, -55.785f)
                curveTo(404.57f, 217.043f, 399.104f, 200.19f, 385.429f, 198.204f)
                close()
                moveTo(307.861f, 264.682f)
                curveToRelative(-3.936f, 3.836f, -5.731f, 9.362f, -4.802f, 14.778f)
                lineToRelative(9.275f, 54.077f)
                lineToRelative(-48.564f, -25.532f)
                curveToRelative(-4.865f, -2.557f, -10.674f, -2.557f, -15.539f, 0.0f)
                lineToRelative(-48.564f, 25.532f)
                lineToRelative(9.275f, -54.077f)
                curveToRelative(0.929f, -5.416f, -0.866f, -10.942f, -4.802f, -14.778f)
                lineToRelative(-39.289f, -38.298f)
                lineToRelative(54.297f, -7.89f)
                curveToRelative(5.438f, -0.79f, 10.138f, -4.205f, 12.571f, -9.133f)
                lineTo(256.0f, 160.159f)
                lineToRelative(24.282f, 49.202f)
                curveToRelative(2.433f, 4.927f, 7.132f, 8.342f, 12.571f, 9.133f)
                lineToRelative(54.297f, 7.89f)
                lineTo(307.861f, 264.682f)
                close()
            }
        }
            .build()
        return _icSpecial!!
    }

private var _icSpecial: ImageVector? = null
