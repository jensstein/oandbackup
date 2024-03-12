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

val Phosphor.FlagBanner: ImageVector
    get() {
        if (_flag_banner != null) {
            return _flag_banner!!
        }
        _flag_banner = Builder(
            name = "Flag-banner",
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
                moveTo(186.2f, 108.0f)
                lineToRelative(44.0f, -55.0f)
                arcTo(8.0f, 8.0f, 0.0f, false, false, 224.0f, 40.0f)
                horizontalLineTo(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineTo(216.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(176.0f)
                horizontalLineTo(224.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 6.2f, -13.0f)
                close()
                moveTo(48.0f, 160.0f)
                verticalLineTo(56.0f)
                horizontalLineTo(207.4f)
                lineToRelative(-37.6f, 47.0f)
                arcToRelative(7.8f, 7.8f, 0.0f, false, false, 0.0f, 10.0f)
                lineToRelative(37.6f, 47.0f)
                close()
            }
        }
            .build()
        return _flag_banner!!
    }

private var _flag_banner: ImageVector? = null



@Preview
@Composable
fun FlagBannerPreview() {
    Image(
        Phosphor.FlagBanner,
        null
    )
}
