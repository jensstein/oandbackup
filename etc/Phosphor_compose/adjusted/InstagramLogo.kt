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

val Phosphor.InstagramLogo: ImageVector
    get() {
        if (_instagram_logo != null) {
            return _instagram_logo!!
        }
        _instagram_logo = Builder(
            name = "Instagram-logo",
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
                moveTo(128.0f, 80.0f)
                arcToRelative(48.0f, 48.0f, 0.0f, true, false, 48.0f, 48.0f)
                arcTo(48.0f, 48.0f, 0.0f, false, false, 128.0f, 80.0f)
                close()
                moveTo(128.0f, 160.0f)
                arcToRelative(32.0f, 32.0f, 0.0f, true, true, 32.0f, -32.0f)
                arcTo(32.1f, 32.1f, 0.0f, false, true, 128.0f, 160.0f)
                close()
                moveTo(172.0f, 28.0f)
                lineTo(84.0f, 28.0f)
                arcTo(56.0f, 56.0f, 0.0f, false, false, 28.0f, 84.0f)
                verticalLineToRelative(88.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, false, 56.0f, 56.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(56.0f, 56.0f, 0.0f, false, false, 56.0f, -56.0f)
                lineTo(228.0f, 84.0f)
                arcTo(56.0f, 56.0f, 0.0f, false, false, 172.0f, 28.0f)
                close()
                moveTo(212.0f, 172.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, -40.0f, 40.0f)
                lineTo(84.0f, 212.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, -40.0f, -40.0f)
                lineTo(44.0f, 84.0f)
                arcTo(40.0f, 40.0f, 0.0f, false, true, 84.0f, 44.0f)
                horizontalLineToRelative(88.0f)
                arcToRelative(40.0f, 40.0f, 0.0f, false, true, 40.0f, 40.0f)
                close()
                moveTo(192.0f, 76.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 192.0f, 76.0f)
                close()
            }
        }
            .build()
        return _instagram_logo!!
    }

private var _instagram_logo: ImageVector? = null



@Preview
@Composable
fun InstagramLogoPreview() {
    Image(
        Phosphor.InstagramLogo,
        null
    )
}
