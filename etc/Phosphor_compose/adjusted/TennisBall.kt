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

val Phosphor.TennisBall: ImageVector
    get() {
        if (_tennis_ball != null) {
            return _tennis_ball!!
        }
        _tennis_ball = Builder(
            name = "Tennis-ball",
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
                moveTo(231.9f, 129.1f)
                arcToRelative(2.8f, 2.8f, 0.0f, false, false, 0.1f, -0.9f)
                arcToRelative(3.1f, 3.1f, 0.0f, false, false, -0.1f, -1.0f)
                arcTo(104.0f, 104.0f, 0.0f, false, false, 128.0f, 24.0f)
                horizontalLineToRelative(-0.2f)
                arcTo(104.0f, 104.0f, 0.0f, false, false, 24.1f, 126.9f)
                arcToRelative(2.8f, 2.8f, 0.0f, false, false, -0.1f, 0.9f)
                arcToRelative(3.1f, 3.1f, 0.0f, false, false, 0.1f, 1.0f)
                arcTo(104.0f, 104.0f, 0.0f, false, false, 128.0f, 232.0f)
                horizontalLineToRelative(0.2f)
                arcTo(104.0f, 104.0f, 0.0f, false, false, 231.9f, 129.1f)
                close()
                moveTo(65.8f, 65.8f)
                arcToRelative(87.4f, 87.4f, 0.0f, false, true, 53.6f, -25.3f)
                arcToRelative(87.8f, 87.8f, 0.0f, false, true, -78.9f, 78.9f)
                arcTo(87.1f, 87.1f, 0.0f, false, true, 65.8f, 65.8f)
                close()
                moveTo(40.4f, 135.5f)
                arcToRelative(104.0f, 104.0f, 0.0f, false, false, 95.1f, -95.1f)
                arcToRelative(87.8f, 87.8f, 0.0f, false, true, 80.1f, 80.1f)
                arcToRelative(104.0f, 104.0f, 0.0f, false, false, -95.1f, 95.1f)
                arcToRelative(87.8f, 87.8f, 0.0f, false, true, -80.1f, -80.1f)
                close()
                moveTo(136.6f, 215.5f)
                arcToRelative(87.8f, 87.8f, 0.0f, false, true, 78.9f, -78.9f)
                arcToRelative(87.8f, 87.8f, 0.0f, false, true, -78.9f, 78.9f)
                close()
            }
        }
            .build()
        return _tennis_ball!!
    }

private var _tennis_ball: ImageVector? = null



@Preview
@Composable
fun TennisBallPreview() {
    Image(
        Phosphor.TennisBall,
        null
    )
}
