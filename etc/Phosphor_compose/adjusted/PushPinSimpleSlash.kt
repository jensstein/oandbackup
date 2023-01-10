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

val Phosphor.PushPinSimpleSlash: ImageVector
    get() {
        if (_push_pin_simple_slash != null) {
            return _push_pin_simple_slash!!
        }
        _push_pin_simple_slash = Builder(
            name = "Push-pin-simple-slash",
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
                moveTo(83.2f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, -8.0f)
                horizontalLineTo(192.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                horizontalLineToRelative(-6.5f)
                lineToRelative(18.8f, 106.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -6.5f, 9.3f)
                horizontalLineToRelative(-1.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -7.9f, -6.6f)
                lineTo(169.3f, 48.0f)
                horizontalLineTo(91.2f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 83.2f, 40.0f)
                close()
                moveTo(213.4f, 221.9f)
                arcTo(8.2f, 8.2f, 0.0f, false, true, 208.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.9f, -2.6f)
                lineToRelative(-34.0f, -37.4f)
                horizontalLineTo(136.0f)
                verticalLineToRelative(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                verticalLineTo(184.0f)
                horizontalLineTo(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(9.3f)
                lineTo(66.2f, 72.0f)
                lineTo(42.1f, 45.4f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 53.9f, 34.6f)
                lineToRelative(160.0f, 176.0f)
                arcTo(7.9f, 7.9f, 0.0f, false, true, 213.4f, 221.9f)
                close()
                moveTo(153.6f, 168.0f)
                lineTo(79.8f, 86.9f)
                lineTo(65.5f, 168.0f)
                close()
            }
        }
            .build()
        return _push_pin_simple_slash!!
    }

private var _push_pin_simple_slash: ImageVector? = null



@Preview
@Composable
fun PushPinSimpleSlashPreview() {
    Image(
        Phosphor.PushPinSimpleSlash,
        null
    )
}
