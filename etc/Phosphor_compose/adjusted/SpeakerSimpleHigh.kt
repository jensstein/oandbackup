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

val Phosphor.SpeakerSimpleHigh: ImageVector
    get() {
        if (_speaker_simple_high != null) {
            return _speaker_simple_high!!
        }
        _speaker_simple_high = Builder(
            name = "Speaker-simple-high",
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
                moveTo(155.5f, 24.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.4f, 0.9f)
                lineTo(77.3f, 80.0f)
                lineTo(32.0f, 80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 16.0f, 96.0f)
                verticalLineToRelative(64.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(77.3f, 176.0f)
                lineToRelative(69.8f, 54.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 152.0f, 232.0f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, false, 3.5f, -0.8f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 160.0f, 224.0f)
                lineTo(160.0f, 32.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 155.5f, 24.8f)
                close()
                moveTo(144.0f, 207.6f)
                lineTo(84.9f, 161.7f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 80.0f, 160.0f)
                lineTo(32.0f, 160.0f)
                lineTo(32.0f, 96.0f)
                lineTo(80.0f, 96.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 4.9f, -1.7f)
                lineTo(144.0f, 48.4f)
                close()
                moveTo(200.0f, 104.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(184.0f, 104.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
                moveTo(232.0f, 88.0f)
                verticalLineToRelative(80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(216.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
            }
        }
            .build()
        return _speaker_simple_high!!
    }

private var _speaker_simple_high: ImageVector? = null



@Preview
@Composable
fun SpeakerSimpleHighPreview() {
    Image(
        Phosphor.SpeakerSimpleHigh,
        null
    )
}
