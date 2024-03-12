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

val Phosphor.FlowArrow: ImageVector
    get() {
        if (_flow_arrow != null) {
            return _flow_arrow!!
        }
        _flow_arrow = Builder(
            name = "Flow-arrow",
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
                moveTo(245.7f, 74.3f)
                lineToRelative(-40.0f, -40.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -11.4f, 11.4f)
                lineTo(220.7f, 72.0f)
                horizontalLineTo(176.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, false, -56.0f, 56.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, -40.0f, 40.0f)
                horizontalLineToRelative(-0.9f)
                arcToRelative(36.0f, 36.0f, 0.0f, true, false, 0.0f, 16.0f)
                horizontalLineTo(80.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, false, 56.0f, -56.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, 40.0f, -40.0f)
                horizontalLineToRelative(44.7f)
                lineToRelative(-26.4f, 26.3f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 0.0f, 11.4f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 11.4f, 0.0f)
                lineToRelative(40.0f, -40.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 245.7f, 74.3f)
                close()
                moveTo(44.0f, 196.0f)
                arcToRelative(20.0f, 20.0f, 0.0f, true, true, 20.0f, -20.0f)
                arcTo(20.1f, 20.1f, 0.0f, false, true, 44.0f, 196.0f)
                close()
            }
        }
            .build()
        return _flow_arrow!!
    }

private var _flow_arrow: ImageVector? = null



@Preview
@Composable
fun FlowArrowPreview() {
    Image(
        Phosphor.FlowArrow,
        null
    )
}
