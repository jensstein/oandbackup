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

val Phosphor.NumberSquareThree: ImageVector
    get() {
        if (_number_square_three != null) {
            return _number_square_three!!
        }
        _number_square_three = Builder(
            name = "Number-square-three",
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
                moveTo(149.5f, 126.5f)
                arcToRelative(36.1f, 36.1f, 0.0f, false, true, -51.0f, 51.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                arcTo(19.9f, 19.9f, 0.0f, true, false, 124.0f, 132.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -7.1f, -4.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.5f, -8.3f)
                lineTo(136.6f, 92.0f)
                horizontalLineTo(104.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 7.1f, 4.3f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, true, -0.6f, 8.3f)
                lineToRelative(-21.0f, 30.0f)
                arcTo(37.9f, 37.9f, 0.0f, false, true, 149.5f, 126.5f)
                close()
                moveTo(224.0f, 48.0f)
                verticalLineTo(208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                horizontalLineTo(48.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineTo(48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 48.0f, 32.0f)
                horizontalLineTo(208.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 224.0f, 48.0f)
                close()
                moveTo(208.0f, 208.0f)
                verticalLineTo(48.0f)
                horizontalLineTo(48.0f)
                verticalLineTo(208.0f)
                close()
            }
        }
            .build()
        return _number_square_three!!
    }

private var _number_square_three: ImageVector? = null



@Preview
@Composable
fun NumberSquareThreePreview() {
    Image(
        Phosphor.NumberSquareThree,
        null
    )
}
