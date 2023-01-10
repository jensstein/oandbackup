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

val Phosphor.ArrowElbowRight: ImageVector
    get() {
        if (_arrow_elbow_right != null) {
            return _arrow_elbow_right!!
        }
        _arrow_elbow_right = Builder(
            name = "Arrow-elbow-right",
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
                moveTo(240.0f, 80.0f)
                verticalLineToRelative(72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -16.0f, 0.0f)
                verticalLineTo(99.3f)
                lineToRelative(-98.3f, 98.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, true, -11.4f, 0.0f)
                lineToRelative(-96.0f, -96.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 29.7f, 90.3f)
                lineTo(120.0f, 180.7f)
                lineTo(212.7f, 88.0f)
                horizontalLineTo(160.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(72.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 240.0f, 80.0f)
                close()
            }
        }
            .build()
        return _arrow_elbow_right!!
    }

private var _arrow_elbow_right: ImageVector? = null



@Preview
@Composable
fun ArrowElbowRightPreview() {
    Image(
        Phosphor.ArrowElbowRight,
        null
    )
}
