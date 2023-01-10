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

val Phosphor.GitBranch: ImageVector
    get() {
        if (_git_branch != null) {
            return _git_branch!!
        }
        _git_branch = Builder(
            name = "Git-branch",
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
                moveTo(224.0f, 68.0f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, -44.0f, 35.1f)
                verticalLineToRelative(0.9f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                horizontalLineTo(92.0f)
                arcToRelative(32.2f, 32.2f, 0.0f, false, false, -16.0f, 4.3f)
                verticalLineTo(103.1f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, -16.0f, 0.0f)
                verticalLineToRelative(49.8f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 16.0f, 0.0f)
                verticalLineTo(152.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                horizontalLineToRelative(72.0f)
                arcToRelative(32.1f, 32.1f, 0.0f, false, false, 32.0f, -32.0f)
                verticalLineToRelative(-0.9f)
                arcTo(36.1f, 36.1f, 0.0f, false, false, 224.0f, 68.0f)
                close()
                moveTo(48.0f, 68.0f)
                arcTo(20.0f, 20.0f, 0.0f, true, true, 68.0f, 88.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 48.0f, 68.0f)
                close()
                moveTo(88.0f, 188.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, -20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 88.0f, 188.0f)
                close()
                moveTo(188.0f, 88.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, 20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 188.0f, 88.0f)
                close()
            }
        }
            .build()
        return _git_branch!!
    }

private var _git_branch: ImageVector? = null



@Preview
@Composable
fun GitBranchPreview() {
    Image(
        Phosphor.GitBranch,
        null
    )
}
