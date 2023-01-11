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

val Phosphor.PaintRoller: ImageVector
    get() {
        if (_paint_roller != null) {
            return _paint_roller!!
        }
        _paint_roller = Builder(
            name = "Paint-roller",
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
                moveTo(232.0f, 88.0f)
                lineTo(216.0f, 88.0f)
                lineTo(216.0f, 64.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(48.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 64.0f)
                lineTo(32.0f, 88.0f)
                lineTo(16.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(32.0f, 104.0f)
                verticalLineToRelative(24.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(200.0f, 144.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(216.0f, 104.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(50.0f)
                lineTo(131.6f, 182.6f)
                arcTo(16.2f, 16.2f, 0.0f, false, false, 120.0f, 198.0f)
                verticalLineToRelative(34.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(136.0f, 198.0f)
                lineToRelative(100.4f, -28.6f)
                arcTo(16.2f, 16.2f, 0.0f, false, false, 248.0f, 154.0f)
                lineTo(248.0f, 104.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 232.0f, 88.0f)
                close()
                moveTo(200.0f, 128.0f)
                lineTo(48.0f, 128.0f)
                lineTo(48.0f, 64.0f)
                lineTo(200.0f, 64.0f)
                lineTo(200.0f, 95.9f)
                horizontalLineToRelative(0.0f)
                lineTo(200.0f, 128.0f)
                close()
            }
        }
            .build()
        return _paint_roller!!
    }

private var _paint_roller: ImageVector? = null



@Preview
@Composable
fun PaintRollerPreview() {
    Image(
        Phosphor.PaintRoller,
        null
    )
}
