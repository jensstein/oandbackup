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

val Phosphor.AlignLeft: ImageVector
    get() {
        if (_align_left != null) {
            return _align_left!!
        }
        _align_left = Builder(
            name = "Align-left",
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
                moveTo(48.0f, 40.0f)
                lineTo(48.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(32.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
                moveTo(64.0f, 104.0f)
                lineTo(64.0f, 64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 80.0f, 48.0f)
                horizontalLineToRelative(96.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, 16.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(80.0f, 120.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 64.0f, 104.0f)
                close()
                moveTo(80.0f, 104.0f)
                horizontalLineToRelative(96.0f)
                lineTo(176.0f, 64.0f)
                lineTo(80.0f, 64.0f)
                close()
                moveTo(232.0f, 152.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(80.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(64.0f, 152.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                lineTo(216.0f, 136.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 232.0f, 152.0f)
                close()
                moveTo(216.0f, 192.0f)
                lineTo(216.0f, 152.0f)
                lineTo(80.0f, 152.0f)
                verticalLineToRelative(40.0f)
                close()
            }
        }
            .build()
        return _align_left!!
    }

private var _align_left: ImageVector? = null



@Preview
@Composable
fun AlignLeftPreview() {
    Image(
        Phosphor.AlignLeft,
        null
    )
}
