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

val Phosphor.AlignRight: ImageVector
    get() {
        if (_align_right != null) {
            return _align_right!!
        }
        _align_right = Builder(
            name = "Align-right",
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
                moveTo(224.0f, 40.0f)
                lineTo(224.0f, 216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                lineTo(208.0f, 40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
                moveTo(192.0f, 64.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(80.0f, 120.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(64.0f, 64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 80.0f, 48.0f)
                horizontalLineToRelative(96.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 192.0f, 64.0f)
                close()
                moveTo(176.0f, 64.0f)
                lineTo(80.0f, 64.0f)
                verticalLineToRelative(40.0f)
                horizontalLineToRelative(96.0f)
                close()
                moveTo(192.0f, 152.0f)
                verticalLineToRelative(40.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
                lineTo(40.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, -16.0f)
                lineTo(24.0f, 152.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, 16.0f, -16.0f)
                lineTo(176.0f, 136.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, true, 192.0f, 152.0f)
                close()
                moveTo(176.0f, 152.0f)
                lineTo(40.0f, 152.0f)
                verticalLineToRelative(40.0f)
                lineTo(176.0f, 192.0f)
                close()
            }
        }
            .build()
        return _align_right!!
    }

private var _align_right: ImageVector? = null



@Preview
@Composable
fun AlignRightPreview() {
    Image(
        Phosphor.AlignRight,
        null
    )
}
