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

val Icon.IcListOrdered: ImageVector
    get() {
        if (_icListOrdered != null) {
            return _icListOrdered!!
        }
        _icListOrdered = Builder(
            name = "IcListOrdered", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(8.0f, 7.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(8.0f)
                curveTo(7.45f, 5.0f, 7.0f, 5.45f, 7.0f, 6.0f)
                reflectiveCurveTo(7.45f, 7.0f, 8.0f, 7.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.0f, 17.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveTo(20.55f, 17.0f, 20.0f, 17.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(20.0f, 11.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(12.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveTo(20.55f, 11.0f, 20.0f, 11.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(4.5f, 16.0f)
                horizontalLineToRelative(-2.0f)
                curveTo(2.22f, 16.0f, 2.0f, 16.22f, 2.0f, 16.5f)
                lineToRelative(0.0f, 0.0f)
                curveTo(2.0f, 16.78f, 2.22f, 17.0f, 2.5f, 17.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(0.5f)
                horizontalLineTo(3.5f)
                curveTo(3.22f, 17.5f, 3.0f, 17.72f, 3.0f, 18.0f)
                lineToRelative(0.0f, 0.0f)
                curveToRelative(0.0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f)
                horizontalLineTo(4.0f)
                verticalLineTo(19.0f)
                horizontalLineTo(2.5f)
                curveTo(2.22f, 19.0f, 2.0f, 19.22f, 2.0f, 19.5f)
                lineToRelative(0.0f, 0.0f)
                curveTo(2.0f, 19.78f, 2.22f, 20.0f, 2.5f, 20.0f)
                horizontalLineToRelative(2.0f)
                curveTo(4.78f, 20.0f, 5.0f, 19.78f, 5.0f, 19.5f)
                verticalLineToRelative(-3.0f)
                curveTo(5.0f, 16.22f, 4.78f, 16.0f, 4.5f, 16.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(2.5f, 5.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(2.5f)
                curveTo(3.0f, 7.78f, 3.22f, 8.0f, 3.5f, 8.0f)
                lineToRelative(0.0f, 0.0f)
                curveTo(3.78f, 8.0f, 4.0f, 7.78f, 4.0f, 7.5f)
                verticalLineToRelative(-3.0f)
                curveTo(4.0f, 4.22f, 3.78f, 4.0f, 3.5f, 4.0f)
                horizontalLineToRelative(-1.0f)
                curveTo(2.22f, 4.0f, 2.0f, 4.22f, 2.0f, 4.5f)
                lineToRelative(0.0f, 0.0f)
                curveTo(2.0f, 4.78f, 2.22f, 5.0f, 2.5f, 5.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(4.5f, 10.0f)
                horizontalLineToRelative(-2.0f)
                curveTo(2.22f, 10.0f, 2.0f, 10.22f, 2.0f, 10.5f)
                lineToRelative(0.0f, 0.0f)
                curveTo(2.0f, 10.78f, 2.22f, 11.0f, 2.5f, 11.0f)
                horizontalLineToRelative(1.3f)
                lineToRelative(-1.68f, 1.96f)
                curveTo(2.04f, 13.05f, 2.0f, 13.17f, 2.0f, 13.28f)
                verticalLineToRelative(0.22f)
                curveTo(2.0f, 13.78f, 2.22f, 14.0f, 2.5f, 14.0f)
                horizontalLineToRelative(2.0f)
                curveTo(4.78f, 14.0f, 5.0f, 13.78f, 5.0f, 13.5f)
                lineToRelative(0.0f, 0.0f)
                curveTo(5.0f, 13.22f, 4.78f, 13.0f, 4.5f, 13.0f)
                horizontalLineTo(3.2f)
                lineToRelative(1.68f, -1.96f)
                curveTo(4.96f, 10.95f, 5.0f, 10.83f, 5.0f, 10.72f)
                verticalLineTo(10.5f)
                curveTo(5.0f, 10.22f, 4.78f, 10.0f, 4.5f, 10.0f)
                close()
            }
        }
            .build()
        return _icListOrdered!!
    }

private var _icListOrdered: ImageVector? = null
