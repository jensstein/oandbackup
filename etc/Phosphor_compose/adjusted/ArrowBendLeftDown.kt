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

val Phosphor.ArrowBendLeftDown: ImageVector
    get() {
        if (_arrow_bend_left_down != null) {
            return _arrow_bend_left_down!!
        }
        _arrow_bend_left_down = Builder(
            name = "Arrow-bend-left-down",
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
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                arcToRelative(88.1f, 88.1f, 0.0f, false, false, -88.0f, 88.0f)
                verticalLineToRelative(76.7f)
                lineToRelative(34.3f, -34.4f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 11.4f)
                lineToRelative(-48.0f, 48.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-48.0f, -48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(96.0f, 204.7f)
                verticalLineTo(128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, true, 200.0f, 24.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 208.0f, 32.0f)
                close()
            }
        }
            .build()
        return _arrow_bend_left_down!!
    }

private var _arrow_bend_left_down: ImageVector? = null



@Preview
@Composable
fun ArrowBendLeftDownPreview() {
    Image(
        Phosphor.ArrowBendLeftDown,
        null
    )
}
