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

val Phosphor.BluetoothConnected: ImageVector
    get() {
        if (_bluetooth_connected != null) {
            return _bluetooth_connected!!
        }
        _bluetooth_connected = Builder(
            name = "Bluetooth-connected",
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
                moveTo(188.8f, 169.6f)
                lineTo(133.3f, 128.0f)
                lineToRelative(55.5f, -41.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -12.8f)
                lineToRelative(-64.0f, -48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.4f, -0.7f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 112.0f, 32.0f)
                verticalLineToRelative(80.0f)
                lineTo(60.8f, 73.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -9.6f, 12.8f)
                lineTo(106.7f, 128.0f)
                lineTo(51.2f, 169.6f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 56.0f, 184.0f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, 4.8f, -1.6f)
                lineTo(112.0f, 144.0f)
                verticalLineToRelative(80.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 4.4f, 7.2f)
                arcToRelative(9.4f, 9.4f, 0.0f, false, false, 3.6f, 0.8f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, false, 4.8f, -1.6f)
                lineToRelative(64.0f, -48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -12.8f)
                close()
                moveTo(128.0f, 48.0f)
                lineToRelative(42.7f, 32.0f)
                lineTo(128.0f, 112.0f)
                close()
                moveTo(128.0f, 208.0f)
                lineTo(128.0f, 144.0f)
                lineToRelative(42.7f, 32.0f)
                close()
                moveTo(208.0f, 128.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 208.0f, 128.0f)
                close()
                moveTo(52.0f, 140.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, 12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 52.0f, 140.0f)
                close()
            }
        }
            .build()
        return _bluetooth_connected!!
    }

private var _bluetooth_connected: ImageVector? = null



@Preview
@Composable
fun BluetoothConnectedPreview() {
    Image(
        Phosphor.BluetoothConnected,
        null
    )
}
