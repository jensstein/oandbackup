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

val Icon.IcObbData: ImageVector
    get() {
        if (_icObbData != null) {
            return _icObbData!!
        }
        _icObbData = Builder(
            name = "IcObbData", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(17.0f, 4.0f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, 6.0f, 6.0f)
                verticalLineToRelative(4.0f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, -6.0f, 6.0f)
                lineTo(7.0f, 20.0f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, -6.0f, -6.0f)
                verticalLineToRelative(-4.0f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, 6.0f, -6.0f)
                horizontalLineToRelative(10.0f)
                close()
                moveTo(17.0f, 6.0f)
                lineTo(7.0f, 6.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, false, -3.995f, 3.8f)
                lineTo(3.0f, 10.0f)
                verticalLineToRelative(4.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, false, 3.8f, 3.995f)
                lineTo(7.0f, 18.0f)
                horizontalLineToRelative(10.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, false, 3.995f, -3.8f)
                lineTo(21.0f, 14.0f)
                verticalLineToRelative(-4.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, false, -3.8f, -3.995f)
                lineTo(17.0f, 6.0f)
                close()
                moveTo(10.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                lineTo(9.999f, 13.0f)
                lineTo(10.0f, 15.0f)
                lineTo(8.0f, 15.0f)
                lineToRelative(-0.001f, -2.0f)
                lineTo(6.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                lineTo(8.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(18.0f, 13.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                lineTo(14.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                close()
            }
        }
            .build()
        return _icObbData!!
    }

private var _icObbData: ImageVector? = null
