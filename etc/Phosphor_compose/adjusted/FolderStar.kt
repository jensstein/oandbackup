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

val Phosphor.FolderStar: ImageVector
    get() {
        if (_folder_star != null) {
            return _folder_star!!
        }
        _folder_star = Builder(
            name = "Folder-star",
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
                moveTo(120.6f, 200.0f)
                lineTo(40.0f, 200.0f)
                lineTo(40.0f, 88.0f)
                lineTo(216.0f, 88.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(232.0f, 88.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -16.0f, -16.0f)
                lineTo(131.3f, 72.0f)
                lineTo(104.0f, 44.7f)
                arcTo(15.9f, 15.9f, 0.0f, false, false, 92.7f, 40.0f)
                lineTo(40.0f, 40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 24.0f, 56.0f)
                lineTo(24.0f, 200.6f)
                arcTo(15.4f, 15.4f, 0.0f, false, false, 39.4f, 216.0f)
                horizontalLineToRelative(81.2f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 0.0f, -16.0f)
                close()
                moveTo(92.7f, 56.0f)
                lineToRelative(16.0f, 16.0f)
                lineTo(40.0f, 72.0f)
                lineTo(40.0f, 56.0f)
                close()
                moveTo(243.6f, 159.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -7.0f, -5.6f)
                lineToRelative(-29.8f, -2.3f)
                lineToRelative(-11.5f, -26.5f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -14.6f, 0.0f)
                lineToRelative(-11.5f, 26.5f)
                lineToRelative(-29.8f, 2.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -4.5f, 14.2f)
                lineToRelative(22.5f, 18.6f)
                lineToRelative(-6.8f, 27.7f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 3.0f, 8.3f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.8f, 0.5f)
                lineTo(188.0f, 207.8f)
                lineToRelative(25.6f, 15.1f)
                arcToRelative(8.7f, 8.7f, 0.0f, false, false, 4.1f, 1.1f)
                arcToRelative(7.4f, 7.4f, 0.0f, false, false, 4.7f, -1.6f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, 3.0f, -8.3f)
                lineToRelative(-6.8f, -27.7f)
                lineToRelative(22.5f, -18.6f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 243.6f, 159.2f)
                close()
                moveTo(204.5f, 177.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -2.7f, 8.1f)
                lineToRelative(3.5f, 14.2f)
                lineToRelative(-13.2f, -7.9f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, -8.2f, 0.0f)
                lineToRelative(-13.2f, 7.9f)
                lineToRelative(3.5f, -14.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -2.7f, -8.1f)
                lineToRelative(-11.1f, -9.1f)
                lineToRelative(14.9f, -1.2f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 6.7f, -4.8f)
                lineToRelative(6.0f, -13.9f)
                lineToRelative(6.0f, 13.9f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 6.7f, 4.8f)
                lineToRelative(14.9f, 1.2f)
                close()
            }
        }
            .build()
        return _folder_star!!
    }

private var _folder_star: ImageVector? = null



@Preview
@Composable
fun FolderStarPreview() {
    Image(
        Phosphor.FolderStar,
        null
    )
}
