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

val Phosphor.ChatCircle: ImageVector
    get() {
        if (_chat_circle != null) {
            return _chat_circle!!
        }
        _chat_circle = Builder(
            name = "Chat-circle",
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
                moveTo(128.0f, 232.0f)
                arcToRelative(103.6f, 103.6f, 0.0f, false, true, -50.0f, -12.8f)
                lineToRelative(-30.0f, 8.5f)
                arcTo(15.9f, 15.9f, 0.0f, false, true, 28.3f, 208.0f)
                lineToRelative(8.5f, -30.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, true, 128.0f, 232.0f)
                close()
                moveTo(79.0f, 202.6f)
                arcToRelative(8.7f, 8.7f, 0.0f, false, true, 4.1f, 1.1f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, false, -30.8f, -30.8f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, 0.8f, 6.3f)
                lineToRelative(-9.5f, 33.2f)
                lineToRelative(33.2f, -9.5f)
                arcTo(8.3f, 8.3f, 0.0f, false, true, 79.0f, 202.6f)
                close()
            }
        }
            .build()
        return _chat_circle!!
    }

private var _chat_circle: ImageVector? = null



@Preview
@Composable
fun ChatCirclePreview() {
    Image(
        Phosphor.ChatCircle,
        null
    )
}
