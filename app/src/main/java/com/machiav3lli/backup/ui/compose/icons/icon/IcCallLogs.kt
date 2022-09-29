package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcCallLogs: ImageVector
    get() {
        if (_icCallLogs != null) {
            return _icCallLogs!!
        }
        _icCallLogs = Builder(
            name = "IcCallLogs", defaultWidth = 38.0.dp, defaultHeight = 38.0.dp,
            viewportWidth = 32.0f, viewportHeight = 32.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(16.0f, 16.0f)
                moveToRelative(-16.0f, 0.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 32.0f, 0.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, -32.0f, 0.0f)
            }
            group {
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(9.0f, 27.0f)
                    lineTo(9.0f, 81.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(19.0f, 15.0f)
                    lineTo(19.0f, 93.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(29.0f, 8.0f)
                    lineTo(29.0f, 100.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(39.0f, 4.0f)
                    lineTo(39.0f, 104.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(49.0f, 2.0f)
                    lineTo(49.0f, 106.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(59.0f, 2.0f)
                    lineTo(59.0f, 106.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(69.0f, 4.0f)
                    lineTo(69.0f, 104.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(79.0f, 8.0f)
                    lineTo(79.0f, 100.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(89.0f, 15.0f)
                    lineTo(89.0f, 93.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(99.0f, 27.0f)
                    lineTo(99.0f, 81.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(27.0f, 9.0f)
                    lineTo(81.0f, 9.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(15.0f, 19.0f)
                    lineTo(93.0f, 19.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(8.0f, 29.0f)
                    lineTo(100.0f, 29.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(4.0f, 39.0f)
                    lineTo(104.0f, 39.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(2.0f, 49.0f)
                    lineTo(106.0f, 49.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(2.0f, 59.0f)
                    lineTo(106.0f, 59.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(4.0f, 69.0f)
                    lineTo(104.0f, 69.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(8.0f, 79.0f)
                    lineTo(100.0f, 79.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(15.0f, 89.0f)
                    lineTo(93.0f, 89.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(27.0f, 99.0f)
                    lineTo(81.0f, 99.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(19.0f, 29.0f)
                    lineTo(89.0f, 29.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(19.0f, 39.0f)
                    lineTo(89.0f, 39.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(19.0f, 49.0f)
                    lineTo(89.0f, 49.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(19.0f, 59.0f)
                    lineTo(89.0f, 59.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(19.0f, 69.0f)
                    lineTo(89.0f, 69.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(19.0f, 79.0f)
                    lineTo(89.0f, 79.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(29.0f, 19.0f)
                    lineTo(29.0f, 89.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(39.0f, 19.0f)
                    lineTo(39.0f, 89.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(49.0f, 19.0f)
                    lineTo(49.0f, 89.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(59.0f, 19.0f)
                    lineTo(59.0f, 89.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(69.0f, 19.0f)
                    lineTo(69.0f, 89.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0x55FFFFFF)),
                    strokeLineWidth = 0.8f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero
                ) {
                    moveTo(79.0f, 19.0f)
                    lineTo(79.0f, 89.0f)
                }
            }
            group {
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(92.5f, 124.8f)
                    arcToRelative(83.6f, 83.6f, 0.0f, false, false, 39.0f, 38.9f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, false, 7.9f, -0.6f)
                    lineToRelative(25.0f, -16.7f)
                    arcToRelative(7.9f, 7.9f, 0.0f, false, true, 7.6f, -0.7f)
                    lineToRelative(46.8f, 20.1f)
                    arcToRelative(7.9f, 7.9f, 0.0f, false, true, 4.8f, 8.3f)
                    arcTo(48.0f, 48.0f, 0.0f, false, true, 176.0f, 216.0f)
                    arcTo(136.0f, 136.0f, 0.0f, false, true, 40.0f, 80.0f)
                    arcTo(48.0f, 48.0f, 0.0f, false, true, 81.9f, 32.4f)
                    arcToRelative(7.9f, 7.9f, 0.0f, false, true, 8.3f, 4.8f)
                    lineToRelative(20.1f, 46.9f)
                    arcToRelative(8.0f, 8.0f, 0.0f, false, true, -0.6f, 7.5f)
                    lineTo(93.0f, 117.0f)
                    arcTo(8.0f, 8.0f, 0.0f, false, false, 92.5f, 124.8f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(160.0f, 56.0f)
                    lineToRelative(0.0f, 40.0f)
                    lineToRelative(40.0f, 0.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(160.0f, 96.0f)
                    lineTo(208.0f, 48.0f)
                }
            }
        }
            .build()
        return _icCallLogs!!
    }

private var _icCallLogs: ImageVector? = null
