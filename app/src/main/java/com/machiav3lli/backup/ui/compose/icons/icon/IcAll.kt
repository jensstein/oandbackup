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

val Icon.IcAll: ImageVector
    get() {
        if (_icAll != null) {
            return _icAll!!
        }
        _icAll = Builder(
            name = "IcAll", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 48.0f, viewportHeight = 48.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 4.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(14.0f, 24.0f)
                lineTo(15.25f, 25.25f)
                moveTo(44.0f, 14.0f)
                lineTo(24.0f, 34.0f)
                lineTo(22.75f, 32.75f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 4.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(4.0f, 24.0f)
                lineTo(14.0f, 34.0f)
                lineTo(34.0f, 14.0f)
            }
        }
            .build()
        return _icAll!!
    }

private var _icAll: ImageVector? = null
