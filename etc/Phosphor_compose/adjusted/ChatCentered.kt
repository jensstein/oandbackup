package com.machiav3lli.backup.ui.compose.icons.phosphor


import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

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

val Phosphor.ChatCentered: ImageVector
    get() {
        if (_chat_centered != null) {
            return _chat_centered!!
        }
        _chat_centered = Builder(
            name = "Chat-centered",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(128.0f, 232.4f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, true, -13.7f, -7.7f)
                lineTo(99.5f, 200.0f)
                horizontalLineTo(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineTo(56.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 40.0f, 40.0f)
                horizontalLineTo(216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                verticalLineTo(184.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                horizontalLineTo(156.5f)
                lineToRelative(-6.8f, -4.1f)
                lineToRelative(6.8f, 4.1f)
                lineToRelative(-14.8f, 24.7f)
                arcTo(15.9f, 15.9f, 0.0f, false, true, 128.0f, 232.4f)
                close()
                moveTo(40.0f, 56.0f)
                verticalLineTo(184.0f)
                horizontalLineTo(99.5f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, true, 13.7f, 7.8f)
                lineTo(128.0f, 216.4f)
                lineToRelative(14.8f, -24.6f)
                horizontalLineToRelative(0.0f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, true, 13.7f, -7.8f)
                horizontalLineTo(216.0f)
                verticalLineTo(56.0f)
                close()
            }
        }
            .build()
        return _chat_centered!!
    }

private var _chat_centered: ImageVector? = null



@Preview
@Composable
fun ChatCenteredPreview() {
    Image(
        Phosphor.ChatCentered,
        null
    )
}
