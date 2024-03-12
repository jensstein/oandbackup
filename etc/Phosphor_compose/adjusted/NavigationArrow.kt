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

val Phosphor.NavigationArrow: ImageVector
    get() {
        if (_navigation_arrow != null) {
            return _navigation_arrow!!
        }
        _navigation_arrow = Builder(
            name = "Navigation-arrow",
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
                moveTo(103.5f, 230.3f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, true, -15.1f, -10.8f)
                lineTo(29.8f, 50.2f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 50.2f, 29.8f)
                lineTo(219.5f, 88.4f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, true, 10.8f, 15.4f)
                arcToRelative(15.8f, 15.8f, 0.0f, false, true, -11.3f, 15.0f)
                lineToRelative(-76.6f, 23.6f)
                lineTo(118.8f, 219.0f)
                arcToRelative(15.6f, 15.6f, 0.0f, false, true, -15.0f, 11.2f)
                close()
                moveTo(45.0f, 44.9f)
                horizontalLineToRelative(0.0f)
                lineToRelative(58.6f, 169.4f)
                horizontalLineToRelative(0.0f)
                lineToRelative(23.5f, -76.6f)
                arcToRelative(16.1f, 16.1f, 0.0f, false, true, 10.6f, -10.6f)
                lineToRelative(76.6f, -23.5f)
                close()
                moveTo(45.0f, 44.9f)
                close()
            }
        }
            .build()
        return _navigation_arrow!!
    }

private var _navigation_arrow: ImageVector? = null



@Preview
@Composable
fun NavigationArrowPreview() {
    Image(
        Phosphor.NavigationArrow,
        null
    )
}
