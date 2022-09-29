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

val Icon.IcFingerprint: ImageVector
    get() {
        if (_icFingerprint != null) {
            return _icFingerprint!!
        }
        _icFingerprint = Builder(
            name = "IcFingerprint", defaultWidth = 38.0.dp, defaultHeight =
            38.0.dp, viewportWidth = 32.0f, viewportHeight = 32.0f
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
                    moveTo(50.7f, 184.9f)
                    arcTo(127.4f, 127.4f, 0.0f, false, false, 64.0f, 128.0f)
                    arcTo(64.2f, 64.2f, 0.0f, false, true, 88.0f, 78.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(128.0f, 128.0f)
                    arcToRelative(191.2f, 191.2f, 0.0f, false, true, -24.0f, 93.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(96.0f, 128.0f)
                    arcToRelative(32.0f, 32.0f, 0.0f, false, true, 64.0f, 0.0f)
                    arcToRelative(222.3f, 222.3f, 0.0f, false, true, -21.3f, 95.4f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(218.6f, 184.0f)
                    arcToRelative(294.6f, 294.6f, 0.0f, false, false, 5.4f, -56.0f)
                    arcToRelative(96.0f, 96.0f, 0.0f, false, false, -192.0f, 0.0f)
                    arcToRelative(94.4f, 94.4f, 0.0f, false, true, -5.5f, 32.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(92.8f, 160.0f)
                    arcToRelative(161.9f, 161.9f, 0.0f, false, true, -18.1f, 47.9f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(120.0f, 64.5f)
                    arcToRelative(70.1f, 70.1f, 0.0f, false, true, 8.0f, -0.5f)
                    arcToRelative(64.0f, 64.0f, 0.0f, false, true, 64.0f, 64.0f)
                    arcToRelative(260.6f, 260.6f, 0.0f, false, true, -2.0f, 32.0f)
                }
                path(
                    fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                    strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                    StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType =
                    NonZero
                ) {
                    moveTo(183.9f, 192.0f)
                    curveToRelative(-1.5f, 5.9f, -3.2f, 11.8f, -5.1f, 17.5f)
                }
            }
        }
            .build()
        return _icFingerprint!!
    }

private var _icFingerprint: ImageVector? = null
