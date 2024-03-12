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

val Phosphor.GenderTransgender: ImageVector
    get() {
        if (_gender_transgender != null) {
            return _gender_transgender!!
        }
        _gender_transgender = Builder(
            name = "Gender-transgender",
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
                moveTo(216.0f, 32.0f)
                horizontalLineTo(168.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineToRelative(28.7f)
                lineTo(168.0f, 76.7f)
                lineTo(149.7f, 58.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 11.4f)
                lineTo(156.7f, 88.0f)
                lineToRelative(-15.8f, 15.8f)
                arcToRelative(72.2f, 72.2f, 0.0f, true, false, 11.3f, 11.3f)
                lineTo(168.0f, 99.3f)
                lineToRelative(18.3f, 18.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, -11.4f)
                lineTo(179.3f, 88.0f)
                lineTo(208.0f, 59.3f)
                verticalLineTo(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(40.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 216.0f, 32.0f)
                close()
                moveTo(135.6f, 199.6f)
                arcToRelative(56.1f, 56.1f, 0.0f, false, true, -79.2f, 0.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, true, 79.2f, -79.2f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, true, 0.0f, 79.2f)
                close()
            }
        }
            .build()
        return _gender_transgender!!
    }

private var _gender_transgender: ImageVector? = null



@Preview
@Composable
fun GenderTransgenderPreview() {
    Image(
        Phosphor.GenderTransgender,
        null
    )
}
