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

val Phosphor.ChatTeardropDots: ImageVector
    get() {
        if (_chat_teardrop_dots != null) {
            return _chat_teardrop_dots!!
        }
        _chat_teardrop_dots = Builder(
            name = "Chat-teardrop-dots",
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
                moveTo(132.0f, 24.0f)
                arcTo(100.2f, 100.2f, 0.0f, false, false, 32.0f, 124.0f)
                verticalLineToRelative(84.3f)
                arcTo(15.7f, 15.7f, 0.0f, false, false, 47.7f, 224.0f)
                lineTo(132.0f, 224.0f)
                arcToRelative(100.0f, 100.0f, 0.0f, false, false, 0.0f, -200.0f)
                close()
                moveTo(132.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 124.0f)
                arcToRelative(84.0f, 84.0f, 0.0f, true, true, 84.0f, 84.0f)
                close()
                moveTo(144.0f, 128.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 144.0f, 128.0f)
                close()
                moveTo(96.0f, 128.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 96.0f, 128.0f)
                close()
                moveTo(192.0f, 128.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 192.0f, 128.0f)
                close()
            }
        }
            .build()
        return _chat_teardrop_dots!!
    }

private var _chat_teardrop_dots: ImageVector? = null



@Preview
@Composable
fun ChatTeardropDotsPreview() {
    Image(
        Phosphor.ChatTeardropDots,
        null
    )
}
