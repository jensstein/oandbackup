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

val Phosphor.NumberFive: ImageVector
    get() {
        if (_number_five != null) {
            return _number_five!!
        }
        _number_five = Builder(
            name = "Number-five",
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
                moveTo(165.2f, 122.7f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, true, 0.0f, 90.6f)
                arcToRelative(64.5f, 64.5f, 0.0f, false, true, -90.8f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 11.2f, -11.4f)
                arcToRelative(48.4f, 48.4f, 0.0f, false, false, 68.3f, 0.0f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 119.8f, 120.0f)
                arcToRelative(48.3f, 48.3f, 0.0f, false, false, -34.2f, 14.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -13.5f, -7.0f)
                lineTo(87.8f, 30.7f)
                arcTo(7.9f, 7.9f, 0.0f, false, true, 95.7f, 24.0f)
                horizontalLineTo(176.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                horizontalLineTo(102.5f)
                lineTo(91.0f, 110.8f)
                arcToRelative(63.0f, 63.0f, 0.0f, false, true, 28.8f, -6.8f)
                arcTo(64.4f, 64.4f, 0.0f, false, true, 165.2f, 122.7f)
                close()
            }
        }
            .build()
        return _number_five!!
    }

private var _number_five: ImageVector? = null



@Preview
@Composable
fun NumberFivePreview() {
    Image(
        Phosphor.NumberFive,
        null
    )
}
