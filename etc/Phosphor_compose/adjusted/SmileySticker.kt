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

val Phosphor.SmileySticker: ImageVector
    get() {
        if (_smiley_sticker != null) {
            return _smiley_sticker!!
        }
        _smiley_sticker = Builder(
            name = "Smiley-sticker",
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
                arcToRelative(104.0f, 104.0f, 0.0f, false, false, 0.0f, 208.0f)
                arcToRelative(102.2f, 102.2f, 0.0f, false, false, 30.6f, -4.6f)
                arcToRelative(6.7f, 6.7f, 0.0f, false, false, 3.3f, -2.0f)
                lineToRelative(63.5f, -63.5f)
                arcToRelative(7.2f, 7.2f, 0.0f, false, false, 2.0f, -3.3f)
                arcTo(102.2f, 102.2f, 0.0f, false, false, 232.0f, 128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(212.7f, 152.0f)
                lineTo(152.0f, 212.7f)
                arcTo(87.9f, 87.9f, 0.0f, true, true, 212.7f, 152.0f)
                close()
                moveTo(80.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 80.0f, 108.0f)
                close()
                moveTo(152.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 152.0f, 108.0f)
                close()
                moveTo(176.5f, 156.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, true, -97.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 13.8f, -8.0f)
                arcToRelative(40.1f, 40.1f, 0.0f, false, false, 69.4f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 13.8f, 8.0f)
                close()
            }
        }
            .build()
        return _smiley_sticker!!
    }

private var _smiley_sticker: ImageVector? = null



@Preview
@Composable
fun SmileyStickerPreview() {
    Image(
        Phosphor.SmileySticker,
        null
    )
}
