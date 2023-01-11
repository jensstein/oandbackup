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

val Phosphor.AnchorSimple: ImageVector
    get() {
        if (_anchor_simple != null) {
            return _anchor_simple!!
        }
        _anchor_simple = Builder(
            name = "Anchor-simple",
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
                moveTo(224.0f, 112.0f)
                horizontalLineTo(200.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(15.6f)
                arcTo(88.0f, 88.0f, 0.0f, false, true, 136.0f, 207.6f)
                verticalLineTo(95.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, false, -16.0f, 0.0f)
                verticalLineTo(207.6f)
                arcTo(88.0f, 88.0f, 0.0f, false, true, 40.4f, 128.0f)
                horizontalLineTo(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                arcToRelative(104.0f, 104.0f, 0.0f, false, false, 208.0f, 0.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 224.0f, 112.0f)
                close()
                moveTo(112.0f, 64.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 16.0f, 16.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 112.0f, 64.0f)
                close()
            }
        }
            .build()
        return _anchor_simple!!
    }

private var _anchor_simple: ImageVector? = null



@Preview
@Composable
fun AnchorSimplePreview() {
    Image(
        Phosphor.AnchorSimple,
        null
    )
}
