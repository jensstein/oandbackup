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

val Phosphor.TextUnderline: ImageVector
    get() {
        if (_text_underline != null) {
            return _text_underline!!
        }
        _text_underline = Builder(
            name = "Text-underline",
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
                moveTo(224.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(40.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                lineTo(216.0f, 208.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 224.0f, 216.0f)
                close()
                moveTo(128.0f, 192.0f)
                arcToRelative(72.1f, 72.1f, 0.0f, false, false, 72.0f, -72.0f)
                lineTo(200.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(64.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, true, -112.0f, 0.0f)
                lineTo(72.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(64.0f)
                arcTo(72.1f, 72.1f, 0.0f, false, false, 128.0f, 192.0f)
                close()
            }
        }
            .build()
        return _text_underline!!
    }

private var _text_underline: ImageVector? = null



@Preview
@Composable
fun TextUnderlinePreview() {
    Image(
        Phosphor.TextUnderline,
        null
    )
}
