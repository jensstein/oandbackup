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

val Phosphor.CircleNotch: ImageVector
    get() {
        if (_circle_notch != null) {
            return _circle_notch!!
        }
        _circle_notch = Builder(
            name = "Circle-notch",
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
                moveTo(232.0f, 128.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, true, 84.7f, 33.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 10.6f, 4.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -4.0f, 10.6f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, false, 73.4f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -4.0f, -10.6f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 10.6f, -4.0f)
                arcTo(104.4f, 104.4f, 0.0f, false, true, 232.0f, 128.0f)
                close()
            }
        }
            .build()
        return _circle_notch!!
    }

private var _circle_notch: ImageVector? = null



@Preview
@Composable
fun CircleNotchPreview() {
    Image(
        Phosphor.CircleNotch,
        null
    )
}
