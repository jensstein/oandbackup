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

val Phosphor.`Arrow-circle-down-right`: ImageVector
    get() {
        if (`_arrow-circle-down-right` != null) {
            return `_arrow-circle-down-right`!!
        }
        `_arrow-circle-down-right` = Builder(
            name = "Arrow-circle-down-right", defaultWidth =
            256.0.dp, defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight =
            256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(201.5f, 54.5f)
                arcToRelative(104.0f, 104.0f, 0.0f, true, false, 0.0f, 147.0f)
                arcTo(103.9f, 103.9f, 0.0f, false, false, 201.5f, 54.5f)
                close()
                moveTo(190.2f, 190.2f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 0.0f, -124.4f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 190.2f, 190.2f)
                close()
                moveTo(164.0f, 108.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(108.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(28.7f)
                lineTo(94.3f, 105.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(148.0f, 136.7f)
                verticalLineTo(108.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
            }
        }
            .build()
        return `_arrow-circle-down-right`!!
    }

private var `_arrow-circle-down-right`: ImageVector? = null
