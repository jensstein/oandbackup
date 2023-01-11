package com.machiav3lli.backup.ui.compose.icons.phosphor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Phosphor

val Phosphor.`Gender-female`: ImageVector
    get() {
        if (`_gender-female` != null) {
            return `_gender-female`!!
        }
        `_gender-female` = Builder(
            name = "Gender-female", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(208.0f, 96.0f)
                arcToRelative(80.0f, 80.0f, 0.0f, true, false, -88.0f, 79.6f)
                verticalLineTo(200.0f)
                horizontalLineTo(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(32.0f)
                verticalLineToRelative(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(216.0f)
                horizontalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(136.0f)
                verticalLineTo(175.6f)
                arcTo(80.1f, 80.1f, 0.0f, false, false, 208.0f, 96.0f)
                close()
                moveTo(64.0f, 96.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, true, true, 64.0f, 64.0f)
                arcTo(64.1f, 64.1f, 0.0f, false, true, 64.0f, 96.0f)
                close()
            }
        }
            .build()
        return `_gender-female`!!
    }

private var `_gender-female`: ImageVector? = null
