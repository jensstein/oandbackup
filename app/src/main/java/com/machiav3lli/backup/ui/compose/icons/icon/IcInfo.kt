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

val Icon.IcInfo: ImageVector
    get() {
        if (_icInfo != null) {
            return _icInfo!!
        }
        _icInfo = Builder(
            name = "IcInfo", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(11.25f, 17.0f)
                curveTo(11.25f, 17.4142f, 11.5858f, 17.75f, 12.0f, 17.75f)
                curveTo(12.4142f, 17.75f, 12.75f, 17.4142f, 12.75f, 17.0f)
                lineTo(11.25f, 17.0f)
                close()
                moveTo(12.75f, 10.0f)
                curveTo(12.75f, 9.5858f, 12.4142f, 9.25f, 12.0f, 9.25f)
                curveTo(11.5858f, 9.25f, 11.25f, 9.5858f, 11.25f, 10.0f)
                lineTo(12.75f, 10.0f)
                close()
                moveTo(11.25f, 7.01f)
                curveTo(11.25f, 7.4242f, 11.5858f, 7.76f, 12.0f, 7.76f)
                curveTo(12.4142f, 7.76f, 12.75f, 7.4242f, 12.75f, 7.01f)
                horizontalLineTo(11.25f)
                close()
                moveTo(12.75f, 7.0f)
                curveTo(12.75f, 6.5858f, 12.4142f, 6.25f, 12.0f, 6.25f)
                curveTo(11.5858f, 6.25f, 11.25f, 6.5858f, 11.25f, 7.0f)
                horizontalLineTo(12.75f)
                close()
                moveTo(12.75f, 17.0f)
                lineTo(12.75f, 10.0f)
                lineTo(11.25f, 10.0f)
                lineTo(11.25f, 17.0f)
                lineTo(12.75f, 17.0f)
                close()
                moveTo(12.75f, 7.01f)
                verticalLineTo(7.0f)
                horizontalLineTo(11.25f)
                verticalLineTo(7.01f)
                horizontalLineTo(12.75f)
                close()
                moveTo(8.0f, 4.75f)
                horizontalLineTo(16.0f)
                verticalLineTo(3.25f)
                horizontalLineTo(8.0f)
                verticalLineTo(4.75f)
                close()
                moveTo(19.25f, 8.0f)
                verticalLineTo(16.0f)
                horizontalLineTo(20.75f)
                verticalLineTo(8.0f)
                horizontalLineTo(19.25f)
                close()
                moveTo(16.0f, 19.25f)
                horizontalLineTo(8.0f)
                verticalLineTo(20.75f)
                horizontalLineTo(16.0f)
                verticalLineTo(19.25f)
                close()
                moveTo(4.75f, 16.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(3.25f)
                verticalLineTo(16.0f)
                horizontalLineTo(4.75f)
                close()
                moveTo(8.0f, 19.25f)
                curveTo(6.2051f, 19.25f, 4.75f, 17.7949f, 4.75f, 16.0f)
                horizontalLineTo(3.25f)
                curveTo(3.25f, 18.6234f, 5.3766f, 20.75f, 8.0f, 20.75f)
                verticalLineTo(19.25f)
                close()
                moveTo(19.25f, 16.0f)
                curveTo(19.25f, 17.7949f, 17.7949f, 19.25f, 16.0f, 19.25f)
                verticalLineTo(20.75f)
                curveTo(18.6234f, 20.75f, 20.75f, 18.6234f, 20.75f, 16.0f)
                horizontalLineTo(19.25f)
                close()
                moveTo(16.0f, 4.75f)
                curveTo(17.7949f, 4.75f, 19.25f, 6.2051f, 19.25f, 8.0f)
                horizontalLineTo(20.75f)
                curveTo(20.75f, 5.3766f, 18.6234f, 3.25f, 16.0f, 3.25f)
                verticalLineTo(4.75f)
                close()
                moveTo(8.0f, 3.25f)
                curveTo(5.3766f, 3.25f, 3.25f, 5.3766f, 3.25f, 8.0f)
                horizontalLineTo(4.75f)
                curveTo(4.75f, 6.2051f, 6.2051f, 4.75f, 8.0f, 4.75f)
                verticalLineTo(3.25f)
                close()
            }
        }
            .build()
        return _icInfo!!
    }

private var _icInfo: ImageVector? = null
