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

val Phosphor.GitMerge: ImageVector
    get() {
        if (_git_merge != null) {
            return _git_merge!!
        }
        _git_merge = Builder(
            name = "Git-merge",
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
                moveTo(188.0f, 104.0f)
                arcToRelative(36.1f, 36.1f, 0.0f, false, false, -35.8f, 32.0f)
                lineTo(130.5f, 136.0f)
                arcToRelative(39.6f, 39.6f, 0.0f, false, true, -30.7f, -14.4f)
                lineTo(82.5f, 100.9f)
                arcTo(35.9f, 35.9f, 0.0f, true, false, 60.0f, 103.1f)
                verticalLineToRelative(49.8f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 16.0f, 0.0f)
                lineTo(76.0f, 118.1f)
                lineToRelative(11.5f, 13.7f)
                arcToRelative(55.4f, 55.4f, 0.0f, false, false, 43.0f, 20.2f)
                horizontalLineToRelative(23.6f)
                arcTo(36.0f, 36.0f, 0.0f, true, false, 188.0f, 104.0f)
                close()
                moveTo(48.0f, 68.0f)
                arcTo(20.0f, 20.0f, 0.0f, true, true, 68.0f, 88.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 48.0f, 68.0f)
                close()
                moveTo(88.0f, 188.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, -20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 88.0f, 188.0f)
                close()
                moveTo(188.0f, 160.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, 20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 188.0f, 160.0f)
                close()
            }
        }
            .build()
        return _git_merge!!
    }

private var _git_merge: ImageVector? = null



@Preview
@Composable
fun GitMergePreview() {
    Image(
        Phosphor.GitMerge,
        null
    )
}
