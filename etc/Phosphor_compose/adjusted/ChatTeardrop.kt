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

val Phosphor.ChatTeardrop: ImageVector
    get() {
        if (_chat_teardrop != null) {
            return _chat_teardrop!!
        }
        _chat_teardrop = Builder(
            name = "Chat-teardrop",
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
                moveTo(132.0f, 224.0f)
                horizontalLineTo(47.7f)
                arcTo(15.7f, 15.7f, 0.0f, false, true, 32.0f, 208.3f)
                verticalLineTo(124.0f)
                arcTo(100.0f, 100.0f, 0.0f, true, true, 132.0f, 224.0f)
                close()
                moveTo(48.0f, 208.0f)
                horizontalLineToRelative(84.0f)
                arcToRelative(84.0f, 84.0f, 0.0f, true, false, -84.0f, -84.0f)
                close()
            }
        }
            .build()
        return _chat_teardrop!!
    }

private var _chat_teardrop: ImageVector? = null



@Preview
@Composable
fun ChatTeardropPreview() {
    Image(
        Phosphor.ChatTeardrop,
        null
    )
}
