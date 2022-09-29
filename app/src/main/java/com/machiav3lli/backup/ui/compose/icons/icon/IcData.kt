package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcData: ImageVector
    get() {
        if (_icData != null) {
            return _icData!!
        }
        _icData = Builder(
            name = "IcData", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 32.0f, viewportHeight = 32.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(24.0f, 3.0f)
                lineTo(8.0f, 3.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, false, 6.0f, 5.0f)
                lineTo(6.0f, 27.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 2.0f, 2.0f)
                lineTo(24.0f, 29.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, 2.0f, -2.0f)
                lineTo(26.0f, 5.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, false, 24.0f, 3.0f)
                close()
                moveTo(24.0f, 5.0f)
                verticalLineToRelative(6.0f)
                lineTo(8.0f, 11.0f)
                lineTo(8.0f, 5.0f)
                close()
                moveTo(8.0f, 19.0f)
                lineTo(8.0f, 13.0f)
                lineTo(24.0f, 13.0f)
                verticalLineToRelative(6.0f)
                close()
                moveTo(8.0f, 27.0f)
                lineTo(8.0f, 21.0f)
                lineTo(24.0f, 21.0f)
                verticalLineToRelative(6.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(11.0f, 8.0f)
                moveToRelative(-1.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(11.0f, 16.0f)
                moveToRelative(-1.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(11.0f, 24.0f)
                moveToRelative(-1.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
            }
        }
            .build()
        return _icData!!
    }

private var _icData: ImageVector? = null
