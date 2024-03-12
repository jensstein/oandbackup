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

val Phosphor.Lightbulb: ImageVector
    get() {
        if (_lightbulb != null) {
            return _lightbulb!!
        }
        _lightbulb = Builder(
            name = "Lightbulb",
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
                moveTo(176.0f, 232.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(88.0f, 240.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(80.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 176.0f, 232.0f)
                close()
                moveTo(216.0f, 104.0f)
                arcToRelative(87.7f, 87.7f, 0.0f, false, true, -33.6f, 69.2f)
                arcTo(16.1f, 16.1f, 0.0f, false, false, 176.0f, 186.0f)
                verticalLineToRelative(6.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(96.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineToRelative(-6.0f)
                arcToRelative(16.2f, 16.2f, 0.0f, false, false, -6.2f, -12.7f)
                arcTo(87.8f, 87.8f, 0.0f, false, true, 40.0f, 104.5f)
                curveTo(39.7f, 56.8f, 78.3f, 17.1f, 125.9f, 16.0f)
                arcTo(87.9f, 87.9f, 0.0f, false, true, 216.0f, 104.0f)
                close()
                moveTo(200.0f, 104.0f)
                arcToRelative(72.1f, 72.1f, 0.0f, false, false, -73.7f, -72.0f)
                curveToRelative(-39.0f, 0.9f, -70.5f, 33.4f, -70.3f, 72.4f)
                arcToRelative(71.7f, 71.7f, 0.0f, false, false, 27.6f, 56.3f)
                arcTo(32.0f, 32.0f, 0.0f, false, true, 96.0f, 186.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(64.0f)
                verticalLineToRelative(-6.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, true, 12.5f, -25.4f)
                arcTo(71.5f, 71.5f, 0.0f, false, false, 200.0f, 104.0f)
                close()
                moveTo(183.2f, 94.6f)
                arcToRelative(55.8f, 55.8f, 0.0f, false, false, -45.8f, -45.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -2.6f, 15.8f)
                arcToRelative(39.7f, 39.7f, 0.0f, false, true, 32.6f, 32.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.9f, 6.6f)
                horizontalLineToRelative(1.4f)
                arcTo(7.9f, 7.9f, 0.0f, false, false, 183.2f, 94.6f)
                close()
            }
        }
            .build()
        return _lightbulb!!
    }

private var _lightbulb: ImageVector? = null



@Preview
@Composable
fun LightbulbPreview() {
    Image(
        Phosphor.Lightbulb,
        null
    )
}
