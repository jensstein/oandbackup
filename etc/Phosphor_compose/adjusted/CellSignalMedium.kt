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

val Phosphor.CellSignalMedium: ImageVector
    get() {
        if (_cell_signal_medium != null) {
            return _cell_signal_medium!!
        }
        _cell_signal_medium = Builder(
            name = "Cell-signal-medium",
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
                moveTo(198.1f, 28.5f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 180.7f, 32.0f)
                lineTo(16.0f, 196.7f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 27.3f, 224.0f)
                horizontalLineTo(192.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                verticalLineTo(43.3f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 198.1f, 28.5f)
                close()
                moveTo(112.0f, 123.3f)
                verticalLineTo(208.0f)
                horizontalLineTo(27.3f)
                close()
                moveTo(192.0f, 208.0f)
                horizontalLineTo(128.0f)
                verticalLineTo(107.3f)
                lineToRelative(64.0f, -64.0f)
                close()
            }
        }
            .build()
        return _cell_signal_medium!!
    }

private var _cell_signal_medium: ImageVector? = null



@Preview
@Composable
fun CellSignalMediumPreview() {
    Image(
        Phosphor.CellSignalMedium,
        null
    )
}
