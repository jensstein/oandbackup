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

val Phosphor.ArrowCircleUp: ImageVector
    get() {
        if (_arrow_circle_up != null) {
            return _arrow_circle_up!!
        }
        _arrow_circle_up = Builder(
            name = "Arrow-circle-up",
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
                moveTo(128.0f, 24.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, false, 232.0f, 128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(128.0f, 216.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 88.0f, -88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 128.0f, 216.0f)
                close()
                moveTo(167.6f, 116.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 11.3f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, true, -5.7f, 2.3f)
                arcToRelative(7.6f, 7.6f, 0.0f, false, true, -5.6f, -2.3f)
                lineTo(136.0f, 107.3f)
                lineTo(136.0f, 168.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(120.0f, 107.3f)
                lineTo(99.7f, 127.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -11.3f, -11.3f)
                lineToRelative(33.9f, -34.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, 11.4f, 0.0f)
                close()
            }
        }
            .build()
        return _arrow_circle_up!!
    }

private var _arrow_circle_up: ImageVector? = null



@Preview
@Composable
fun ArrowCircleUpPreview() {
    Image(
        Phosphor.ArrowCircleUp,
        null
    )
}
