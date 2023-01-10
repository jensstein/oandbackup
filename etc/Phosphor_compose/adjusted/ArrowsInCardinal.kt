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

val Phosphor.ArrowsInCardinal: ImageVector
    get() {
        if (_arrows_in_cardinal != null) {
            return _arrows_in_cardinal!!
        }
        _arrows_in_cardinal = Builder(
            name = "Arrows-in-cardinal",
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
                moveTo(161.9f, 182.6f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.0f, 11.3f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -5.6f, 2.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.7f, -2.4f)
                lineTo(136.0f, 179.3f)
                lineTo(136.0f, 232.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(120.0f, 179.3f)
                lineToRelative(-14.6f, 14.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -11.3f, -11.3f)
                lineToRelative(28.2f, -28.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 0.0f)
                close()
                moveTo(122.3f, 101.7f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineToRelative(28.2f, -28.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -11.3f, -11.3f)
                lineTo(136.0f, 76.7f)
                lineTo(136.0f, 24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                lineTo(120.0f, 76.7f)
                lineTo(105.4f, 62.1f)
                arcTo(8.0f, 8.0f, 0.0f, true, false, 94.1f, 73.4f)
                close()
                moveTo(101.7f, 122.3f)
                lineTo(73.4f, 94.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -11.3f, 11.3f)
                lineTo(76.7f, 120.0f)
                lineTo(24.0f, 120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(76.7f, 136.0f)
                lineTo(62.1f, 150.6f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 0.0f, 11.3f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, 5.6f, 2.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 5.7f, -2.4f)
                lineToRelative(28.3f, -28.2f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 101.7f, 122.3f)
                close()
                moveTo(232.0f, 120.0f)
                lineTo(179.3f, 120.0f)
                lineToRelative(14.6f, -14.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -11.3f, -11.3f)
                lineToRelative(-28.3f, 28.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                lineToRelative(28.3f, 28.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 5.7f, 2.4f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, 5.6f, -2.4f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 0.0f, -11.3f)
                lineTo(179.3f, 136.0f)
                lineTo(232.0f, 136.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
            }
        }
            .build()
        return _arrows_in_cardinal!!
    }

private var _arrows_in_cardinal: ImageVector? = null



@Preview
@Composable
fun ArrowsInCardinalPreview() {
    Image(
        Phosphor.ArrowsInCardinal,
        null
    )
}
