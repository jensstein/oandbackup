package com.machiav3lli.backup.ui.compose.icons.phosphor


import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.Phosphor

val Phosphor.HardDrive: ImageVector
    get() {
        if (_hard_drive != null) {
            return _hard_drive!!
        }
        _hard_drive = Builder(
            name = "Hard-drive",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 256.0f,
            viewportHeight = 256.0f,
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(224.0f, 64.0f)
                lineTo(32.0f, 64.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 16.0f, 80.0f)
                verticalLineToRelative(96.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, 16.0f)
                lineTo(224.0f, 192.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, false, 16.0f, -16.0f)
                lineTo(240.0f, 80.0f)
                arcTo(16.0f, 16.0f, 0.0f, false, false, 224.0f, 64.0f)
                close()
                moveTo(224.0f, 176.0f)
                lineTo(32.0f, 176.0f)
                lineTo(32.0f, 80.0f)
                lineTo(224.0f, 80.0f)
                verticalLineToRelative(96.0f)
                close()
                moveTo(200.0f, 128.0f)
                arcToRelative(12.0f, 12.0f, 0.0f, true, true, -12.0f, -12.0f)
                arcTo(12.0f, 12.0f, 0.0f, false, true, 200.0f, 128.0f)
                close()
            }
        }
            .build()
        return _hard_drive!!
    }

private var _hard_drive: ImageVector? = null



@Preview
@Composable
fun HardDrivePreview() {
    Image(
        Phosphor.HardDrive,
        null
    )
}
