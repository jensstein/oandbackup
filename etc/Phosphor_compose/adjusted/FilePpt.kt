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

val Phosphor.FilePpt: ImageVector
    get() {
        if (_file_ppt != null) {
            return _file_ppt!!
        }
        _file_ppt = Builder(
            name = "File-ppt",
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
                moveTo(64.0f, 160.0f)
                lineTo(48.0f, 160.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, 0.0f, -48.0f)
                close()
                moveTo(64.0f, 192.0f)
                lineTo(56.0f, 192.0f)
                lineTo(56.0f, 176.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                close()
                moveTo(128.0f, 160.0f)
                lineTo(112.0f, 160.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(24.0f, 24.0f, 0.0f, false, false, 0.0f, -48.0f)
                close()
                moveTo(128.0f, 192.0f)
                horizontalLineToRelative(-8.0f)
                lineTo(120.0f, 176.0f)
                horizontalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 16.0f)
                close()
                moveTo(216.0f, 168.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(198.0f, 176.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(182.0f, 176.0f)
                lineTo(172.0f, 176.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(36.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 216.0f, 168.0f)
                close()
                moveTo(216.0f, 88.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, -2.4f, -5.7f)
                lineToRelative(-55.9f, -56.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 152.0f, 24.0f)
                lineTo(56.0f, 24.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 40.0f, 40.0f)
                verticalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(56.0f, 40.0f)
                horizontalLineToRelative(88.0f)
                lineTo(144.0f, 88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, 8.0f)
                horizontalLineToRelative(48.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(216.0f, 88.0f)
                close()
                moveTo(160.0f, 51.3f)
                lineTo(188.7f, 80.0f)
                lineTo(160.0f, 80.0f)
                close()
            }
        }
            .build()
        return _file_ppt!!
    }

private var _file_ppt: ImageVector? = null



@Preview
@Composable
fun FilePptPreview() {
    Image(
        Phosphor.FilePpt,
        null
    )
}
