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

val Phosphor.PhosphorLogo: ImageVector
    get() {
        if (_phosphor_logo != null) {
            return _phosphor_logo!!
        }
        _phosphor_logo = Builder(
            name = "Phosphor-logo",
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
                moveTo(142.0f, 24.0f)
                lineTo(68.0f, 24.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                lineTo(60.0f, 164.0f)
                arcToRelative(74.1f, 74.1f, 0.0f, false, false, 74.0f, 74.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 8.0f, -8.0f)
                lineTo(142.0f, 172.0f)
                arcToRelative(74.0f, 74.0f, 0.0f, false, false, 0.0f, -148.0f)
                close()
                moveTo(76.0f, 65.9f)
                lineTo(121.1f, 156.0f)
                lineTo(76.0f, 156.0f)
                close()
                moveTo(126.0f, 130.1f)
                lineTo(80.9f, 40.0f)
                lineTo(126.0f, 40.0f)
                close()
                moveTo(76.6f, 172.0f)
                lineTo(126.0f, 172.0f)
                verticalLineToRelative(49.5f)
                arcTo(58.1f, 58.1f, 0.0f, false, true, 76.6f, 172.0f)
                close()
                moveTo(142.0f, 156.0f)
                lineTo(142.0f, 40.0f)
                arcToRelative(58.0f, 58.0f, 0.0f, false, true, 0.0f, 116.0f)
                close()
            }
        }
            .build()
        return _phosphor_logo!!
    }

private var _phosphor_logo: ImageVector? = null



@Preview
@Composable
fun PhosphorLogoPreview() {
    Image(
        Phosphor.PhosphorLogo,
        null
    )
}
