package com.machiav3lli.backup.ui.compose.icons.phosphor

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Phosphor

val Phosphor.Broom: ImageVector
    get() {
        if (_Broom != null) {
            return _Broom!!
        }
        _Broom = ImageVector.Builder(
            name = "Broom",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f,
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(235.5f, 216.81f)
                curveToRelative(-22.56f, -11f, -35.5f, -34.58f, -35.5f, -64.8f)
                lineTo(200f, 134.73f)
                arcToRelative(
                    15.94f,
                    15.94f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -10.09f,
                    -14.87f
                )
                lineTo(165f, 110f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -4.48f,
                    -10.34f
                )
                lineToRelative(21.32f, -53f)
                arcToRelative(
                    28f,
                    28f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -16.1f,
                    -37f
                )
                arcToRelative(
                    28.14f,
                    28.14f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -35.82f,
                    16f
                )
                arcToRelative(
                    0.61f,
                    0.61f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    0f,
                    0.12f
                )
                lineTo(108.9f, 79f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -10.37f,
                    4.49f
                )
                lineTo(73.11f, 73.14f)
                arcTo(
                    15.89f,
                    15.89f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    55.74f,
                    76.8f
                )
                curveTo(34.68f, 98.45f, 24f, 123.75f, 24f, 152f)
                arcToRelative(
                    111.45f,
                    111.45f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    31.18f,
                    77.53f
                )
                arcTo(8f, 8f, 0f, isMoreThanHalf = false, isPositiveArc = false, 61f, 232f)
                lineTo(232f, 232f)
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    3.5f,
                    -15.19f
                )
                close()
                moveTo(67.14f, 88f)
                lineToRelative(25.41f, 10.3f)
                arcToRelative(
                    24f,
                    24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    31.23f,
                    -13.45f
                )
                lineToRelative(21f, -53f)
                curveToRelative(2.56f, -6.11f, 9.47f, -9.27f, 15.43f, -7f)
                arcToRelative(
                    12f,
                    12f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    6.88f,
                    15.92f
                )
                lineTo(145.69f, 93.76f)
                arcToRelative(
                    24f,
                    24f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    13.43f,
                    31.14f
                )
                lineTo(184f, 134.73f)
                lineTo(184f, 152f)
                curveToRelative(0f, 0.33f, 0f, 0.66f, 0f, 1f)
                lineTo(55.77f, 101.71f)
                arcTo(
                    108.84f,
                    108.84f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    67.14f,
                    88f
                )
                close()
                moveTo(115.14f, 216f)
                arcToRelative(
                    87.53f,
                    87.53f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    -24.34f,
                    -42f
                )
                arcToRelative(
                    8f,
                    8f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    -15.49f,
                    4f
                )
                arcToRelative(
                    105.16f,
                    105.16f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    18.36f,
                    38f
                )
                lineTo(64.44f, 216f)
                arcTo(95.54f, 95.54f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, 152f)
                arcToRelative(
                    85.9f,
                    85.9f,
                    0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    7.73f,
                    -36.29f
                )
                lineToRelative(137.8f, 55.12f)
                curveToRelative(3f, 18f, 10.56f, 33.48f, 21.89f, 45.16f)
                close()
            }
        }.build()

        return _Broom!!
    }

@Suppress("ObjectPropertyName")
private var _Broom: ImageVector? = null

@Preview
@Composable
fun BroomPreview() {
    Image(
        Phosphor.Broom,
        null
    )
}