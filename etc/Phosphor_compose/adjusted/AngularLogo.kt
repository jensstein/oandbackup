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

val Phosphor.AngularLogo: ImageVector
    get() {
        if (_angular_logo != null) {
            return _angular_logo!!
        }
        _angular_logo = Builder(
            name = "Angular-logo",
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
                moveTo(227.1f, 64.6f)
                lineToRelative(-96.0f, -40.0f)
                arcToRelative(8.3f, 8.3f, 0.0f, false, false, -6.2f, 0.0f)
                lineToRelative(-96.0f, 40.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -4.8f, 8.5f)
                lineToRelative(16.0f, 120.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 4.3f, 6.1f)
                lineToRelative(80.0f, 40.0f)
                arcToRelative(8.5f, 8.5f, 0.0f, false, false, 7.2f, 0.0f)
                lineToRelative(80.0f, -40.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, 4.3f, -6.1f)
                lineToRelative(16.0f, -120.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 227.1f, 64.6f)
                close()
                moveTo(200.6f, 186.7f)
                lineTo(128.0f, 223.1f)
                lineTo(55.4f, 186.7f)
                lineTo(40.7f, 77.0f)
                lineTo(128.0f, 40.7f)
                lineTo(215.3f, 77.0f)
                close()
                moveTo(120.8f, 84.4f)
                lineToRelative(-36.0f, 72.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 14.4f, 7.2f)
                lineToRelative(8.4f, -16.9f)
                horizontalLineToRelative(40.8f)
                lineToRelative(8.4f, 16.9f)
                arcTo(8.1f, 8.1f, 0.0f, false, false, 164.0f, 168.0f)
                arcToRelative(9.4f, 9.4f, 0.0f, false, false, 3.6f, -0.8f)
                arcToRelative(8.2f, 8.2f, 0.0f, false, false, 3.6f, -10.8f)
                lineToRelative(-36.0f, -72.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, false, -14.4f, 0.0f)
                close()
                moveTo(140.4f, 130.7f)
                lineTo(115.6f, 130.7f)
                lineTo(128.0f, 105.9f)
                close()
            }
        }
            .build()
        return _angular_logo!!
    }

private var _angular_logo: ImageVector? = null



@Preview
@Composable
fun AngularLogoPreview() {
    Image(
        Phosphor.AngularLogo,
        null
    )
}
