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

val Phosphor.Hexagon: ImageVector
    get() {
        if (_hexagon != null) {
            return _hexagon!!
        }
        _hexagon = Builder(
            name = "Hexagon",
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
                moveTo(128.0f, 238.8f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, true, -7.9f, -2.1f)
                lineToRelative(-84.0f, -47.4f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, true, -8.1f, -14.0f)
                lineTo(28.0f, 80.7f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, true, 8.1f, -14.0f)
                lineToRelative(84.0f, -47.4f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, true, 15.8f, 0.0f)
                lineToRelative(84.0f, 47.4f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, true, 8.1f, 14.0f)
                verticalLineToRelative(94.6f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, true, -8.1f, 14.0f)
                lineToRelative(-84.0f, 47.4f)
                arcTo(16.1f, 16.1f, 0.0f, false, true, 128.0f, 238.8f)
                close()
                moveTo(128.0f, 33.2f)
                lineTo(44.0f, 80.7f)
                verticalLineToRelative(94.6f)
                lineToRelative(84.0f, 47.5f)
                lineToRelative(84.0f, -47.5f)
                lineTo(212.0f, 80.7f)
                close()
            }
        }
            .build()
        return _hexagon!!
    }

private var _hexagon: ImageVector? = null



@Preview
@Composable
fun HexagonPreview() {
    Image(
        Phosphor.Hexagon,
        null
    )
}
