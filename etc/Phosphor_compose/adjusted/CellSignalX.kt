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

val Phosphor.CellSignalX: ImageVector
    get() {
        if (_cell_signal_x != null) {
            return _cell_signal_x!!
        }
        _cell_signal_x = Builder(
            name = "Cell-signal-x",
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
                moveTo(229.7f, 146.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineTo(200.0f, 139.3f)
                lineToRelative(-18.3f, 18.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, -11.4f)
                lineTo(188.7f, 128.0f)
                lineToRelative(-18.4f, -18.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(200.0f, 116.7f)
                lineToRelative(18.3f, -18.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 11.4f)
                lineTo(211.3f, 128.0f)
                close()
                moveTo(200.0f, 165.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                lineTo(192.0f, 208.0f)
                lineTo(27.3f, 208.0f)
                lineTo(192.0f, 43.3f)
                lineTo(192.0f, 82.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(208.0f, 43.3f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 180.7f, 32.0f)
                lineTo(16.0f, 196.7f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 27.3f, 224.0f)
                lineTo(192.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(208.0f, 173.3f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 200.0f, 165.3f)
                close()
            }
        }
            .build()
        return _cell_signal_x!!
    }

private var _cell_signal_x: ImageVector? = null



@Preview
@Composable
fun CellSignalXPreview() {
    Image(
        Phosphor.CellSignalX,
        null
    )
}
