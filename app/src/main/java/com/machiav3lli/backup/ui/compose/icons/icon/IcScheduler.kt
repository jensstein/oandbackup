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

val Icon.IcScheduler: ImageVector
    get() {
        if (_icScheduler != null) {
            return _icScheduler!!
        }
        _icScheduler = Builder(
            name = "IcScheduler", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(20.0f, 10.0f)
                verticalLineTo(7.0f)
                curveTo(20.0f, 5.8954f, 19.1046f, 5.0f, 18.0f, 5.0f)
                horizontalLineTo(6.0f)
                curveTo(4.8954f, 5.0f, 4.0f, 5.8954f, 4.0f, 7.0f)
                verticalLineTo(19.0f)
                curveTo(4.0f, 20.1046f, 4.8954f, 21.0f, 6.0f, 21.0f)
                horizontalLineTo(9.0f)
                moveTo(15.0f, 3.0f)
                verticalLineTo(7.0f)
                moveTo(9.0f, 3.0f)
                verticalLineTo(7.0f)
                moveTo(4.0f, 11.0f)
                horizontalLineTo(9.0f)
                moveTo(16.0f, 15.2f)
                verticalLineTo(16.8875f)
                lineTo(16.9f, 17.9f)
                moveTo(20.5f, 17.0f)
                curveTo(20.5f, 19.4853f, 18.4853f, 21.5f, 16.0f, 21.5f)
                curveTo(13.5147f, 21.5f, 11.5f, 19.4853f, 11.5f, 17.0f)
                curveTo(11.5f, 14.5147f, 13.5147f, 12.5f, 16.0f, 12.5f)
                curveTo(18.4853f, 12.5f, 20.5f, 14.5147f, 20.5f, 17.0f)
                close()
            }
        }
            .build()
        return _icScheduler!!
    }

private var _icScheduler: ImageVector? = null
