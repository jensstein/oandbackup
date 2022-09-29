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

val Icon.IcSettings: ImageVector
    get() {
        if (_icSettings != null) {
            return _icSettings!!
        }
        _icSettings = Builder(
            name = "IcSettings", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(10.0f, 4.0f)
                curveTo(10.0f, 3.4477f, 10.4477f, 3.0f, 11.0f, 3.0f)
                horizontalLineTo(13.0f)
                curveTo(13.5523f, 3.0f, 14.0f, 3.4477f, 14.0f, 4.0f)
                verticalLineTo(4.5688f)
                curveTo(14.0f, 4.9966f, 14.2871f, 5.3682f, 14.6822f, 5.5323f)
                curveTo(15.0775f, 5.6964f, 15.5377f, 5.6338f, 15.8403f, 5.3312f)
                lineTo(16.2426f, 4.9289f)
                curveTo(16.6331f, 4.5384f, 17.2663f, 4.5384f, 17.6568f, 4.9289f)
                lineTo(19.071f, 6.3431f)
                curveTo(19.4616f, 6.7337f, 19.4616f, 7.3668f, 19.071f, 7.7573f)
                lineTo(18.6688f, 8.1596f)
                curveTo(18.3661f, 8.4622f, 18.3036f, 8.9225f, 18.4677f, 9.3177f)
                curveTo(18.6317f, 9.7129f, 19.0034f, 10.0f, 19.4313f, 10.0f)
                lineTo(20.0f, 10.0f)
                curveTo(20.5523f, 10.0f, 21.0f, 10.4477f, 21.0f, 11.0f)
                verticalLineTo(13.0f)
                curveTo(21.0f, 13.5523f, 20.5523f, 14.0f, 20.0f, 14.0f)
                horizontalLineTo(19.4312f)
                curveTo(19.0034f, 14.0f, 18.6318f, 14.2871f, 18.4677f, 14.6822f)
                curveTo(18.3036f, 15.0775f, 18.3661f, 15.5377f, 18.6688f, 15.8403f)
                lineTo(19.071f, 16.2426f)
                curveTo(19.4616f, 16.6331f, 19.4616f, 17.2663f, 19.071f, 17.6568f)
                lineTo(17.6568f, 19.071f)
                curveTo(17.2663f, 19.4616f, 16.6331f, 19.4616f, 16.2426f, 19.071f)
                lineTo(15.8403f, 18.6688f)
                curveTo(15.5377f, 18.3661f, 15.0775f, 18.3036f, 14.6822f, 18.4677f)
                curveTo(14.2871f, 18.6317f, 14.0f, 19.0034f, 14.0f, 19.4312f)
                verticalLineTo(20.0f)
                curveTo(14.0f, 20.5523f, 13.5523f, 21.0f, 13.0f, 21.0f)
                horizontalLineTo(11.0f)
                curveTo(10.4477f, 21.0f, 10.0f, 20.5523f, 10.0f, 20.0f)
                verticalLineTo(19.4313f)
                curveTo(10.0f, 19.0034f, 9.7129f, 18.6317f, 9.3177f, 18.4677f)
                curveTo(8.9225f, 18.3036f, 8.4622f, 18.3661f, 8.1596f, 18.6688f)
                lineTo(7.7573f, 19.071f)
                curveTo(7.3668f, 19.4616f, 6.7336f, 19.4616f, 6.3431f, 19.071f)
                lineTo(4.9289f, 17.6568f)
                curveTo(4.5384f, 17.2663f, 4.5384f, 16.6331f, 4.9289f, 16.2426f)
                lineTo(5.3312f, 15.8403f)
                curveTo(5.6338f, 15.5377f, 5.6964f, 15.0775f, 5.5323f, 14.6822f)
                curveTo(5.3682f, 14.2871f, 4.9966f, 14.0f, 4.5688f, 14.0f)
                horizontalLineTo(4.0f)
                curveTo(3.4477f, 14.0f, 3.0f, 13.5523f, 3.0f, 13.0f)
                verticalLineTo(11.0f)
                curveTo(3.0f, 10.4477f, 3.4477f, 10.0f, 4.0f, 10.0f)
                lineTo(4.5688f, 10.0f)
                curveTo(4.9966f, 10.0f, 5.3682f, 9.7129f, 5.5323f, 9.3178f)
                curveTo(5.6964f, 8.9225f, 5.6339f, 8.4623f, 5.3312f, 8.1597f)
                lineTo(4.9289f, 7.7573f)
                curveTo(4.5384f, 7.3668f, 4.5384f, 6.7337f, 4.9289f, 6.3431f)
                lineTo(6.3431f, 4.9289f)
                curveTo(6.7337f, 4.5384f, 7.3668f, 4.5384f, 7.7573f, 4.9289f)
                lineTo(8.1597f, 5.3312f)
                curveTo(8.4623f, 5.6339f, 8.9225f, 5.6964f, 9.3178f, 5.5323f)
                curveTo(9.7129f, 5.3682f, 10.0f, 4.9966f, 10.0f, 4.5688f)
                verticalLineTo(4.0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.5f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(14.0f, 12.0f)
                curveTo(14.0f, 13.1046f, 13.1046f, 14.0f, 12.0f, 14.0f)
                curveTo(10.8954f, 14.0f, 10.0f, 13.1046f, 10.0f, 12.0f)
                curveTo(10.0f, 10.8954f, 10.8954f, 10.0f, 12.0f, 10.0f)
                curveTo(13.1046f, 10.0f, 14.0f, 10.8954f, 14.0f, 12.0f)
                close()
            }
        }
            .build()
        return _icSettings!!
    }

private var _icSettings: ImageVector? = null
