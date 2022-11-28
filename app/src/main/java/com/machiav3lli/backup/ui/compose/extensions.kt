package com.machiav3lli.backup.ui.compose

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import com.machiav3lli.backup.preferences.pref_useSelectableText
import com.machiav3lli.backup.traceFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

@Composable
fun SelectionContainerX(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (pref_useSelectableText.value)
        SelectionContainer(modifier = modifier, content = content)
    else
        content()
}


class MutableComposableSharedFlow<T>(
    var initial: T,
    val scope: CoroutineScope,
    val label: String = "ComposableSharedFlow"
) {
    var flow = MutableSharedFlow<T>()

    var state = flow
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            initial
        )

    var value: T
        get() {
            val value = state.value
            traceFlows { "*** $label => $value" }
            return value
        }
        set(value: T) {
            traceFlows { "*** $label <= $value" }
            initial = value
            scope.launch { flow.emit(value) }
        }

    init {
        value = initial
    }
}

class MutableComposableStateFlow<T>(
    var initial: T,
    val scope: CoroutineScope,
    val label: String = "ComposableStateFlow"
) {
    var flow = MutableStateFlow<T>(initial)

    val state = flow.asStateFlow()

    var value: T
        get() {
            val value = state.value
            traceFlows { "*** $label => $value" }
            return value
        }
        set(value: T) {
            traceFlows { "*** $label <= $value" }
            //initial = value
            scope.launch { flow.update { value } }
        }

    init {
        value = initial
    }
}

//typealias MutableComposableFlow<T> = MutableComposableSharedFlow<T>
typealias MutableComposableFlow<T> = MutableComposableStateFlow<T>
