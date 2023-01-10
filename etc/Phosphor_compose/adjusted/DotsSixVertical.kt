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

val Phosphor.DotsSixVertical: ImageVector
    get() {
        if (_dots_six_vertical != null) {
            return _dots_six_vertical!!
        }
        _dots_six_vertical = Builder(
            name = "Dots-six-vertical",
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
                moveTo(104.0f, 60.0f)
                arcTo(12.0f, 12.0f, 0.0f, true, true, 92.0f, 48.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 104.0f, 60.0f)
                close()
                moveTo(164.0f, 72.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 164.0f, 72.0f)
                close()
                moveTo(92.0f, 116.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 92.0f, 116.0f)
                close()
                moveTo(164.0f, 116.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 164.0f, 116.0f)
                close()
                moveTo(92.0f, 184.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 92.0f, 184.0f)
                close()
                moveTo(164.0f, 184.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, false, 12.0f, 12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, false, 164.0f, 184.0f)
                close()
            }
        }
            .build()
        return _dots_six_vertical!!
    }

private var _dots_six_vertical: ImageVector? = null



@Preview
@Composable
fun DotsSixVerticalPreview() {
    Image(
        Phosphor.DotsSixVertical,
        null
    )
}
