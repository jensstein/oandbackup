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

val Icon.IcPrefsUser: ImageVector
    get() {
        if (_icPrefsUser != null) {
            return _icPrefsUser!!
        }
        _icPrefsUser = Builder(
            name = "IcPrefsUser", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(20.0f, 21.0f)
                verticalLineTo(19.0f)
                curveTo(20.0f, 16.7909f, 18.2091f, 15.0f, 16.0f, 15.0f)
                horizontalLineTo(8.0f)
                curveTo(5.7909f, 15.0f, 4.0f, 16.7909f, 4.0f, 19.0f)
                verticalLineTo(21.0f)
                moveTo(16.0f, 7.0f)
                curveTo(16.0f, 9.2091f, 14.2091f, 11.0f, 12.0f, 11.0f)
                curveTo(9.7909f, 11.0f, 8.0f, 9.2091f, 8.0f, 7.0f)
                curveTo(8.0f, 4.7909f, 9.7909f, 3.0f, 12.0f, 3.0f)
                curveTo(14.2091f, 3.0f, 16.0f, 4.7909f, 16.0f, 7.0f)
                close()
            }
        }
            .build()
        return _icPrefsUser!!
    }

private var _icPrefsUser: ImageVector? = null
