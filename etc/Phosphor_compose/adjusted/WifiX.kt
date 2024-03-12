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

val Phosphor.WifiX: ImageVector
    get() {
        if (_wifi_x != null) {
            return _wifi_x!!
        }
        _wifi_x = Builder(
            name = "Wifi-x",
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
                moveTo(168.6f, 160.3f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, 0.2f, 11.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -5.7f, 2.5f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -5.6f, -2.3f)
                arcToRelative(42.8f, 42.8f, 0.0f, false, false, -59.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -11.1f, -11.5f)
                arcToRelative(58.7f, 58.7f, 0.0f, false, true, 81.2f, 0.0f)
                close()
                moveTo(211.3f, 80.0f)
                lineToRelative(18.4f, -18.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, -11.4f)
                lineTo(200.0f, 68.7f)
                lineTo(181.7f, 50.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 11.4f)
                lineTo(188.7f, 80.0f)
                lineTo(170.3f, 98.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineTo(200.0f, 91.3f)
                lineToRelative(18.3f, 18.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, -11.4f)
                close()
                moveTo(143.1f, 64.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 1.8f, -15.9f)
                arcTo(159.5f, 159.5f, 0.0f, false, false, 128.0f, 48.0f)
                arcTo(153.4f, 153.4f, 0.0f, false, false, 19.4f, 92.5f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 11.3f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 5.7f, 2.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 5.6f, -2.3f)
                arcTo(137.5f, 137.5f, 0.0f, false, true, 128.0f, 64.0f)
                arcTo(149.6f, 149.6f, 0.0f, false, true, 143.1f, 64.8f)
                close()
                moveTo(142.7f, 113.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 2.6f, -15.8f)
                arcTo(111.1f, 111.1f, 0.0f, false, false, 128.0f, 96.0f)
                arcToRelative(105.6f, 105.6f, 0.0f, false, false, -74.6f, 30.4f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -0.1f, 11.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 11.3f, 0.1f)
                arcToRelative(90.8f, 90.8f, 0.0f, false, true, 78.1f, -24.6f)
                close()
                moveTo(128.0f, 188.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 128.0f, 188.0f)
                close()
            }
        }
            .build()
        return _wifi_x!!
    }

private var _wifi_x: ImageVector? = null



@Preview
@Composable
fun WifiXPreview() {
    Image(
        Phosphor.WifiX,
        null
    )
}
