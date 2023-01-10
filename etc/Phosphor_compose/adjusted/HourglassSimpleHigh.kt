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

val Phosphor.HourglassSimpleHigh: ImageVector
    get() {
        if (_hourglass_simple_high != null) {
            return _hourglass_simple_high!!
        }
        _hourglass_simple_high = Builder(
            name = "Hourglass-simple-high",
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
                moveTo(139.3f, 128.0f)
                lineToRelative(50.4f, -50.3f)
                horizontalLineToRelative(0.0f)
                lineTo(208.0f, 59.3f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 196.7f, 32.0f)
                lineTo(59.3f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 48.0f, 59.3f)
                lineTo(116.7f, 128.0f)
                lineTo(48.0f, 196.7f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 59.3f, 224.0f)
                lineTo(196.7f, 224.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 196.7f)
                close()
                moveTo(196.7f, 48.0f)
                lineTo(180.7f, 64.0f)
                lineTo(75.3f, 64.0f)
                lineToRelative(-16.0f, -16.0f)
                close()
                moveTo(91.3f, 80.0f)
                horizontalLineToRelative(73.4f)
                lineTo(128.0f, 116.7f)
                close()
                moveTo(59.3f, 208.0f)
                lineTo(128.0f, 139.3f)
                lineTo(196.7f, 208.0f)
                close()
            }
        }
            .build()
        return _hourglass_simple_high!!
    }

private var _hourglass_simple_high: ImageVector? = null



@Preview
@Composable
fun HourglassSimpleHighPreview() {
    Image(
        Phosphor.HourglassSimpleHigh,
        null
    )
}
