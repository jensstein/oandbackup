package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcElement: ImageVector
    get() {
        if (_icElement != null) {
            return _icElement!!
        }
        _icElement = Builder(
            name = "IcElement", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 21.0f, viewportHeight = 21.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(6.8647f, 3.8212f)
                lineTo(14.1847f, 3.8212f)
                lineTo(14.1847f, 2.4487f)
                lineTo(6.8647f, 2.4487f)
                close()
                moveTo(17.1584f, 6.7949f)
                lineTo(17.1584f, 14.1149f)
                lineTo(18.5309f, 14.1149f)
                lineTo(18.5309f, 6.7949f)
                close()
                moveTo(14.1847f, 17.0887f)
                lineTo(6.8647f, 17.0887f)
                verticalLineToRelative(1.3725f)
                lineTo(14.1847f, 18.4612f)
                close()
                moveTo(3.891f, 14.1149f)
                lineTo(3.891f, 6.7949f)
                lineTo(2.5185f, 6.7949f)
                verticalLineToRelative(7.32f)
                close()
                moveTo(6.8647f, 17.0887f)
                curveToRelative(-1.6424f, 0.0f, -2.9737f, -1.3314f, -2.9737f, -2.9738f)
                lineTo(2.5185f, 14.1149f)
                curveToRelative(0.0f, 2.4004f, 1.9459f, 4.3462f, 4.3462f, 4.3462f)
                close()
                moveTo(17.1584f, 14.1149f)
                curveToRelative(0.0f, 1.6423f, -1.3314f, 2.9738f, -2.9738f, 2.9738f)
                verticalLineToRelative(1.3725f)
                curveToRelative(2.4004f, 0.0f, 4.3462f, -1.9458f, 4.3462f, -4.3462f)
                close()
                moveTo(14.1847f, 3.8212f)
                curveToRelative(1.6423f, 0.0f, 2.9738f, 1.3314f, 2.9738f, 2.9737f)
                horizontalLineToRelative(1.3725f)
                curveToRelative(0.0f, -2.4004f, -1.9458f, -4.3462f, -4.3462f, -4.3462f)
                close()
                moveTo(6.8647f, 2.4487f)
                curveToRelative(-2.4004f, 0.0f, -4.3462f, 1.9459f, -4.3462f, 4.3462f)
                horizontalLineToRelative(1.3725f)
                curveToRelative(0.0f, -1.6424f, 1.3314f, -2.9737f, 2.9737f, -2.9737f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = EvenOdd
            ) {
                moveToRelative(9.1231f, 6.0274f)
                curveToRelative(0.0f, -0.3314f, 0.2686f, -0.6f, 0.6f, -0.6f)
                curveToRelative(2.2091f, 0.0f, 4.0f, 1.7909f, 4.0f, 4.0f)
                curveToRelative(0.0f, 0.3314f, -0.2686f, 0.6f, -0.6f, 0.6f)
                curveToRelative(-0.3314f, 0.0f, -0.6f, -0.2686f, -0.6f, -0.6f)
                curveToRelative(0.0f, -1.5464f, -1.2536f, -2.8f, -2.8f, -2.8f)
                curveToRelative(-0.3314f, 0.0f, -0.6f, -0.2686f, -0.6f, -0.6f)
                close()
                moveTo(11.9231f, 14.8274f)
                curveToRelative(0.0f, 0.3314f, -0.2686f, 0.6f, -0.6f, 0.6f)
                curveToRelative(-2.2091f, 0.0f, -4.0f, -1.7909f, -4.0f, -4.0f)
                curveToRelative(0.0f, -0.3314f, 0.2686f, -0.6f, 0.6f, -0.6f)
                curveToRelative(0.3314f, 0.0f, 0.6f, 0.2686f, 0.6f, 0.6f)
                curveToRelative(0.0f, 1.5464f, 1.2536f, 2.8f, 2.8f, 2.8f)
                curveToRelative(0.3314f, 0.0f, 0.6f, 0.2686f, 0.6f, 0.6f)
                close()
                moveTo(6.1231f, 11.8274f)
                curveToRelative(-0.3314f, 0.0f, -0.6f, -0.2686f, -0.6f, -0.6f)
                curveToRelative(0.0f, -2.2091f, 1.7909f, -4.0f, 4.0f, -4.0f)
                curveToRelative(0.3314f, 0.0f, 0.6f, 0.2686f, 0.6f, 0.6f)
                curveToRelative(0.0f, 0.3314f, -0.2686f, 0.6f, -0.6f, 0.6f)
                curveToRelative(-1.5464f, 0.0f, -2.8f, 1.2536f, -2.8f, 2.8f)
                curveToRelative(0.0f, 0.3314f, -0.2686f, 0.6f, -0.6f, 0.6f)
                close()
                moveTo(14.9231f, 9.0274f)
                curveToRelative(0.3314f, 0.0f, 0.6f, 0.2686f, 0.6f, 0.6f)
                curveToRelative(0.0f, 2.2091f, -1.7909f, 4.0f, -4.0f, 4.0f)
                curveToRelative(-0.3314f, 0.0f, -0.6f, -0.2686f, -0.6f, -0.6f)
                curveToRelative(0.0f, -0.3314f, 0.2686f, -0.6f, 0.6f, -0.6f)
                curveToRelative(1.5464f, 0.0f, 2.8f, -1.2536f, 2.8f, -2.8f)
                curveToRelative(0.0f, -0.3314f, 0.2686f, -0.6f, 0.6f, -0.6f)
                close()
            }
        }
            .build()
        return _icElement!!
    }

private var _icElement: ImageVector? = null
