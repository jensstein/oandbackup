package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcLanguages: ImageVector
    get() {
        if (_icLanguages != null) {
            return _icLanguages!!
        }
        _icLanguages = Builder(
            name = "IcLanguages", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 21.0f, viewportHeight = 21.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(18.5f, 10.5f)
                verticalLineToRelative(-6.0f)
                curveToRelative(0.0f, -1.1046f, -0.8954f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-6.0f)
                curveToRelative(-1.1046f, 0.0f, -2.0f, 0.8954f, -2.0f, 2.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 1.1046f, 0.8954f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1046f, 0.0f, 2.0f, -0.8954f, 2.0f, -2.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(6.5f, 8.5034f)
                horizontalLineToRelative(-2.0f)
                curveToRelative(-1.1046f, 0.0f, -2.0f, 0.8954f, -2.0f, 2.0f)
                verticalLineToRelative(5.9994f)
                curveToRelative(0.0f, 1.1046f, 0.8954f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(0.0035f)
                lineToRelative(6.0f, -0.0104f)
                curveToRelative(1.1032f, -0.0019f, 1.9965f, -0.8968f, 1.9965f, -2.0f)
                verticalLineToRelative(-1.9925f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(7.5f, 12.503f)
                horizontalLineToRelative(-3.0f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(9.0004f, 14.0f)
                curveToRelative(-0.3333f, 0.3333f, -0.6667f, 0.6667f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.1667f, 0.8333f, -2.5f, 1.5f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(5.5004f, 12.5032f)
                curveToRelative(0.3333f, 1.1661f, 0.8333f, 1.9989f, 1.5f, 2.4984f)
                reflectiveCurveToRelative(1.5f, 0.9989f, 2.5f, 1.4984f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(13.5f, 4.5f)
                lineToRelative(-3.0f, 6.0f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(13.5f, 4.5f)
                lineToRelative(3.0f, 6.0f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveToRelative(15.5f, 8.5f)
                lineToRelative(-4.0f, 0.0f)
            }
        }
            .build()
        return _icLanguages!!
    }

private var _icLanguages: ImageVector? = null
