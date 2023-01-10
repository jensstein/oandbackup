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

val Phosphor.SpeakerSimpleNone: ImageVector
    get() {
        if (_speaker_simple_none != null) {
            return _speaker_simple_none!!
        }
        _speaker_simple_none = Builder(
            name = "Speaker-simple-none",
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
                moveTo(152.0f, 232.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -4.9f, -1.7f)
                lineTo(77.3f, 176.0f)
                horizontalLineTo(32.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineTo(96.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 32.0f, 80.0f)
                horizontalLineTo(77.3f)
                lineToRelative(69.8f, -54.3f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 160.0f, 32.0f)
                verticalLineTo(224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -4.5f, 7.2f)
                arcTo(8.5f, 8.5f, 0.0f, false, true, 152.0f, 232.0f)
                close()
                moveTo(32.0f, 96.0f)
                verticalLineToRelative(64.0f)
                horizontalLineTo(80.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 4.9f, 1.7f)
                lineTo(144.0f, 207.6f)
                verticalLineTo(48.4f)
                lineTo(84.9f, 94.3f)
                arcTo(7.9f, 7.9f, 0.0f, false, true, 80.0f, 96.0f)
                close()
            }
        }
            .build()
        return _speaker_simple_none!!
    }

private var _speaker_simple_none: ImageVector? = null



@Preview
@Composable
fun SpeakerSimpleNonePreview() {
    Image(
        Phosphor.SpeakerSimpleNone,
        null
    )
}
