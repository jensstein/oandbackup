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

val Icon.IcRevisions: ImageVector
    get() {
        if (_icRevisions != null) {
            return _icRevisions!!
        }
        _icRevisions = Builder(
            name = "IcRevisions", defaultWidth = 32.0.dp, defaultHeight =
            32.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(17.0483f, 2.001f)
                lineTo(17.1644f, 2.0136f)
                curveTo(17.7091f, 2.1044f, 18.0772f, 2.6196f, 17.9864f, 3.1644f)
                lineTo(17.9864f, 3.1644f)
                lineTo(17.1817f, 7.9922f)
                lineTo(20.9994f, 7.99f)
                curveTo(21.5517f, 7.9896f, 21.9997f, 8.4371f, 22.0f, 8.9894f)
                curveTo(22.0003f, 9.5417f, 21.5529f, 9.9896f, 21.0006f, 9.99f)
                lineTo(21.0006f, 9.99f)
                lineTo(16.8484f, 9.9924f)
                lineTo(16.1814f, 13.9941f)
                lineTo(19.9996f, 13.9924f)
                curveTo(20.5519f, 13.9922f, 20.9998f, 14.4397f, 21.0f, 14.992f)
                curveTo(21.0003f, 15.5442f, 20.5528f, 15.9922f, 20.0005f, 15.9924f)
                lineTo(20.0005f, 15.9924f)
                lineTo(15.8481f, 15.9943f)
                lineTo(14.9864f, 21.1644f)
                curveTo(14.8956f, 21.7091f, 14.3803f, 22.0772f, 13.8356f, 21.9864f)
                curveTo(13.2908f, 21.8956f, 12.9228f, 21.3803f, 13.0136f, 20.8356f)
                lineTo(13.0136f, 20.8356f)
                lineTo(13.8203f, 15.9952f)
                lineTo(8.8448f, 15.9974f)
                lineTo(7.9831f, 21.1645f)
                curveTo(7.8923f, 21.7092f, 7.377f, 22.0772f, 6.8323f, 21.9864f)
                curveTo(6.2875f, 21.8955f, 5.9195f, 21.3802f, 6.0104f, 20.8355f)
                lineTo(6.0104f, 20.8355f)
                lineTo(6.817f, 15.9983f)
                lineTo(3.0005f, 16.0f)
                curveTo(2.4482f, 16.0002f, 2.0003f, 15.5527f, 2.0f, 15.0004f)
                curveTo(1.9998f, 14.4481f, 2.4473f, 14.0002f, 2.9996f, 14.0f)
                lineTo(2.9996f, 14.0f)
                lineTo(7.1505f, 13.9981f)
                lineTo(7.8176f, 9.9977f)
                lineTo(4.0006f, 10.0f)
                curveTo(3.4483f, 10.0003f, 3.0003f, 9.5528f, 3.0f, 9.0006f)
                curveTo(2.9997f, 8.4483f, 3.4471f, 8.0003f, 3.9994f, 8.0f)
                lineTo(3.9994f, 8.0f)
                lineTo(8.1512f, 7.9975f)
                lineTo(9.012f, 2.8355f)
                curveTo(9.1029f, 2.2907f, 9.6181f, 1.9227f, 10.1629f, 2.0136f)
                curveTo(10.7077f, 2.1044f, 11.0756f, 2.6197f, 10.9848f, 3.1645f)
                lineTo(10.9848f, 3.1645f)
                lineTo(10.179f, 7.9963f)
                lineTo(15.1539f, 7.9934f)
                lineTo(16.0136f, 2.8356f)
                curveTo(16.1044f, 2.2908f, 16.6196f, 1.9228f, 17.1644f, 2.0136f)
                close()
                moveTo(14.8206f, 9.9936f)
                lineTo(9.8455f, 9.9965f)
                lineTo(9.1783f, 13.9972f)
                lineTo(14.1537f, 13.995f)
                lineTo(14.8206f, 9.9936f)
                close()
            }
        }
            .build()
        return _icRevisions!!
    }

private var _icRevisions: ImageVector? = null
