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

val Phosphor.DeviceTablet: ImageVector
    get() {
        if (_device_tablet != null) {
            return _device_tablet!!
        }
        _device_tablet = Builder(
            name = "Device-tablet",
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
                moveTo(192.0f, 24.0f)
                lineTo(64.0f, 24.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, false, 40.0f, 48.0f)
                lineTo(40.0f, 208.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, 24.0f)
                lineTo(192.0f, 232.0f)
                arcToRelative(24.1f, 24.1f, 0.0f, false, false, 24.0f, -24.0f)
                lineTo(216.0f, 48.0f)
                arcTo(24.1f, 24.1f, 0.0f, false, false, 192.0f, 24.0f)
                close()
                moveTo(56.0f, 72.0f)
                lineTo(200.0f, 72.0f)
                lineTo(200.0f, 184.0f)
                lineTo(56.0f, 184.0f)
                close()
                moveTo(64.0f, 40.0f)
                lineTo(192.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, 8.0f)
                verticalLineToRelative(8.0f)
                lineTo(56.0f, 56.0f)
                lineTo(56.0f, 48.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 64.0f, 40.0f)
                close()
                moveTo(192.0f, 216.0f)
                lineTo(64.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, -8.0f)
                verticalLineToRelative(-8.0f)
                lineTo(200.0f, 200.0f)
                verticalLineToRelative(8.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 192.0f, 216.0f)
                close()
            }
        }
            .build()
        return _device_tablet!!
    }

private var _device_tablet: ImageVector? = null



@Preview
@Composable
fun DeviceTabletPreview() {
    Image(
        Phosphor.DeviceTablet,
        null
    )
}
