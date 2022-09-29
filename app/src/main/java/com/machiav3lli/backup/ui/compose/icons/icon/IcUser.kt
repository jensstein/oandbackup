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

val Icon.IcUser: ImageVector
    get() {
        if (_icUser != null) {
            return _icUser!!
        }
        _icUser = Builder(
            name = "IcUser", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(9.2001f, 16.6331f)
                lineTo(14.833f, 16.6331f)
                lineTo(14.833f, 14.7945f)
                lineTo(9.2001f, 14.7945f)
                close()
                moveTo(14.833f, 16.6331f)
                curveToRelative(1.9986f, 0.0f, 3.6188f, 1.6202f, 3.6188f, 3.6188f)
                horizontalLineToRelative(1.8386f)
                curveToRelative(0.0f, -3.0141f, -2.4433f, -5.4575f, -5.4575f, -5.4575f)
                close()
                moveTo(9.2001f, 14.7945f)
                curveToRelative(-3.0141f, 0.0f, -5.4574f, 2.4433f, -5.4574f, 5.4575f)
                horizontalLineToRelative(1.8386f)
                curveToRelative(0.0f, -1.9986f, 1.6202f, -3.6188f, 3.6188f, -3.6188f)
                close()
                moveTo(14.7746f, 8.3592f)
                curveToRelative(0.0f, 1.5231f, -1.2348f, 2.758f, -2.758f, 2.758f)
                verticalLineToRelative(1.8386f)
                curveToRelative(2.5387f, 0.0f, 4.5966f, -2.0579f, 4.5966f, -4.5966f)
                close()
                moveTo(12.0166f, 11.1172f)
                curveToRelative(-1.5231f, 0.0f, -2.758f, -1.2348f, -2.758f, -2.758f)
                lineTo(7.42f, 8.3592f)
                curveToRelative(0.0f, 2.5387f, 2.058f, 4.5966f, 4.5966f, 4.5966f)
                close()
                moveTo(9.2586f, 8.3592f)
                curveToRelative(0.0f, -1.5232f, 1.2348f, -2.758f, 2.758f, -2.758f)
                lineTo(12.0166f, 3.7626f)
                curveToRelative(-2.5386f, 0.0f, -4.5966f, 2.058f, -4.5966f, 4.5966f)
                close()
                moveTo(12.0166f, 5.6013f)
                curveToRelative(1.5231f, 0.0f, 2.758f, 1.2348f, 2.758f, 2.758f)
                horizontalLineToRelative(1.8386f)
                curveToRelative(0.0f, -2.5386f, -2.0579f, -4.5966f, -4.5966f, -4.5966f)
                close()
                moveTo(22.1291f, 12.0365f)
                curveToRelative(0.0f, 5.5849f, -4.5276f, 10.1125f, -10.1125f, 10.1125f)
                verticalLineToRelative(1.8386f)
                curveToRelative(6.6005f, 0.0f, 11.9512f, -5.3507f, 11.9512f, -11.9512f)
                close()
                moveTo(12.0166f, 22.1491f)
                curveToRelative(-5.585f, 0.0f, -10.1125f, -4.5276f, -10.1125f, -10.1125f)
                lineTo(0.0654f, 12.0365f)
                curveToRelative(0.0f, 6.6005f, 5.3507f, 11.9512f, 11.9512f, 11.9512f)
                close()
                moveTo(1.9041f, 12.0365f)
                curveToRelative(0.0f, -5.585f, 4.5275f, -10.1125f, 10.1125f, -10.1125f)
                lineTo(12.0166f, 0.0853f)
                curveTo(5.4161f, 0.0853f, 0.0654f, 5.4361f, 0.0654f, 12.0365f)
                close()
                moveTo(12.0166f, 1.924f)
                curveToRelative(5.5849f, 0.0f, 10.1125f, 4.5275f, 10.1125f, 10.1125f)
                horizontalLineToRelative(1.8386f)
                curveToRelative(0.0f, -6.6005f, -5.3507f, -11.9512f, -11.9512f, -11.9512f)
                close()
            }
        }
            .build()
        return _icUser!!
    }

private var _icUser: ImageVector? = null
