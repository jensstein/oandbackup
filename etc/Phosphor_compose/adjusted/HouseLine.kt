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

val Phosphor.HouseLine: ImageVector
    get() {
        if (_house_line != null) {
            return _house_line!!
        }
        _house_line = Builder(
            name = "House-line",
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
                moveTo(240.0f, 208.0f)
                horizontalLineTo(224.0f)
                verticalLineTo(115.5f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -5.2f, -11.8f)
                lineTo(138.8f, 31.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -21.6f, 0.0f)
                lineToRelative(-80.0f, 72.7f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 115.5f)
                verticalLineTo(208.0f)
                horizontalLineTo(16.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineTo(240.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(48.0f, 115.5f)
                lineToRelative(80.0f, -72.7f)
                lineToRelative(80.0f, 72.7f)
                verticalLineTo(208.0f)
                horizontalLineTo(160.0f)
                verticalLineTo(160.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                horizontalLineTo(112.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, 16.0f)
                verticalLineToRelative(48.0f)
                horizontalLineTo(48.0f)
                close()
                moveTo(144.0f, 208.0f)
                horizontalLineTo(112.0f)
                verticalLineTo(160.0f)
                horizontalLineToRelative(32.0f)
                close()
            }
        }
            .build()
        return _house_line!!
    }

private var _house_line: ImageVector? = null



@Preview
@Composable
fun HouseLinePreview() {
    Image(
        Phosphor.HouseLine,
        null
    )
}
