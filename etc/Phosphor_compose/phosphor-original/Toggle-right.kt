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

val Phosphor.`Toggle-right`: ImageVector
    get() {
        if (`_toggle-right` != null) {
            return `_toggle-right`!!
        }
        `_toggle-right` = Builder(
            name = "Toggle-right", defaultWidth = 256.0.dp, defaultHeight =
            256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(176.0f, 56.0f)
                lineTo(80.0f, 56.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, false, false, 0.0f, 144.0f)
                horizontalLineToRelative(96.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, false, false, 0.0f, -144.0f)
                close()
                moveTo(176.0f, 184.0f)
                lineTo(80.0f, 184.0f)
                arcTo(56.0f, 56.0f, 0.0f, false, true, 80.0f, 72.0f)
                horizontalLineToRelative(96.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, true, 0.0f, 112.0f)
                close()
                moveTo(176.0f, 88.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, true, false, 40.0f, 40.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, false, 176.0f, 88.0f)
                close()
                moveTo(176.0f, 152.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, true, 24.0f, -24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 176.0f, 152.0f)
                close()
            }
        }
            .build()
        return `_toggle-right`!!
    }

private var `_toggle-right`: ImageVector? = null
