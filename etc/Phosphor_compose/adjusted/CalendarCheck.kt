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

val Phosphor.CalendarCheck: ImageVector
    get() {
        if (_calendar_check != null) {
            return _calendar_check!!
        }
        _calendar_check = Builder(
            name = "Calendar-check",
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
                lineTo(184.0f, 32.0f)
                lineTo(184.0f, 24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                lineTo(88.0f, 32.0f)
                lineTo(88.0f, 24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -16.0f, 0.0f)
                verticalLineToRelative(8.0f)
                lineTo(48.0f, 32.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                lineTo(32.0f, 208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(208.0f, 224.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(224.0f, 48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 32.0f)
                close()
                moveTo(72.0f, 48.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(88.0f, 48.0f)
                horizontalLineToRelative(80.0f)
                verticalLineToRelative(8.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                lineTo(184.0f, 48.0f)
                horizontalLineToRelative(24.0f)
                lineTo(208.0f, 80.0f)
                lineTo(48.0f, 80.0f)
                lineTo(48.0f, 48.0f)
                close()
                moveTo(208.0f, 208.0f)
                lineTo(48.0f, 208.0f)
                lineTo(48.0f, 96.0f)
                lineTo(208.0f, 96.0f)
                lineTo(208.0f, 208.0f)
                close()
                moveTo(169.8f, 122.5f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -0.3f, 11.3f)
                lineToRelative(-46.7f, 44.0f)
                arcToRelative(7.7f, 7.7f, 0.0f, false, true, -5.5f, 2.2f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, -5.5f, -2.2f)
                lineToRelative(-25.3f, -24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 11.0f, -11.6f)
                lineTo(117.3f, 161.0f)
                lineToRelative(41.2f, -38.8f)
                arcTo(7.9f, 7.9f, 0.0f, false, true, 169.8f, 122.5f)
                close()
            }
        }
            .build()
        return _calendar_check!!
    }

private var _calendar_check: ImageVector? = null



@Preview
@Composable
fun CalendarCheckPreview() {
    Image(
        Phosphor.CalendarCheck,
        null
    )
}
