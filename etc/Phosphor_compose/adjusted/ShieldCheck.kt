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

val Phosphor.ShieldCheck: ImageVector
    get() {
        if (_shield_check != null) {
            return _shield_check!!
        }
        _shield_check = Builder(
            name = "Shield-check",
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
                moveTo(208.0f, 40.0f)
                lineTo(48.0f, 40.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 32.0f, 56.0f)
                verticalLineToRelative(58.7f)
                curveToRelative(0.0f, 89.4f, 75.8f, 119.1f, 91.0f, 124.1f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 10.0f, 0.0f)
                curveToRelative(15.2f, -5.0f, 91.0f, -34.7f, 91.0f, -124.1f)
                lineTo(224.0f, 56.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 208.0f, 40.0f)
                close()
                moveTo(208.0f, 114.7f)
                curveToRelative(0.0f, 78.2f, -66.4f, 104.4f, -80.0f, 108.9f)
                curveToRelative(-13.5f, -4.5f, -80.0f, -30.6f, -80.0f, -108.9f)
                lineTo(48.0f, 56.0f)
                lineTo(208.0f, 56.0f)
                close()
                moveTo(78.5f, 137.8f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, 11.0f, -11.6f)
                lineToRelative(23.8f, 22.7f)
                lineToRelative(53.2f, -50.7f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 11.0f, 11.6f)
                lineToRelative(-58.6f, 56.0f)
                arcToRelative(8.1f, 8.1f, 0.0f, false, true, -5.6f, 2.2f)
                arcToRelative(7.9f, 7.9f, 0.0f, false, true, -5.5f, -2.2f)
                close()
            }
        }
            .build()
        return _shield_check!!
    }

private var _shield_check: ImageVector? = null



@Preview
@Composable
fun ShieldCheckPreview() {
    Image(
        Phosphor.ShieldCheck,
        null
    )
}
