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

val Phosphor.ListNumbers: ImageVector
    get() {
        if (_list_numbers != null) {
            return _list_numbers!!
        }
        _list_numbers = Builder(
            name = "List-numbers",
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
                moveTo(224.0f, 128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -8.0f, 8.0f)
                horizontalLineTo(104.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineTo(216.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 224.0f, 128.0f)
                close()
                moveTo(104.0f, 72.0f)
                horizontalLineTo(216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(104.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                close()
                moveTo(216.0f, 184.0f)
                horizontalLineTo(104.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineTo(216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(43.6f, 67.2f)
                lineTo(48.0f, 64.9f)
                verticalLineTo(108.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(52.0f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, false, -3.8f, -6.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -7.8f, -0.4f)
                lineToRelative(-16.0f, 8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, 7.2f, 14.4f)
                close()
                moveTo(72.2f, 170.3f)
                arcTo(21.5f, 21.5f, 0.0f, false, false, 76.0f, 158.0f)
                arcToRelative(22.0f, 22.0f, 0.0f, false, false, -42.3f, -8.6f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 14.8f, 6.3f)
                arcTo(5.8f, 5.8f, 0.0f, false, true, 54.0f, 152.0f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, 6.0f, 6.0f)
                arcToRelative(5.7f, 5.7f, 0.0f, false, true, -0.9f, 3.2f)
                lineToRelative(-25.5f, 34.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 40.0f, 208.0f)
                horizontalLineTo(68.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(56.0f)
                lineToRelative(16.0f, -21.4f)
                close()
            }
        }
            .build()
        return _list_numbers!!
    }

private var _list_numbers: ImageVector? = null



@Preview
@Composable
fun ListNumbersPreview() {
    Image(
        Phosphor.ListNumbers,
        null
    )
}
