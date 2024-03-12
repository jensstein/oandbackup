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

val Phosphor.NumberSix: ImageVector
    get() {
        if (_number_six != null) {
            return _number_six!!
        }
        _number_six = Builder(
            name = "Number-six",
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
                moveTo(128.0f, 104.0f)
                arcToRelative(66.3f, 66.3f, 0.0f, false, false, -19.5f, 3.0f)
                lineToRelative(42.4f, -70.9f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -13.8f, -8.2f)
                lineToRelative(-64.5f, 108.0f)
                lineToRelative(-0.2f, 0.5f)
                arcTo(63.0f, 63.0f, 0.0f, false, false, 64.0f, 168.0f)
                arcToRelative(64.0f, 64.0f, 0.0f, true, false, 64.0f, -64.0f)
                close()
                moveTo(128.0f, 216.0f)
                arcToRelative(48.1f, 48.1f, 0.0f, false, true, -41.2f, -72.7f)
                lineToRelative(0.2f, -0.3f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, true, 41.0f, 73.0f)
                close()
            }
        }
            .build()
        return _number_six!!
    }

private var _number_six: ImageVector? = null



@Preview
@Composable
fun NumberSixPreview() {
    Image(
        Phosphor.NumberSix,
        null
    )
}
