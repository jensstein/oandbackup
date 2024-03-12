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

val Phosphor.`Projector-screen`: ImageVector
    get() {
        if (`_projector-screen` != null) {
            return `_projector-screen`!!
        }
        `_projector-screen` = Builder(
            name = "Projector-screen", defaultWidth = 256.0.dp,
            defaultHeight = 256.0.dp, viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(232.0f, 64.0f)
                verticalLineTo(48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                horizontalLineTo(40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 24.0f, 48.0f)
                verticalLineTo(64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 40.0f, 80.0f)
                verticalLineToRelative(96.0f)
                horizontalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(88.0f)
                verticalLineToRelative(17.4f)
                arcToRelative(24.0f, 24.0f, 0.0f, true, false, 16.0f, 0.0f)
                verticalLineTo(192.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineTo(80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 232.0f, 64.0f)
                close()
                moveTo(128.0f, 240.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 8.0f, -8.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 128.0f, 240.0f)
                close()
                moveTo(40.0f, 48.0f)
                horizontalLineTo(216.0f)
                verticalLineTo(64.0f)
                horizontalLineTo(40.0f)
                close()
                moveTo(200.0f, 176.0f)
                horizontalLineTo(56.0f)
                verticalLineTo(80.0f)
                horizontalLineTo(200.0f)
                close()
            }
        }
            .build()
        return `_projector-screen`!!
    }

private var `_projector-screen`: ImageVector? = null
