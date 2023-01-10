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

val Phosphor.ArrowUUpRight: ImageVector
    get() {
        if (_arrow_u_up_right != null) {
            return _arrow_u_up_right!!
        }
        _arrow_u_up_right = Builder(
            name = "Arrow-u-up-right",
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
                moveTo(170.3f, 130.3f)
                lineTo(204.7f, 96.0f)
                horizontalLineTo(88.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, false, 0.0f, 96.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                horizontalLineTo(88.0f)
                arcTo(64.0f, 64.0f, 0.0f, false, true, 88.0f, 80.0f)
                horizontalLineTo(204.7f)
                lineTo(170.3f, 45.7f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineToRelative(48.0f, 48.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                lineToRelative(-48.0f, 48.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 170.3f, 130.3f)
                close()
            }
        }
            .build()
        return _arrow_u_up_right!!
    }

private var _arrow_u_up_right: ImageVector? = null



@Preview
@Composable
fun ArrowUUpRightPreview() {
    Image(
        Phosphor.ArrowUUpRight,
        null
    )
}
