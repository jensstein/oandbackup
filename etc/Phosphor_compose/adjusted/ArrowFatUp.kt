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

val Phosphor.ArrowFatUp: ImageVector
    get() {
        if (_arrow_fat_up != null) {
            return _arrow_fat_up!!
        }
        _arrow_fat_up = Builder(
            name = "Arrow-fat-up",
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
                moveTo(229.7f, 114.3f)
                lineToRelative(-96.0f, -96.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 0.0f)
                lineToRelative(-96.0f, 96.0f)
                arcToRelative(8.4f, 8.4f, 0.0f, false, false, -1.7f, 8.8f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 32.0f, 128.0f)
                horizontalLineTo(72.0f)
                verticalLineToRelative(80.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineToRelative(80.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                verticalLineTo(128.0f)
                horizontalLineToRelative(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.4f, -4.9f)
                arcTo(8.4f, 8.4f, 0.0f, false, false, 229.7f, 114.3f)
                close()
                moveTo(176.0f, 112.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(88.0f)
                horizontalLineTo(88.0f)
                verticalLineTo(120.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, -8.0f)
                horizontalLineTo(51.3f)
                lineTo(128.0f, 35.3f)
                lineTo(204.7f, 112.0f)
                close()
            }
        }
            .build()
        return _arrow_fat_up!!
    }

private var _arrow_fat_up: ImageVector? = null



@Preview
@Composable
fun ArrowFatUpPreview() {
    Image(
        Phosphor.ArrowFatUp,
        null
    )
}
