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

val Phosphor.UmbrellaSimple: ImageVector
    get() {
        if (_umbrella_simple != null) {
            return _umbrella_simple!!
        }
        _umbrella_simple = Builder(
            name = "Umbrella-simple",
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
                moveTo(239.6f, 126.6f)
                arcTo(111.9f, 111.9f, 0.0f, false, false, 52.0f, 53.8f)
                arcToRelative(110.9f, 110.9f, 0.0f, false, false, -35.6f, 72.8f)
                arcToRelative(15.7f, 15.7f, 0.0f, false, false, 4.2f, 12.2f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 32.3f, 144.0f)
                horizontalLineTo(120.0f)
                verticalLineToRelative(56.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, false, 64.0f, 0.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -32.0f, 0.0f)
                verticalLineTo(144.0f)
                horizontalLineToRelative(87.7f)
                arcToRelative(15.9f, 15.9f, 0.0f, false, false, 11.7f, -5.2f)
                arcTo(15.7f, 15.7f, 0.0f, false, false, 239.6f, 126.6f)
                close()
                moveTo(32.3f, 128.0f)
                horizontalLineToRelative(0.0f)
                arcToRelative(96.0f, 96.0f, 0.0f, false, true, 191.4f, 0.0f)
                close()
            }
        }
            .build()
        return _umbrella_simple!!
    }

private var _umbrella_simple: ImageVector? = null



@Preview
@Composable
fun UmbrellaSimplePreview() {
    Image(
        Phosphor.UmbrellaSimple,
        null
    )
}
