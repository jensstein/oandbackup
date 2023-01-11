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

val Phosphor.FolderSimple: ImageVector
    get() {
        if (_folder_simple != null) {
            return _folder_simple!!
        }
        _folder_simple = Builder(
            name = "Folder-simple",
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
                moveTo(216.9f, 216.0f)
                horizontalLineTo(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                verticalLineTo(64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 40.0f, 48.0f)
                horizontalLineTo(93.3f)
                arcToRelative(15.6f, 15.6f, 0.0f, false, true, 9.6f, 3.2f)
                lineTo(130.7f, 72.0f)
                horizontalLineTo(216.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                verticalLineTo(200.9f)
                arcTo(15.2f, 15.2f, 0.0f, false, true, 216.9f, 216.0f)
                close()
                moveTo(40.0f, 64.0f)
                verticalLineTo(200.0f)
                horizontalLineTo(216.0f)
                verticalLineTo(88.0f)
                horizontalLineTo(130.7f)
                arcToRelative(15.6f, 15.6f, 0.0f, false, true, -9.6f, -3.2f)
                lineTo(93.3f, 64.0f)
                close()
            }
        }
            .build()
        return _folder_simple!!
    }

private var _folder_simple: ImageVector? = null



@Preview
@Composable
fun FolderSimplePreview() {
    Image(
        Phosphor.FolderSimple,
        null
    )
}
