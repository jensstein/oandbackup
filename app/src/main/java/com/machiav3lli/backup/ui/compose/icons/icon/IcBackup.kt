package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcBackup: ImageVector
    get() {
        if (_icBackup != null) {
            return _icBackup!!
        }
        _icBackup = Builder(
            name = "IcBackup", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 21.0f, viewportHeight = 21.0f
        ).apply {
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = EvenOdd
            ) {
                moveToRelative(12.5f, 4.5f)
                horizontalLineToRelative(2.3406f)
                curveToRelative(0.4f, 0.0f, 0.7616f, 0.2384f, 0.9191f, 0.6061f)
                lineToRelative(2.7403f, 6.3939f)
                verticalLineToRelative(4.0f)
                curveToRelative(0.0f, 1.1046f, -0.8954f, 2.0f, -2.0f, 2.0f)
                horizontalLineToRelative(-12.0f)
                curveToRelative(-1.1046f, 0.0f, -2.0f, -0.8954f, -2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                lineToRelative(2.7403f, -6.3939f)
                curveToRelative(0.1576f, -0.3677f, 0.5191f, -0.6061f, 0.9191f, -0.6061f)
                horizontalLineToRelative(2.3406f)
            }
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = EvenOdd
            ) {
                moveToRelative(13.5f, 7.586f)
                lineToRelative(-3.0f, 2.914f)
                lineToRelative(-3.0f, -2.914f)
            }
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = EvenOdd
            ) {
                moveToRelative(10.5f, 1.5f)
                verticalLineToRelative(9.0f)
            }
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = EvenOdd
            ) {
                moveToRelative(2.5f, 11.5f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.5523f, 0.0f, 1.0f, 0.4477f, 1.0f, 1.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 0.5523f, 0.4477f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.5523f, 0.0f, 1.0f, -0.4477f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                curveToRelative(0.0f, -0.5523f, 0.4477f, -1.0f, 1.0f, -1.0f)
                horizontalLineToRelative(4.0f)
            }
        }
            .build()
        return _icBackup!!
    }

private var _icBackup: ImageVector? = null
