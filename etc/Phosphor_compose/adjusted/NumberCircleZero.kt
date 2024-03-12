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

val Phosphor.NumberCircleZero: ImageVector
    get() {
        if (_number_circle_zero != null) {
            return _number_circle_zero!!
        }
        _number_circle_zero = Builder(
            name = "Number-circle-zero",
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
                moveTo(128.0f, 72.0f)
                curveToRelative(-13.0f, 0.0f, -24.0f, 6.7f, -31.2f, 19.0f)
                curveToRelative(-5.7f, 9.7f, -8.8f, 22.9f, -8.8f, 37.0f)
                reflectiveCurveToRelative(3.1f, 27.3f, 8.8f, 37.0f)
                curveToRelative(7.2f, 12.3f, 18.2f, 19.0f, 31.2f, 19.0f)
                reflectiveCurveToRelative(24.0f, -6.7f, 31.2f, -19.0f)
                curveToRelative(5.7f, -9.7f, 8.8f, -22.9f, 8.8f, -37.0f)
                reflectiveCurveToRelative(-3.1f, -27.3f, -8.8f, -37.0f)
                curveTo(152.0f, 78.7f, 141.0f, 72.0f, 128.0f, 72.0f)
                close()
                moveTo(128.0f, 168.0f)
                curveToRelative(-8.9f, 0.0f, -24.0f, -8.4f, -24.0f, -40.0f)
                reflectiveCurveToRelative(15.1f, -40.0f, 24.0f, -40.0f)
                reflectiveCurveToRelative(24.0f, 8.4f, 24.0f, 40.0f)
                reflectiveCurveTo(136.9f, 168.0f, 128.0f, 168.0f)
                close()
                moveTo(128.0f, 24.0f)
                arcTo(104.0f, 104.0f, 0.0f, true, false, 232.0f, 128.0f)
                arcTo(104.2f, 104.2f, 0.0f, false, false, 128.0f, 24.0f)
                close()
                moveTo(128.0f, 216.0f)
                arcToRelative(88.0f, 88.0f, 0.0f, true, true, 88.0f, -88.0f)
                arcTo(88.1f, 88.1f, 0.0f, false, true, 128.0f, 216.0f)
                close()
            }
        }
            .build()
        return _number_circle_zero!!
    }

private var _number_circle_zero: ImageVector? = null



@Preview
@Composable
fun NumberCircleZeroPreview() {
    Image(
        Phosphor.NumberCircleZero,
        null
    )
}
