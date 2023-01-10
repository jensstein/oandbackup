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

val Phosphor.`Traffic-signal`: ImageVector
    get() {
        if (`_traffic-signal` != null) {
            return `_traffic-signal`!!
        }
        `_traffic-signal` = Builder(
            name = "Traffic-signal", defaultWidth = 256.0.dp, defaultHeight
            = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(128.0f, 56.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, false, 32.0f, 32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 128.0f, 56.0f)
                close()
                moveTo(128.0f, 104.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 16.0f, -16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 128.0f, 104.0f)
                close()
                moveTo(128.0f, 136.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, false, 32.0f, 32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 128.0f, 136.0f)
                close()
                moveTo(128.0f, 184.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 16.0f, -16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 128.0f, 184.0f)
                close()
                moveTo(216.0f, 144.0f)
                lineTo(200.0f, 144.0f)
                lineTo(200.0f, 80.0f)
                horizontalLineToRelative(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(200.0f, 64.0f)
                lineTo(200.0f, 40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(72.0f, 24.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 56.0f, 40.0f)
                lineTo(56.0f, 64.0f)
                lineTo(40.0f, 64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(56.0f, 80.0f)
                verticalLineToRelative(64.0f)
                lineTo(40.0f, 144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(56.0f, 160.0f)
                verticalLineToRelative(56.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(184.0f, 232.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(200.0f, 160.0f)
                horizontalLineToRelative(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(184.0f, 216.0f)
                lineTo(72.0f, 216.0f)
                lineTo(72.0f, 40.0f)
                lineTo(184.0f, 40.0f)
                lineTo(184.0f, 216.0f)
                close()
            }
        }
            .build()
        return `_traffic-signal`!!
    }

private var `_traffic-signal`: ImageVector? = null
