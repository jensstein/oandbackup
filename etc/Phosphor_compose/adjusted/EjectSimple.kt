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

val Phosphor.EjectSimple: ImageVector
    get() {
        if (_eject_simple != null) {
            return _eject_simple!!
        }
        _eject_simple = Builder(
            name = "Eject-simple",
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
                moveTo(40.8f, 168.0f)
                horizontalLineTo(215.2f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 12.4f, -26.1f)
                lineTo(140.4f, 34.6f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -24.8f, 0.0f)
                lineTo(28.4f, 141.9f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 40.8f, 168.0f)
                close()
                moveTo(128.0f, 44.7f)
                lineTo(215.2f, 152.0f)
                horizontalLineTo(40.8f)
                close()
                moveTo(232.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineTo(224.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 232.0f, 208.0f)
                close()
            }
        }
            .build()
        return _eject_simple!!
    }

private var _eject_simple: ImageVector? = null



@Preview
@Composable
fun EjectSimplePreview() {
    Image(
        Phosphor.EjectSimple,
        null
    )
}
