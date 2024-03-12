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

val Phosphor.ChartBarHorizontal: ImageVector
    get() {
        if (_chart_bar_horizontal != null) {
            return _chart_bar_horizontal!!
        }
        _chart_bar_horizontal = Builder(
            name = "Chart-bar-horizontal",
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
                moveTo(216.0f, 96.0f)
                lineTo(176.0f, 96.0f)
                lineTo(176.0f, 56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                lineTo(48.0f, 48.0f)
                lineTo(48.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                lineTo(32.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(144.0f, 160.0f)
                horizontalLineToRelative(72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(224.0f, 104.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 216.0f, 96.0f)
                close()
                moveTo(160.0f, 64.0f)
                lineTo(160.0f, 96.0f)
                lineTo(48.0f, 96.0f)
                lineTo(48.0f, 64.0f)
                close()
                moveTo(128.0f, 192.0f)
                lineTo(48.0f, 192.0f)
                lineTo(48.0f, 160.0f)
                horizontalLineToRelative(80.0f)
                close()
                moveTo(208.0f, 144.0f)
                lineTo(48.0f, 144.0f)
                lineTo(48.0f, 112.0f)
                lineTo(208.0f, 112.0f)
                close()
            }
        }
            .build()
        return _chart_bar_horizontal!!
    }

private var _chart_bar_horizontal: ImageVector? = null



@Preview
@Composable
fun ChartBarHorizontalPreview() {
    Image(
        Phosphor.ChartBarHorizontal,
        null
    )
}
