package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcPrefsService: ImageVector
    get() {
        if (_icPrefsService != null) {
            return _icPrefsService!!
        }
        _icPrefsService = Builder(
            name = "IcPrefsService", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f, strokeLineCap = Round, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(14.0f, 5.0f)
                curveTo(14.0f, 3.8954f, 13.1046f, 3.0f, 12.0f, 3.0f)
                curveTo(10.8954f, 3.0f, 10.0f, 3.8954f, 10.0f, 5.0f)
                moveTo(14.0f, 5.0f)
                curveTo(14.0f, 6.1046f, 13.1046f, 7.0f, 12.0f, 7.0f)
                curveTo(10.8954f, 7.0f, 10.0f, 6.1046f, 10.0f, 5.0f)
                moveTo(14.0f, 5.0f)
                horizontalLineTo(20.0f)
                moveTo(10.0f, 5.0f)
                lineTo(4.0f, 5.0f)
                moveTo(16.0f, 12.0f)
                curveTo(16.0f, 13.1046f, 16.8954f, 14.0f, 18.0f, 14.0f)
                curveTo(19.1046f, 14.0f, 20.0f, 13.1046f, 20.0f, 12.0f)
                curveTo(20.0f, 10.8954f, 19.1046f, 10.0f, 18.0f, 10.0f)
                curveTo(16.8954f, 10.0f, 16.0f, 10.8954f, 16.0f, 12.0f)
                close()
                moveTo(16.0f, 12.0f)
                horizontalLineTo(4.0f)
                moveTo(8.0f, 19.0f)
                curveTo(8.0f, 17.8954f, 7.1046f, 17.0f, 6.0f, 17.0f)
                curveTo(4.8954f, 17.0f, 4.0f, 17.8954f, 4.0f, 19.0f)
                curveTo(4.0f, 20.1046f, 4.8954f, 21.0f, 6.0f, 21.0f)
                curveTo(7.1046f, 21.0f, 8.0f, 20.1046f, 8.0f, 19.0f)
                close()
                moveTo(8.0f, 19.0f)
                horizontalLineTo(20.0f)
            }
        }
            .build()
        return _icPrefsService!!
    }

private var _icPrefsService: ImageVector? = null
