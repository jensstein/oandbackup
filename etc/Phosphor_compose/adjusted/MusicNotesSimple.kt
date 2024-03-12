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

val Phosphor.MusicNotesSimple: ImageVector
    get() {
        if (_music_notes_simple != null) {
            return _music_notes_simple!!
        }
        _music_notes_simple = Builder(
            name = "Music-notes-simple",
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
                moveTo(212.9f, 25.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -6.8f, -1.5f)
                lineToRelative(-128.0f, 32.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 72.0f, 64.0f)
                lineTo(72.0f, 174.1f)
                arcTo(35.3f, 35.3f, 0.0f, false, false, 52.0f, 168.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 36.0f, 36.0f)
                lineTo(88.0f, 70.2f)
                lineToRelative(112.0f, -28.0f)
                verticalLineToRelative(99.9f)
                arcToRelative(35.3f, 35.3f, 0.0f, false, false, -20.0f, -6.1f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 36.0f, 36.0f)
                lineTo(216.0f, 32.0f)
                arcTo(7.8f, 7.8f, 0.0f, false, false, 212.9f, 25.7f)
                close()
                moveTo(52.0f, 224.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, 20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 52.0f, 224.0f)
                close()
                moveTo(180.0f, 192.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, 20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 180.0f, 192.0f)
                close()
            }
        }
            .build()
        return _music_notes_simple!!
    }

private var _music_notes_simple: ImageVector? = null



@Preview
@Composable
fun MusicNotesSimplePreview() {
    Image(
        Phosphor.MusicNotesSimple,
        null
    )
}
