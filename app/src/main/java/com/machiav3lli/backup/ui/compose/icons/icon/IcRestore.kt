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

val Icon.IcRestore: ImageVector
    get() {
        if (_icRestore != null) {
            return _icRestore!!
        }
        _icRestore = Builder(
            name = "IcRestore", defaultWidth = 32.0.dp, defaultHeight = 32.0.dp,
            viewportWidth = 256.0f, viewportHeight = 256.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(136.0f, 80.0f)
                verticalLineToRelative(43.381f)
                lineToRelative(37.569f, 21.691f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, true, -8.0f, 13.856f)
                lineToRelative(-41.569f, -24.0f)
                curveToRelative(-0.064f, -0.037f, -0.121f, -0.08f, -0.184f, -0.119f)
                curveToRelative(-0.136f, -0.084f, -0.271f, -0.168f, -0.401f, -0.259f)
                curveToRelative(-0.104f, -0.072f, -0.202f, -0.149f, -0.301f, -0.225f)
                curveToRelative(-0.102f, -0.079f, -0.204f, -0.158f, -0.301f, -0.241f)
                curveToRelative(-0.112f, -0.095f, -0.218f, -0.193f, -0.323f, -0.293f)
                curveToRelative(-0.079f, -0.075f, -0.158f, -0.151f, -0.234f, -0.229f)
                curveToRelative(-0.107f, -0.11f, -0.208f, -0.224f, -0.308f, -0.339f)
                curveToRelative(-0.069f, -0.08f, -0.138f, -0.161f, -0.204f, -0.244f)
                curveToRelative(-0.092f, -0.116f, -0.179f, -0.234f, -0.264f, -0.354f)
                curveToRelative(-0.067f, -0.095f, -0.133f, -0.19f, -0.196f, -0.288f)
                curveToRelative(-0.072f, -0.112f, -0.141f, -0.225f, -0.207f, -0.341f)
                curveToRelative(-0.066f, -0.114f, -0.13f, -0.23f, -0.19f, -0.348f)
                curveToRelative(-0.053f, -0.104f, -0.104f, -0.209f, -0.152f, -0.316f)
                curveToRelative(-0.061f, -0.133f, -0.119f, -0.267f, -0.173f, -0.403f)
                curveToRelative(-0.039f, -0.1f, -0.075f, -0.201f, -0.11f, -0.302f)
                curveToRelative(-0.05f, -0.143f, -0.097f, -0.286f, -0.138f, -0.432f)
                curveToRelative(-0.03f, -0.107f, -0.057f, -0.215f, -0.083f, -0.323f)
                curveToRelative(-0.033f, -0.139f, -0.065f, -0.279f, -0.091f, -0.421f)
                curveToRelative(-0.024f, -0.128f, -0.041f, -0.258f, -0.059f, -0.387f)
                curveToRelative(-0.017f, -0.122f, -0.033f, -0.244f, -0.044f, -0.368f)
                curveToRelative(-0.014f, -0.16f, -0.02f, -0.321f, -0.024f, -0.482f)
                curveTo(120.009f, 128.143f, 120.0f, 128.073f, 120.0f, 128.0f)
                lineTo(120.0f, 80.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, 16.0f, 0.0f)
                close()
                moveTo(195.882f, 60.118f)
                arcToRelative(96.108f, 96.108f, 0.0f, false, false, -135.764f, 0.0f)
                lineTo(39.833f, 80.402f)
                lineTo(39.833f, 59.716f)
                arcToRelative(8.0f, 8.0f, 0.0f, true, false, -16.0f, 0.0f)
                verticalLineToRelative(40.0f)
                lineToRelative(0.001f, 0.022f)
                quadToRelative(0.001f, 0.384f, 0.039f, 0.767f)
                curveToRelative(0.012f, 0.121f, 0.034f, 0.239f, 0.051f, 0.358f)
                curveToRelative(0.021f, 0.139f, 0.036f, 0.278f, 0.063f, 0.416f)
                curveToRelative(0.027f, 0.136f, 0.064f, 0.268f, 0.099f, 0.402f)
                curveToRelative(0.03f, 0.119f, 0.056f, 0.239f, 0.091f, 0.357f)
                curveToRelative(0.04f, 0.132f, 0.089f, 0.26f, 0.136f, 0.389f)
                curveToRelative(0.042f, 0.117f, 0.08f, 0.235f, 0.128f, 0.35f)
                curveToRelative(0.05f, 0.121f, 0.108f, 0.237f, 0.164f, 0.354f)
                curveToRelative(0.057f, 0.119f, 0.109f, 0.239f, 0.172f, 0.356f)
                curveToRelative(0.061f, 0.114f, 0.13f, 0.222f, 0.196f, 0.332f)
                curveToRelative(0.067f, 0.113f, 0.131f, 0.228f, 0.205f, 0.338f)
                curveToRelative(0.083f, 0.124f, 0.176f, 0.241f, 0.266f, 0.361f)
                curveToRelative(0.067f, 0.089f, 0.129f, 0.181f, 0.201f, 0.268f)
                arcToRelative(8.034f, 8.034f, 0.0f, false, false, 1.119f, 1.119f)
                curveToRelative(0.085f, 0.07f, 0.176f, 0.131f, 0.264f, 0.197f)
                curveToRelative(0.121f, 0.091f, 0.239f, 0.184f, 0.365f, 0.269f)
                curveToRelative(0.109f, 0.073f, 0.222f, 0.136f, 0.333f, 0.203f)
                curveToRelative(0.112f, 0.067f, 0.222f, 0.138f, 0.337f, 0.2f)
                curveToRelative(0.115f, 0.061f, 0.232f, 0.113f, 0.349f, 0.168f)
                curveToRelative(0.12f, 0.057f, 0.238f, 0.117f, 0.361f, 0.168f)
                curveToRelative(0.113f, 0.046f, 0.228f, 0.084f, 0.342f, 0.125f)
                curveToRelative(0.132f, 0.048f, 0.263f, 0.098f, 0.397f, 0.139f)
                curveToRelative(0.114f, 0.034f, 0.229f, 0.059f, 0.344f, 0.088f)
                curveToRelative(0.138f, 0.035f, 0.274f, 0.073f, 0.415f, 0.101f)
                curveToRelative(0.131f, 0.026f, 0.263f, 0.041f, 0.394f, 0.06f)
                curveToRelative(0.127f, 0.019f, 0.251f, 0.042f, 0.38f, 0.055f)
                curveToRelative(0.232f, 0.023f, 0.466f, 0.033f, 0.699f, 0.035f)
                curveToRelative(0.029f, 0.0f, 0.058f, 0.004f, 0.087f, 0.004f)
                horizontalLineToRelative(40.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, 0.0f, -16.0f)
                lineTo(51.147f, 91.716f)
                lineTo(71.432f, 71.431f)
                arcToRelative(80.0f, 80.0f, 0.0f, true, true, 0.0f, 113.137f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, false, -11.314f, 11.314f)
                arcTo(96.0f, 96.0f, 0.0f, false, false, 195.882f, 60.118f)
                close()
            }
        }
            .build()
        return _icRestore!!
    }

private var _icRestore: ImageVector? = null
