package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcExternalData: ImageVector
    get() {
        if (_icExternalData != null) {
            return _icExternalData!!
        }
        _icExternalData = Builder(
            name = "IcExternalData", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 16.0f, viewportHeight = 16.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(6.25f, 3.5f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, -1.5f, 0.0f)
                verticalLineToRelative(2.0f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, 1.5f, 0.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(8.25f, 3.5f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, -1.5f, 0.0f)
                verticalLineToRelative(2.0f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, 1.5f, 0.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(10.25f, 3.5f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, -1.5f, 0.0f)
                verticalLineToRelative(2.0f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, 1.5f, 0.0f)
                verticalLineToRelative(-2.0f)
                close()
                moveTo(12.25f, 3.5f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, -1.5f, 0.0f)
                verticalLineToRelative(2.0f)
                arcToRelative(0.75f, 0.75f, 0.0f, false, false, 1.5f, 0.0f)
                verticalLineToRelative(-2.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = EvenOdd
            ) {
                moveTo(5.914f, 0.0f)
                horizontalLineTo(12.5f)
                arcTo(1.5f, 1.5f, 0.0f, false, true, 14.0f, 1.5f)
                verticalLineToRelative(13.0f)
                arcToRelative(1.5f, 1.5f, 0.0f, false, true, -1.5f, 1.5f)
                horizontalLineToRelative(-9.0f)
                arcTo(1.5f, 1.5f, 0.0f, false, true, 2.0f, 14.5f)
                verticalLineTo(3.914f)
                curveToRelative(0.0f, -0.398f, 0.158f, -0.78f, 0.44f, -1.06f)
                lineTo(4.853f, 0.439f)
                arcTo(1.5f, 1.5f, 0.0f, false, true, 5.914f, 0.0f)
                close()
                moveTo(13.0f, 1.5f)
                arcToRelative(0.5f, 0.5f, 0.0f, false, false, -0.5f, -0.5f)
                horizontalLineTo(5.914f)
                arcToRelative(0.5f, 0.5f, 0.0f, false, false, -0.353f, 0.146f)
                lineTo(3.146f, 3.561f)
                arcTo(0.5f, 0.5f, 0.0f, false, false, 3.0f, 3.914f)
                verticalLineTo(14.5f)
                arcToRelative(0.5f, 0.5f, 0.0f, false, false, 0.5f, 0.5f)
                horizontalLineToRelative(9.0f)
                arcToRelative(0.5f, 0.5f, 0.0f, false, false, 0.5f, -0.5f)
                verticalLineToRelative(-13.0f)
                close()
            }
        }
            .build()
        return _icExternalData!!
    }

private var _icExternalData: ImageVector? = null
