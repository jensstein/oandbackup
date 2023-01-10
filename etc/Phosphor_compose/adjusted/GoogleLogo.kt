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

val Phosphor.GoogleLogo: ImageVector
    get() {
        if (_google_logo != null) {
            return _google_logo!!
        }
        _google_logo = Builder(
            name = "Google-logo",
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
                moveTo(128.0f, 224.0f)
                arcTo(96.0f, 96.0f, 0.0f, true, true, 195.9f, 60.1f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, 11.3f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, -11.3f, 0.0f)
                arcToRelative(80.0f, 80.0f, 0.0f, true, false, 23.0f, 64.6f)
                horizontalLineTo(128.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 8.0f, 8.0f)
                arcTo(96.2f, 96.2f, 0.0f, false, true, 128.0f, 224.0f)
                close()
            }
        }
            .build()
        return _google_logo!!
    }

private var _google_logo: ImageVector? = null



@Preview
@Composable
fun GoogleLogoPreview() {
    Image(
        Phosphor.GoogleLogo,
        null
    )
}
