package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcTelegram: ImageVector
    get() {
        if (_icTelegram != null) {
            return _icTelegram!!
        }
        _icTelegram = Builder(
            name = "IcTelegram", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
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
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 0.768251f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(15.5129f, 6.7912f)
                lineTo(5.781f, 10.6327f)
                lineTo(9.3664f, 11.1449f)
                moveTo(15.5129f, 6.7912f)
                lineTo(14.2324f, 14.4742f)
                lineTo(9.3664f, 11.1449f)
                moveTo(15.5129f, 6.7912f)
                lineTo(9.3664f, 11.1449f)
                moveToRelative(0.0f, 0.0f)
                verticalLineToRelative(2.8171f)
                lineToRelative(1.664f, -1.6785f)
            }
        }
            .build()
        return _icTelegram!!
    }

private var _icTelegram: ImageVector? = null
