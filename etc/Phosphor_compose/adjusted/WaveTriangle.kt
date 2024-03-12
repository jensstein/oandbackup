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

val Phosphor.WaveTriangle: ImageVector
    get() {
        if (_wave_triangle != null) {
            return _wave_triangle!!
        }
        _wave_triangle = Builder(
            name = "Wave-triangle",
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
                moveTo(238.5f, 132.7f)
                lineToRelative(-52.0f, 72.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -13.0f, 0.0f)
                lineTo(76.0f, 69.7f)
                lineToRelative(-45.5f, 63.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -13.0f, -9.4f)
                lineToRelative(52.0f, -72.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 13.0f, 0.0f)
                lineToRelative(97.5f, 135.0f)
                lineToRelative(45.5f, -63.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 13.0f, 9.4f)
                close()
            }
        }
            .build()
        return _wave_triangle!!
    }

private var _wave_triangle: ImageVector? = null



@Preview
@Composable
fun WaveTrianglePreview() {
    Image(
        Phosphor.WaveTriangle,
        null
    )
}
