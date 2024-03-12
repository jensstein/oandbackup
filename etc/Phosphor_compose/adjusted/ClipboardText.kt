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

val Phosphor.ClipboardText: ImageVector
    get() {
        if (_clipboard_text != null) {
            return _clipboard_text!!
        }
        _clipboard_text = Builder(
            name = "Clipboard-text",
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
                moveTo(168.0f, 152.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(96.0f, 160.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(64.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 168.0f, 152.0f)
                close()
                moveTo(160.0f, 112.0f)
                lineTo(96.0f, 112.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(216.0f, 48.0f)
                lineTo(216.0f, 216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(56.0f, 232.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(40.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 56.0f, 32.0f)
                lineTo(92.3f, 32.0f)
                arcToRelative(47.8f, 47.8f, 0.0f, false, true, 71.4f, 0.0f)
                lineTo(200.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 216.0f, 48.0f)
                close()
                moveTo(96.0f, 64.0f)
                horizontalLineToRelative(64.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, false, -64.0f, 0.0f)
                close()
                moveTo(200.0f, 48.0f)
                lineTo(173.2f, 48.0f)
                arcTo(47.2f, 47.2f, 0.0f, false, true, 176.0f, 64.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(88.0f, 80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, -8.0f)
                lineTo(80.0f, 64.0f)
                arcToRelative(47.2f, 47.2f, 0.0f, false, true, 2.8f, -16.0f)
                lineTo(56.0f, 48.0f)
                lineTo(56.0f, 216.0f)
                lineTo(200.0f, 216.0f)
                close()
            }
        }
            .build()
        return _clipboard_text!!
    }

private var _clipboard_text: ImageVector? = null



@Preview
@Composable
fun ClipboardTextPreview() {
    Image(
        Phosphor.ClipboardText,
        null
    )
}
