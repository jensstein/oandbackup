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

val Icon.IcFolder: ImageVector
    get() {
        if (_icFolder != null) {
            return _icFolder!!
        }
        _icFolder = Builder(
            name = "IcFolder", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 481.2f, viewportHeight = 481.2f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(403.9f, 97.85f)
                horizontalLineToRelative(-114.0f)
                curveToRelative(-27.7f, 0.0f, -50.3f, -22.5f, -50.3f, -50.3f)
                curveToRelative(0.0f, -7.5f, -6.0f, -13.5f, -13.5f, -13.5f)
                horizontalLineTo(77.3f)
                curveToRelative(-42.6f, 0.0f, -77.3f, 34.7f, -77.3f, 77.3f)
                verticalLineToRelative(258.5f)
                curveToRelative(0.0f, 42.6f, 34.7f, 77.3f, 77.3f, 77.3f)
                horizontalLineToRelative(326.6f)
                curveToRelative(42.6f, 0.0f, 77.3f, -34.7f, 77.3f, -77.3f)
                verticalLineToRelative(-194.8f)
                curveTo(481.2f, 132.45f, 446.5f, 97.85f, 403.9f, 97.85f)
                close()
                moveTo(454.2f, 369.75f)
                curveToRelative(0.0f, 27.7f, -22.5f, 50.3f, -50.3f, 50.3f)
                horizontalLineTo(77.3f)
                curveToRelative(-27.7f, 0.0f, -50.3f, -22.5f, -50.3f, -50.3f)
                verticalLineToRelative(-258.4f)
                curveToRelative(0.0f, -27.7f, 22.5f, -50.3f, 50.3f, -50.3f)
                horizontalLineToRelative(136.5f)
                curveToRelative(6.4f, 36.2f, 38.1f, 63.8f, 76.1f, 63.8f)
                horizontalLineToRelative(114.0f)
                curveToRelative(27.7f, 0.0f, 50.3f, 22.5f, 50.3f, 50.3f)
                lineTo(454.2f, 369.75f)
                lineTo(454.2f, 369.75f)
                close()
            }
        }
            .build()
        return _icFolder!!
    }

private var _icFolder: ImageVector? = null
