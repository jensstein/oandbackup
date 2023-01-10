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

val Phosphor.TrafficCone: ImageVector
    get() {
        if (_traffic_cone != null) {
            return _traffic_cone!!
        }
        _traffic_cone = Builder(
            name = "Traffic-cone",
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
                moveTo(232.0f, 208.0f)
                lineTo(213.7f, 208.0f)
                lineTo(153.4f, 34.7f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 138.3f, 24.0f)
                lineTo(117.7f, 24.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, -15.1f, 10.7f)
                lineTo(42.3f, 208.0f)
                lineTo(24.0f, 208.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                lineTo(232.0f, 224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
                moveTo(95.4f, 104.0f)
                horizontalLineToRelative(65.2f)
                lineToRelative(16.7f, 48.0f)
                lineTo(78.7f, 152.0f)
                close()
                moveTo(117.7f, 40.0f)
                horizontalLineToRelative(20.6f)
                lineTo(155.0f, 88.0f)
                lineTo(101.0f, 88.0f)
                close()
                moveTo(73.2f, 168.0f)
                lineTo(182.8f, 168.0f)
                lineToRelative(13.9f, 40.0f)
                lineTo(59.3f, 208.0f)
                close()
            }
        }
            .build()
        return _traffic_cone!!
    }

private var _traffic_cone: ImageVector? = null



@Preview
@Composable
fun TrafficConePreview() {
    Image(
        Phosphor.TrafficCone,
        null
    )
}
