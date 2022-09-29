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

val Icon.IcTheme: ImageVector
    get() {
        if (_icTheme != null) {
            return _icTheme!!
        }
        _icTheme = Builder(
            name = "IcTheme", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 512.0f, viewportHeight = 512.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.821927f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(414.318f, 274.979f)
                horizontalLineToRelative(-12.955f)
                lineToRelative(46.102f, -46.278f)
                curveToRelative(10.111f, -10.111f, 15.68f, -23.554f, 15.68f, -37.853f)
                curveToRelative(0.0f, -14.299f, -7.2f, -28.527f, -17.311f, -38.639f)
                lineTo(386.851f, 93.226f)
                curveToRelative(-10.111f, -10.111f, -23.554f, -15.68f, -37.853f, -15.68f)
                curveToRelative(-14.299f, 0.0f, -27.742f, 5.569f, -37.853f, 15.68f)
                lineTo(237.503f, 166.868f)
                lineTo(237.503f, 98.163f)
                curveToRelative(0.0f, -29.517f, -24.014f, -53.532f, -53.532f, -53.532f)
                horizontalLineToRelative(-83.415f)
                curveToRelative(-29.518f, 0.0f, -53.532f, 24.014f, -53.532f, 53.532f)
                lineTo(47.024f, 411.926f)
                curveToRelative(0.0f, 29.518f, 24.014f, 53.532f, 53.532f, 53.532f)
                horizontalLineToRelative(313.762f)
                curveToRelative(29.518f, 0.0f, 53.532f, -24.014f, 53.532f, -53.532f)
                verticalLineToRelative(-83.415f)
                curveToRelative(0.0f, -29.518f, -24.014f, -53.532f, -53.532f, -53.532f)
                close()
                moveTo(238.643f, 192.519f)
                lineTo(321.3f, 110.735f)
                curveToRelative(15.692f, -15.691f, 38.099f, -13.953f, 53.912f, 1.615f)
                lineToRelative(52.119f, 51.31f)
                curveToRelative(15.69f, 15.691f, 18.69f, 39.607f, 3.014f, 55.285f)
                lineTo(294.261f, 357.688f)
                lineTo(237.503f, 305.45f)
                close()
                moveTo(237.503f, 408.124f)
                lineTo(237.941f, 334.94f)
                lineTo(277.439f, 371.284f)
                lineTo(237.288f, 412.766f)
                curveToRelative(0.133f, -1.531f, 0.215f, -3.076f, 0.215f, -4.641f)
                close()
                moveTo(184.116f, 446.191f)
                horizontalLineToRelative(-83.416f)
                curveToRelative(-22.225f, 0.0f, -36.01f, -20.366f, -36.046f, -42.591f)
                lineToRelative(-0.145f, -91.103f)
                lineToRelative(29.997f, 0.046f)
                curveToRelative(9.197f, -2.515f, 5.823f, -19.006f, -0.289f, -19.384f)
                lineTo(64.22f, 293.114f)
                lineTo(64.73f, 105.214f)
                curveTo(64.79f, 82.989f, 78.551f, 67.32f, 100.775f, 67.32f)
                horizontalLineToRelative(83.415f)
                curveToRelative(22.226f, 0.0f, 35.643f, 17.57f, 35.618f, 39.795f)
                lineToRelative(-0.349f, 184.911f)
                lineToRelative(-125.241f, 1.134f)
                curveToRelative(-20.341f, 1.607f, -11.309f, 18.3f, 0.289f, 19.384f)
                lineToRelative(125.242f, -1.134f)
                lineToRelative(-0.016f, 94.091f)
                curveToRelative(-0.004f, 22.225f, -13.392f, 40.691f, -35.617f, 40.691f)
                close()
                moveTo(282.191f, 444.094f)
                lineTo(237.434f, 444.85f)
                lineTo(282.145f, 399.205f)
                close()
                moveTo(448.719f, 411.379f)
                curveToRelative(0.0f, 22.19f, -12.455f, 32.998f, -34.644f, 32.801f)
                lineToRelative(-110.479f, 0.417f)
                lineToRelative(1.463f, -64.988f)
                lineToRelative(82.087f, -85.173f)
                horizontalLineToRelative(26.317f)
                curveToRelative(22.19f, 0.0f, 35.255f, 11.337f, 35.255f, 33.527f)
                verticalLineToRelative(83.416f)
                close()
            }
        }
            .build()
        return _icTheme!!
    }

private var _icTheme: ImageVector? = null
