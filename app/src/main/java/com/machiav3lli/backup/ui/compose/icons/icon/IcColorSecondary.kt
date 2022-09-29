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

val Icon.IcColorSecondary: ImageVector
    get() {
        if (_icColorSecondary != null) {
            return _icColorSecondary!!
        }
        _icColorSecondary = Builder(
            name = "IcColorSecondary", defaultWidth = 32.0.dp, defaultHeight
            = 32.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveToRelative(20.1148f, 14.4764f)
                curveToRelative(-0.092f, 0.065f, -1.4321f, 1.5606f, -1.4183f, 2.9642f)
                curveToRelative(0.0114f, 1.1617f, 0.5264f, 1.7281f, 1.3971f, 1.8724f)
                curveToRelative(0.7718f, -0.0912f, 1.4846f, -0.4432f, 1.4893f, -1.7926f)
                curveToRelative(0.0056f, -1.6037f, -1.376f, -2.979f, -1.468f, -3.044f)
                close()
                moveTo(3.414f, 12.998f)
                curveToRelative(0.0f, 0.534f, 0.208f, 1.036f, 0.586f, 1.414f)
                lineToRelative(5.586f, 5.586f)
                curveToRelative(0.378f, 0.378f, 0.88f, 0.586f, 1.414f, 0.586f)
                curveToRelative(0.534f, 0.0f, 1.036f, -0.208f, 1.414f, -0.586f)
                lineToRelative(6.0469f, -5.9762f)
                curveTo(19.1899f, 13.3013f, 19.263f, 13.084f, 18.707f, 12.291f)
                lineTo(11.0f, 4.584f)
                lineTo(9.1078f, 2.7278f)
                curveTo(8.5114f, 2.7336f, 8.0346f, 2.8893f, 8.1265f, 3.7576f)
                lineTo(10.0187f, 5.6138f)
                lineTo(4.0f, 11.584f)
                curveToRelative(-0.3795f, 0.3765f, -0.586f, 0.88f, -0.586f, 1.414f)
                close()
                moveTo(11.0432f, 6.5584f)
                lineTo(17.5372f, 13.0707f)
                lineTo(11.053f, 19.4269f)
                horizontalLineToRelative(0.001f)
                horizontalLineToRelative(-0.001f)
                lineTo(4.559f, 12.9146f)
                close()
            }
        }
            .build()
        return _icColorSecondary!!
    }

private var _icColorSecondary: ImageVector? = null
