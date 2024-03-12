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

val Phosphor.ArrowsVertical: ImageVector
    get() {
        if (_arrows_vertical != null) {
            return _arrows_vertical!!
        }
        _arrows_vertical = Builder(
            name = "Arrows-vertical",
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
                moveTo(165.7f, 194.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                lineToRelative(-32.0f, 32.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-32.0f, -32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, -11.4f)
                lineTo(120.0f, 212.7f)
                verticalLineTo(43.3f)
                lineTo(101.7f, 61.7f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 90.3f, 50.3f)
                lineToRelative(32.0f, -32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 11.4f, 0.0f)
                lineToRelative(32.0f, 32.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineTo(136.0f, 43.3f)
                verticalLineTo(212.7f)
                lineToRelative(18.3f, -18.4f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 165.7f, 194.3f)
                close()
            }
        }
            .build()
        return _arrows_vertical!!
    }

private var _arrows_vertical: ImageVector? = null



@Preview
@Composable
fun ArrowsVerticalPreview() {
    Image(
        Phosphor.ArrowsVertical,
        null
    )
}
