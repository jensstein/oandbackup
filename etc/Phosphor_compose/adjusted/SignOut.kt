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

val Phosphor.SignOut: ImageVector
    get() {
        if (_sign_out != null) {
            return _sign_out!!
        }
        _sign_out = Builder(
            name = "Sign-out",
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
                moveTo(221.7f, 133.7f)
                lineToRelative(-42.0f, 42.0f)
                arcTo(8.3f, 8.3f, 0.0f, false, true, 174.0f, 178.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -5.6f, -13.7f)
                lineTo(196.7f, 136.0f)
                horizontalLineTo(104.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 0.0f, -16.0f)
                horizontalLineToRelative(92.7f)
                lineTo(168.4f, 91.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.3f, -11.4f)
                lineToRelative(42.0f, 42.0f)
                arcTo(8.1f, 8.1f, 0.0f, false, true, 221.7f, 133.7f)
                close()
                moveTo(104.0f, 208.0f)
                horizontalLineTo(48.0f)
                verticalLineTo(48.0f)
                horizontalLineToRelative(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(48.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 48.0f)
                verticalLineTo(208.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                horizontalLineToRelative(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                close()
            }
        }
            .build()
        return _sign_out!!
    }

private var _sign_out: ImageVector? = null



@Preview
@Composable
fun SignOutPreview() {
    Image(
        Phosphor.SignOut,
        null
    )
}
