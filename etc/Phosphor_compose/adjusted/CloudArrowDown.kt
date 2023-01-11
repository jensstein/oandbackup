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

val Phosphor.CloudArrowDown: ImageVector
    get() {
        if (_cloud_arrow_down != null) {
            return _cloud_arrow_down!!
        }
        _cloud_arrow_down = Builder(
            name = "Cloud-arrow-down",
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
                moveTo(191.6f, 168.4f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 11.3f)
                lineToRelative(-33.9f, 34.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-33.9f, -34.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, -11.3f)
                lineTo(144.0f, 188.7f)
                verticalLineTo(128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(60.7f)
                lineToRelative(20.3f, -20.3f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 191.6f, 168.4f)
                close()
                moveTo(160.0f, 40.0f)
                arcTo(88.0f, 88.0f, 0.0f, false, false, 81.3f, 88.7f)
                arcTo(58.2f, 58.2f, 0.0f, false, false, 72.0f, 88.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, false, false, 0.0f, 128.0f)
                horizontalLineTo(96.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(72.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, false, true, 0.0f, -96.0f)
                horizontalLineToRelative(3.3f)
                arcTo(85.7f, 85.7f, 0.0f, false, false, 72.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                arcToRelative(72.0f, 72.0f, 0.0f, true, true, 129.6f, 43.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 1.6f, 11.2f)
                arcTo(7.7f, 7.7f, 0.0f, false, false, 224.0f, 184.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.4f, -3.2f)
                arcTo(88.0f, 88.0f, 0.0f, false, false, 160.0f, 40.0f)
                close()
            }
        }
            .build()
        return _cloud_arrow_down!!
    }

private var _cloud_arrow_down: ImageVector? = null



@Preview
@Composable
fun CloudArrowDownPreview() {
    Image(
        Phosphor.CloudArrowDown,
        null
    )
}
