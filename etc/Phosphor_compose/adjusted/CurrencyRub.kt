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

val Phosphor.CurrencyRub: ImageVector
    get() {
        if (_currency_rub != null) {
            return _currency_rub!!
        }
        _currency_rub = Builder(
            name = "Currency-rub",
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
                moveTo(148.0f, 152.0f)
                arcToRelative(60.0f, 60.0f, 0.0f, false, false, 0.0f, -120.0f)
                horizontalLineTo(88.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -8.0f, 8.0f)
                verticalLineToRelative(96.0f)
                horizontalLineTo(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineTo(80.0f)
                verticalLineToRelative(16.0f)
                horizontalLineTo(56.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, 16.0f)
                horizontalLineTo(80.0f)
                verticalLineToRelative(32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 16.0f, 0.0f)
                verticalLineTo(184.0f)
                horizontalLineToRelative(48.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                horizontalLineTo(96.0f)
                verticalLineTo(152.0f)
                close()
                moveTo(96.0f, 48.0f)
                horizontalLineToRelative(52.0f)
                arcToRelative(44.0f, 44.0f, 0.0f, false, true, 0.0f, 88.0f)
                horizontalLineTo(96.0f)
                close()
            }
        }
            .build()
        return _currency_rub!!
    }

private var _currency_rub: ImageVector? = null



@Preview
@Composable
fun CurrencyRubPreview() {
    Image(
        Phosphor.CurrencyRub,
        null
    )
}
