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

val Icon.IcContacts: ImageVector
    get() {
        if (_icContacts != null) {
            return _icContacts!!
        }
        _icContacts = Builder(
            name = "IcContacts", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(11.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(11.0f, 11.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(17.0f, 1.01f)
                lineTo(7.0f, 1.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(19.0f, 3.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -1.99f, -2.0f, -1.99f)
                close()
                moveTo(17.0f, 21.0f)
                lineTo(7.0f, 21.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(1.0f)
                close()
                moveTo(17.0f, 18.0f)
                lineTo(7.0f, 18.0f)
                lineTo(7.0f, 6.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(12.0f)
                close()
                moveTo(7.0f, 4.0f)
                lineTo(7.0f, 3.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(1.0f)
                lineTo(7.0f, 4.0f)
                close()
            }
        }
            .build()
        return _icContacts!!
    }

private var _icContacts: ImageVector? = null
