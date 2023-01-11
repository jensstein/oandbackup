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

val Phosphor.DownloadSimple: ImageVector
    get() {
        if (_download_simple != null) {
            return _download_simple!!
        }
        _download_simple = Builder(
            name = "Download-simple",
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
                moveTo(80.3f, 115.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.4f, -11.3f)
                lineTo(120.0f, 132.7f)
                verticalLineTo(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                verticalLineToRelative(92.7f)
                lineToRelative(28.3f, -28.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.4f, 11.3f)
                lineToRelative(-42.0f, 42.0f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                close()
                moveTo(216.0f, 144.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(56.0f)
                horizontalLineTo(48.0f)
                verticalLineTo(152.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(56.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineTo(208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                verticalLineTo(152.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 216.0f, 144.0f)
                close()
            }
        }
            .build()
        return _download_simple!!
    }

private var _download_simple: ImageVector? = null



@Preview
@Composable
fun DownloadSimplePreview() {
    Image(
        Phosphor.DownloadSimple,
        null
    )
}
