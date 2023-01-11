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

val Phosphor.PersonSimple: ImageVector
    get() {
        if (_person_simple != null) {
            return _person_simple!!
        }
        _person_simple = Builder(
            name = "Person-simple",
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
                moveTo(127.9f, 80.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, false, -32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, false, 127.9f, 80.0f)
                close()
                moveTo(127.9f, 32.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, -16.0f, 16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 127.9f, 32.0f)
                close()
                moveTo(230.9f, 132.1f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 224.0f, 136.0f)
                arcToRelative(8.7f, 8.7f, 0.0f, false, true, -4.1f, -1.1f)
                curveToRelative(-0.4f, -0.3f, -35.1f, -20.6f, -83.9f, -22.7f)
                lineTo(136.0f, 149.0f)
                lineToRelative(62.0f, 69.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -0.7f, 11.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -5.3f, 2.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -6.0f, -2.7f)
                lineTo(128.0f, 164.0f)
                lineTo(70.0f, 229.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -12.0f, -10.6f)
                lineTo(120.0f, 149.0f)
                lineTo(120.0f, 112.2f)
                curveToRelative(-49.0f, 2.1f, -83.5f, 22.4f, -83.9f, 22.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.2f, -13.8f)
                curveTo(29.6f, 120.1f, 70.4f, 96.0f, 128.0f, 96.0f)
                reflectiveCurveToRelative(98.4f, 24.1f, 100.1f, 25.1f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 230.9f, 132.1f)
                close()
            }
        }
            .build()
        return _person_simple!!
    }

private var _person_simple: ImageVector? = null



@Preview
@Composable
fun PersonSimplePreview() {
    Image(
        Phosphor.PersonSimple,
        null
    )
}
