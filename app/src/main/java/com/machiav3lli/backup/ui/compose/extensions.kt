package com.machiav3lli.backup.ui.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.machiav3lli.backup.traceFlows
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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

@Composable
inline fun Modifier.ifThen(
    boolean: Boolean,
    crossinline modifier: @Composable Modifier.() -> Modifier,
): Modifier {
    return if (boolean) {
        modifier.invoke(this)
    } else {
        this
    }
}

fun Modifier.blockBorder() = composed {
    this
        .clip(MaterialTheme.shapes.extraLarge)
        .border(
            2.dp,
            MaterialTheme.colorScheme.outlineVariant,
            MaterialTheme.shapes.extraLarge,
        )
}

@Composable
fun SelectionContainerX(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    SelectionContainer(modifier = modifier, content = content)
    //content()
}

@Composable
fun <T> ObservedEffect(flow: Flow<T?>, onChange: (T?) -> Unit) {
    val lcOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lcOwner.lifecycle) {
        lcOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(onChange)
        }
    }
}

@Composable
fun ObservedEffect(onChange: () -> Unit) {
    val lcOwner = LocalLifecycleOwner.current
    LaunchedEffect(lcOwner.lifecycle) {
        lcOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            onChange()
        }
    }
}


class MutableComposableSharedFlow<T>(
    var initial: T,
    val scope: CoroutineScope,
    val label: String = "ComposableSharedFlow",
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
            if (value is String)
                traceFlows { "*** $label => '$value'" }
            else
                traceFlows { "*** $label => $value" }
            return value
        }
        set(value: T) {
            if (value is String)
                traceFlows { "*** $label <= '$value'" }
            else
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
    val label: String = "ComposableStateFlow",
) {
    var flow = MutableStateFlow<T>(initial)

    val state = flow.asStateFlow()

    var value: T
        get() {
            val value = state.value
            if (value is String)
                traceFlows { "*** $label => '$value'" }
            else
                traceFlows { "*** $label => $value" }
            return value
        }
        set(value: T) {
            if (value is String)
                traceFlows { "*** $label <= '$value'" }
            else
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


@Composable
fun LazyListState.isAtTop() = remember {
    derivedStateOf {
        firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
    }
}.value

@Composable
fun LazyListState.isAtBottom() = remember {
    derivedStateOf {
        try {
            layoutInfo.visibleItemsInfo.last().index >= layoutInfo.totalItemsCount - 1
        } catch (_: Throwable) {
            true
        }
    }
}.value

