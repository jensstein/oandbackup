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

val Phosphor.BatteryWarningVertical: ImageVector
    get() {
        if (_battery_warning_vertical != null) {
            return _battery_warning_vertical!!
        }
        _battery_warning_vertical = Builder(
            name = "Battery-warning-vertical",
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
                moveTo(120.0f, 132.0f)
                lineTo(120.0f, 92.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                close()
                moveTo(96.0f, 16.0f)
                horizontalLineToRelative(64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(96.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                close()
                moveTo(208.0f, 56.0f)
                lineTo(208.0f, 208.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, true, -24.0f, 24.0f)
                lineTo(72.0f, 232.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, true, -24.0f, -24.0f)
                lineTo(48.0f, 56.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 72.0f, 32.0f)
                lineTo(184.0f, 32.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, true, 208.0f, 56.0f)
                close()
                moveTo(192.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                lineTo(72.0f, 48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                lineTo(64.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                lineTo(184.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                close()
                moveTo(128.0f, 156.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 128.0f, 156.0f)
                close()
            }
        }
            .build()
        return _battery_warning_vertical!!
    }

private var _battery_warning_vertical: ImageVector? = null



@Preview
@Composable
fun BatteryWarningVerticalPreview() {
    Image(
        Phosphor.BatteryWarningVertical,
        null
    )
}
