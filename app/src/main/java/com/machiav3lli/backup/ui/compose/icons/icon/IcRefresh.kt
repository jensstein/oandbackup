package com.machiav3lli.backup.ui.compose.icons.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Icon

val Icon.IcRefresh: ImageVector
    get() {
        if (_icRefresh != null) {
            return _icRefresh!!
        }
        _icRefresh = Builder(
            name = "IcRefresh", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(176.2f, 99.7f)
                lineToRelative(48.0f, 0.0f)
                lineToRelative(0.0f, -48.0f)
            }
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(65.8f, 65.8f)
                arcToRelative(87.9f, 87.9f, 0.0f, false, true, 124.4f, 0.0f)
                lineToRelative(34.0f, 33.9f)
            }
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(79.8f, 156.3f)
                lineToRelative(-48.0f, 0.0f)
                lineToRelative(0.0f, 48.0f)
            }
            path(
                fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 16.0f, strokeLineCap = Round, strokeLineJoin =
                StrokeJoin.Companion.Round, strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(190.2f, 190.2f)
                arcToRelative(87.9f, 87.9f, 0.0f, false, true, -124.4f, 0.0f)
                lineToRelative(-34.0f, -33.9f)
            }
        }
            .build()
        return _icRefresh!!
    }

private var _icRefresh: ImageVector? = null
