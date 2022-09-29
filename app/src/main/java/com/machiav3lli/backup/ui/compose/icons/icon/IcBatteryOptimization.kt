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

val Icon.IcBatteryOptimization: ImageVector
    get() {
        if (_icBatteryOptimization != null) {
            return _icBatteryOptimization!!
        }
        _icBatteryOptimization = Builder(
            name = "IcBatteryOptimization", defaultWidth = 32.0.dp,
            defaultHeight = 32.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(6.05f, 8.05f)
                curveToRelative(-2.73f, 2.73f, -2.73f, 7.17f, 0.0f, 9.9f)
                curveTo(7.42f, 19.32f, 9.21f, 20.0f, 11.0f, 20.0f)
                reflectiveCurveToRelative(3.58f, -0.68f, 4.95f, -2.05f)
                curveTo(19.43f, 14.47f, 20.0f, 4.0f, 20.0f, 4.0f)
                reflectiveCurveTo(9.53f, 4.57f, 6.05f, 8.05f)
                close()
                moveTo(14.54f, 16.54f)
                curveTo(13.59f, 17.48f, 12.34f, 18.0f, 11.0f, 18.0f)
                curveToRelative(-0.89f, 0.0f, -1.73f, -0.25f, -2.48f, -0.68f)
                curveToRelative(0.92f, -2.88f, 2.62f, -5.41f, 4.88f, -7.32f)
                curveToRelative(-2.63f, 1.36f, -4.84f, 3.46f, -6.37f, 6.0f)
                curveToRelative(-1.48f, -1.96f, -1.35f, -4.75f, 0.44f, -6.54f)
                curveTo(9.21f, 7.72f, 14.04f, 6.65f, 17.8f, 6.2f)
                curveTo(17.35f, 9.96f, 16.28f, 14.79f, 14.54f, 16.54f)
                close()
            }
        }
            .build()
        return _icBatteryOptimization!!
    }

private var _icBatteryOptimization: ImageVector? = null
