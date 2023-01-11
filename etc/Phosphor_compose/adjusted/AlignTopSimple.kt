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

val Phosphor.AlignTopSimple: ImageVector
    get() {
        if (_align_top_simple != null) {
            return _align_top_simple!!
        }
        _align_top_simple = Builder(
            name = "Align-top-simple",
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
                moveTo(208.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                lineTo(56.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                lineTo(200.0f, 24.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 208.0f, 32.0f)
                close()
                moveTo(176.0f, 72.0f)
                lineTo(176.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(96.0f, 240.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(80.0f, 72.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 96.0f, 56.0f)
                horizontalLineToRelative(64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 176.0f, 72.0f)
                close()
                moveTo(160.0f, 72.0f)
                lineTo(96.0f, 72.0f)
                lineTo(96.0f, 224.0f)
                horizontalLineToRelative(64.0f)
                close()
            }
        }
            .build()
        return _align_top_simple!!
    }

private var _align_top_simple: ImageVector? = null



@Preview
@Composable
fun AlignTopSimplePreview() {
    Image(
        Phosphor.AlignTopSimple,
        null
    )
}
