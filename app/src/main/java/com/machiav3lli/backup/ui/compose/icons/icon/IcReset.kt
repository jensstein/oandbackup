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

val Icon.IcReset: ImageVector
    get() {
        if (_icReset != null) {
            return _icReset!!
        }
        _icReset = Builder(
            name = "IcReset", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(3.0f, 9.0f)
                verticalLineTo(15.0f)
                moveTo(3.0f, 15.0f)
                horizontalLineTo(9.0f)
                moveTo(3.0f, 15.0f)
                lineTo(5.6403f, 12.6307f)
                curveTo(7.0213f, 11.2521f, 8.813f, 10.3596f, 10.7453f, 10.0878f)
                curveTo(12.6777f, 9.8159f, 14.6461f, 10.1794f, 16.3539f, 11.1234f)
                curveTo(18.0617f, 12.0675f, 19.4164f, 13.5409f, 20.2139f, 15.3218f)
            }
        }
            .build()
        return _icReset!!
    }

private var _icReset: ImageVector? = null
