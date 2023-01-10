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

val Phosphor.CirclesThree: ImageVector
    get() {
        if (_circles_three != null) {
            return _circles_three!!
        }
        _circles_three = Builder(
            name = "Circles-three",
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
                moveTo(176.0f, 68.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, false, -48.0f, 48.0f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 176.0f, 68.0f)
                close()
                moveTo(128.0f, 100.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, true, 32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, true, 128.0f, 100.0f)
                close()
                moveTo(188.0f, 124.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, false, 48.0f, 48.0f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 188.0f, 124.0f)
                close()
                moveTo(188.0f, 204.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, true, 32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, true, 188.0f, 204.0f)
                close()
                moveTo(68.0f, 124.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, false, 48.0f, 48.0f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 68.0f, 124.0f)
                close()
                moveTo(68.0f, 204.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, true, 32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, true, 68.0f, 204.0f)
                close()
            }
        }
            .build()
        return _circles_three!!
    }

private var _circles_three: ImageVector? = null



@Preview
@Composable
fun CirclesThreePreview() {
    Image(
        Phosphor.CirclesThree,
        null
    )
}
