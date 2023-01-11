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

val Phosphor.HourglassSimpleLow: ImageVector
    get() {
        if (_hourglass_simple_low != null) {
            return _hourglass_simple_low!!
        }
        _hourglass_simple_low = Builder(
            name = "Hourglass-simple-low",
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
                lineTo(208.0f, 59.3f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 196.7f, 32.0f)
                lineTo(59.3f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 48.0f, 59.3f)
                lineTo(116.7f, 128.0f)
                lineTo(74.3f, 170.3f)
                horizontalLineToRelative(0.0f)
                lineTo(48.0f, 196.7f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 59.3f, 224.0f)
                lineTo(196.7f, 224.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 196.7f)
                close()
                moveTo(59.3f, 48.0f)
                lineTo(196.7f, 48.0f)
                lineTo(128.0f, 116.7f)
                close()
                moveTo(128.0f, 139.3f)
                lineTo(156.7f, 168.0f)
                lineTo(99.3f, 168.0f)
                close()
                moveTo(59.3f, 208.0f)
                lineToRelative(24.0f, -24.0f)
                horizontalLineToRelative(89.4f)
                lineToRelative(24.0f, 24.0f)
                close()
            }
        }
            .build()
        return _hourglass_simple_low!!
    }

private var _hourglass_simple_low: ImageVector? = null



@Preview
@Composable
fun HourglassSimpleLowPreview() {
    Image(
        Phosphor.HourglassSimpleLow,
        null
    )
}
