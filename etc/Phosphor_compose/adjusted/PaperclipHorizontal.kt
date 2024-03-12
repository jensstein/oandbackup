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

val Phosphor.PaperclipHorizontal: ImageVector
    get() {
        if (_paperclip_horizontal != null) {
            return _paperclip_horizontal!!
        }
        _paperclip_horizontal = Builder(
            name = "Paperclip-horizontal",
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
                moveTo(192.0f, 184.0f)
                horizontalLineTo(46.0f)
                curveToRelative(-21.6f, 0.0f, -39.2f, -17.9f, -39.2f, -40.0f)
                reflectiveCurveTo(24.4f, 104.0f, 46.0f, 104.0f)
                horizontalLineTo(190.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, true, 0.0f, 48.0f)
                horizontalLineTo(78.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineTo(190.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(46.0f)
                curveToRelative(-12.8f, 0.0f, -23.2f, 10.8f, -23.2f, 24.0f)
                reflectiveCurveTo(33.2f, 168.0f, 46.0f, 168.0f)
                horizontalLineTo(192.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, false, 0.0f, -80.0f)
                horizontalLineTo(80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineTo(192.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, true, 0.0f, 112.0f)
                close()
            }
        }
            .build()
        return _paperclip_horizontal!!
    }

private var _paperclip_horizontal: ImageVector? = null



@Preview
@Composable
fun PaperclipHorizontalPreview() {
    Image(
        Phosphor.PaperclipHorizontal,
        null
    )
}
