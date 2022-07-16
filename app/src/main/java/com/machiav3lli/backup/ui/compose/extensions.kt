package com.machiav3lli.backup.ui.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

fun Modifier.vertical() = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.height, placeable.width) {
        placeable.place(
            x = -(placeable.width / 2 - placeable.height / 2),
            y = -(placeable.height / 2 - placeable.width / 2)
        )
    }
}

fun Modifier.ifThen(boolean: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (boolean) {
        modifier.invoke(this)
    } else {
        this
    }
}
