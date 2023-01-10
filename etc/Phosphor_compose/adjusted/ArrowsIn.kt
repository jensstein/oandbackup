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

val Phosphor.ArrowsIn: ImageVector
    get() {
        if (_arrows_in != null) {
            return _arrows_in!!
        }
        _arrows_in = Builder(
            name = "Arrows-in",
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
                moveTo(144.0f, 104.0f)
                lineTo(144.0f, 64.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                lineTo(160.0f, 84.7f)
                lineToRelative(42.3f, -42.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 11.4f)
                lineTo(171.3f, 96.0f)
                lineTo(192.0f, 96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                lineTo(152.0f, 112.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 144.0f, 104.0f)
                close()
                moveTo(104.0f, 144.0f)
                lineTo(64.0f, 144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(84.7f, 160.0f)
                lineTo(42.3f, 202.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineTo(96.0f, 171.3f)
                lineTo(96.0f, 192.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(112.0f, 152.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 104.0f, 144.0f)
                close()
                moveTo(171.3f, 160.0f)
                lineTo(192.0f, 160.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(152.0f, 144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(160.0f, 171.3f)
                lineToRelative(42.3f, 42.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, -11.4f)
                close()
                moveTo(104.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                lineTo(96.0f, 84.7f)
                lineTo(53.7f, 42.3f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 42.3f, 53.7f)
                lineTo(84.7f, 96.0f)
                lineTo(64.0f, 96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(112.0f, 64.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 104.0f, 56.0f)
                close()
            }
        }
            .build()
        return _arrows_in!!
    }

private var _arrows_in: ImageVector? = null



@Preview
@Composable
fun ArrowsInPreview() {
    Image(
        Phosphor.ArrowsIn,
        null
    )
}
