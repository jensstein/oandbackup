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

val Phosphor.TextBolder: ImageVector
    get() {
        if (_text_bolder != null) {
            return _text_bolder!!
        }
        _text_bolder = Builder(
            name = "Text-bolder",
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
                moveTo(170.5f, 115.7f)
                arcTo(44.0f, 44.0f, 0.0f, false, false, 140.0f, 40.0f)
                lineTo(64.0f, 40.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -8.0f, 8.0f)
                lineTo(56.0f, 200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, 18.5f, -92.3f)
                close()
                moveTo(72.0f, 56.0f)
                horizontalLineToRelative(68.0f)
                arcToRelative(28.0f, 28.0f, 0.0f, false, true, 0.0f, 56.0f)
                lineTo(72.0f, 112.0f)
                close()
                moveTo(152.0f, 192.0f)
                lineTo(72.0f, 192.0f)
                lineTo(72.0f, 128.0f)
                horizontalLineToRelative(80.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 0.0f, 64.0f)
                close()
            }
        }
            .build()
        return _text_bolder!!
    }

private var _text_bolder: ImageVector? = null



@Preview
@Composable
fun TextBolderPreview() {
    Image(
        Phosphor.TextBolder,
        null
    )
}
