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

val Phosphor.ArrowSquareDownLeft: ImageVector
    get() {
        if (_arrow_square_down_left != null) {
            return _arrow_square_down_left!!
        }
        _arrow_square_down_left = Builder(
            name = "Arrow-square-down-left",
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
                moveTo(208.0f, 32.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 32.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 48.0f)
                lineTo(208.0f, 48.0f)
                close()
                moveTo(92.0f, 156.0f)
                lineTo(92.0f, 108.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(28.7f)
                lineToRelative(42.3f, -42.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 11.4f)
                lineTo(119.3f, 148.0f)
                lineTo(148.0f, 148.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                lineTo(100.0f, 164.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 92.0f, 156.0f)
                close()
            }
        }
            .build()
        return _arrow_square_down_left!!
    }

private var _arrow_square_down_left: ImageVector? = null



@Preview
@Composable
fun ArrowSquareDownLeftPreview() {
    Image(
        Phosphor.ArrowSquareDownLeft,
        null
    )
}
