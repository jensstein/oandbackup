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

val Phosphor.HandGrabbing: ImageVector
    get() {
        if (_hand_grabbing != null) {
            return _hand_grabbing!!
        }
        _hand_grabbing = Builder(
            name = "Hand-grabbing",
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
                moveTo(188.0f, 80.0f)
                arcToRelative(27.8f, 27.8f, 0.0f, false, false, -13.4f, 3.4f)
                arcToRelative(28.0f, 28.0f, 0.0f, false, false, -46.6f, -11.0f)
                arcTo(28.0f, 28.0f, 0.0f, false, false, 80.0f, 92.0f)
                verticalLineToRelative(16.0f)
                lineTo(68.0f, 108.0f)
                arcToRelative(28.1f, 28.1f, 0.0f, false, false, -28.0f, 28.0f)
                verticalLineToRelative(16.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, false, false, 176.0f, 0.0f)
                lineTo(216.0f, 108.0f)
                arcTo(28.1f, 28.1f, 0.0f, false, false, 188.0f, 80.0f)
                close()
                moveTo(200.0f, 152.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, false, true, -144.0f, 0.0f)
                lineTo(56.0f, 136.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, 12.0f, -12.0f)
                lineTo(80.0f, 124.0f)
                verticalLineToRelative(24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(96.0f, 92.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, 24.0f, 0.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(136.0f, 92.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, 24.0f, 0.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(176.0f, 108.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, false, true, 24.0f, 0.0f)
                close()
            }
        }
            .build()
        return _hand_grabbing!!
    }

private var _hand_grabbing: ImageVector? = null



@Preview
@Composable
fun HandGrabbingPreview() {
    Image(
        Phosphor.HandGrabbing,
        null
    )
}
