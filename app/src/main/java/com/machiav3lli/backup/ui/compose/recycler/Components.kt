package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.preferences.pref_busyFadeTime
import com.machiav3lli.backup.preferences.pref_busyLaserBackground
import com.machiav3lli.backup.preferences.pref_busyTurnTime
import com.machiav3lli.backup.preferences.pref_versionOpacity
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.SelectionChip
import com.machiav3lli.backup.ui.item.ChipItem
import com.machiav3lli.backup.utils.SystemUtils.applicationIssuer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun <T : Any> VerticalItemList(
    modifier: Modifier = Modifier,
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("VerticalItemList"),
        contentAlignment = if (list.isNullOrEmpty()) Alignment.Center else Alignment.TopStart
    ) {
        when {
            list == null   -> Text(
                text = stringResource(id = R.string.loading_list),
                color = MaterialTheme.colorScheme.onBackground
            )

            list.isEmpty() -> Text(
                text = stringResource(id = R.string.empty_filtered_list),
                color = MaterialTheme.colorScheme.onBackground
            )

            else           -> {
                // TODO add scrollbars
                val state = rememberLazyListState()

                LazyColumn(
                    state = state,
                    modifier = modifier
                        .testTag("VerticalItemList.Column"),
                    verticalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    itemsIndexed(
                        items = list,
                        itemContent = { _: Int, it: T ->
                            itemContent(it)
                        },
                        key = { index: Int, it: T ->
                            itemKey?.invoke(it) ?: index
                        }
                    )
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)  // workaround for floating buttons hiding the elements
                            // unfortunately the sizes are private
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <T> SizedItemList(
    modifier: Modifier = Modifier,
    itemHeight: Int,
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .requiredHeight(if (list != null) (list.size * (8 + itemHeight) + 8).dp else 20.dp),
        contentAlignment = if (list.isNullOrEmpty()) Alignment.Center else Alignment.TopStart
    ) {
        when {
            list == null   -> Text(
                text = stringResource(id = R.string.loading_list),
                color = MaterialTheme.colorScheme.onBackground
            )

            list.isEmpty() -> Text(
                text = stringResource(id = R.string.empty_filtered_list),
                color = MaterialTheme.colorScheme.onBackground
            )

            else           -> {
                // TODO add scrollbars
                LazyColumn(
                    verticalArrangement = Arrangement.Absolute.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                ) {
                    items(items = list, key = itemKey, itemContent = itemContent)
                }
            }
        }
    }
}

@Composable
fun <T> HorizontalItemList(
    modifier: Modifier = Modifier,
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = if (list.isNullOrEmpty()) Alignment.Center else Alignment.CenterStart
    ) {
        when {
            list == null   -> Text(
                text = stringResource(id = R.string.loading_list),
                color = MaterialTheme.colorScheme.onBackground
            )

            list.isEmpty() -> Text(
                text = stringResource(id = R.string.empty_filtered_list),
                color = MaterialTheme.colorScheme.onBackground
            )

            else           -> {
                LazyRow(
                    horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                ) {
                    items(items = list, key = itemKey, itemContent = itemContent)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectableChipGroup(
    //TODO hg42 move to item/Components.kt ?
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlag: Int,
    onClick: (Int) -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        list.forEach { item ->
            SelectionChip(
                item = item,
                isSelected = item.flag == selectedFlag,
            ) {
                onClick(item.flag)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MultiSelectableChipGroup(
    //TODO hg42 move to item/Components.kt ?
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlags: Int,
    onClick: (Int, Int) -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        list.forEach { item ->
            SelectionChip(
                item = item,
                isSelected = item.flag and selectedFlags != 0,
            ) {
                onClick(selectedFlags xor item.flag, item.flag)
            }
        }
    }
}

fun Modifier.angledGradientBackground(colors: List<Color>, degrees: Float, factor: Float = 1f) =
    this.then(
        drawBehind {

            val (w, h) = size
            val dim = max(w, h) * factor

            val degreesNormalised = (degrees % 360).let { if (it < 0) it + 360 else it }

            val alpha = (degreesNormalised * PI / 180).toFloat()

            val centerOffsetX = cos(alpha) * dim / 2
            val centerOffsetY = sin(alpha) * dim / 2

            drawRect(
                brush = Brush.linearGradient(
                    colors = colors,
                    // negative here so that 0 degrees is left -> right
                    // and 90 degrees is top -> bottom
                    start = Offset(center.x - centerOffsetX, center.y - centerOffsetY),
                    end = Offset(center.x + centerOffsetX, center.y + centerOffsetY)
                ),
                size = size
            )
        }
    )

fun Modifier.busyBackground(
    angle: Float,
    color0: Color,
    color1: Color,
    color2: Color,
): Modifier {
    val factor = 0.2f
    return this
        .angledGradientBackground(
            listOf(
                color0,
                color0,
                color1,
                color1,
                color0,
                color0,
                color0,
                color0,
                color0,
            ), angle, factor
        )
        .angledGradientBackground(
            listOf(
                color0,
                color0,
                color2,
                color2,
                color0,
                color0,
                color0,
                color0,
                color0,
            ), angle * 1.5f, factor
        )
}

@Composable
fun BusyBackgroundAnimated(
    busy: Boolean,
    content: @Composable () -> Unit,
) {
    val rounds = 12
    val color0 = Color.Transparent
    val color1 = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    val color2 = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.20f)

    val inTime = pref_busyFadeTime.value
    val outTime = pref_busyFadeTime.value
    val turnTime = pref_busyTurnTime.value

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = busy,
            enter = fadeIn(tween(inTime)),
            exit = fadeOut(tween(outTime)),
            modifier = Modifier
                .fillMaxSize()
        ) {
            //var angle by rememberSaveable { mutableStateOf(70f) }
            var angle by rememberSaveable { mutableFloatStateOf(System.currentTimeMillis() % turnTime * 360f / turnTime) }
            LaunchedEffect(true) {
                withContext(Dispatchers.IO) {
                    animate(
                        initialValue = angle,
                        targetValue = angle + 360f * rounds,
                        animationSpec = infiniteRepeatable(
                            animation = tween(turnTime * rounds, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                            //repeatMode = RepeatMode.Reverse
                        )
                    ) { value, _ -> angle = value }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .busyBackground(angle, color0, color1, color2)
            ) {
            }
        }

        content()
    }
}

@Composable
fun BusyBackgroundColor(
    busy: Boolean,
    content: @Composable () -> Unit,
) {
    val inTime = pref_busyFadeTime.value
    val outTime = pref_busyFadeTime.value

    AnimatedVisibility(
        visible = busy,
        enter = fadeIn(tween(inTime)),
        exit = fadeOut(tween(outTime)),
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Gray.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
        }
    }

    content()
}

@Composable
fun BusyBackground(
    modifier: Modifier = Modifier,
    busy: State<Boolean>? = null,
    content: @Composable () -> Unit,
) {
    val isBusy by remember { busy ?: OABX.busy }
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (pref_busyLaserBackground.value)
            BusyBackgroundAnimated(busy = isBusy, content = content)
        else
            BusyBackgroundColor(busy = isBusy, content = content)

        if (pref_versionOpacity.value > 0)
            Text(
                text = "${BuildConfig.VERSION_NAME} $applicationIssuer",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = pref_versionOpacity.value / 100f),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .padding(16.dp, 0.dp, 0.dp, 0.dp)
            )
    }
}

@Preview
@Composable
fun BusyBackgroundPreview() {
    val busy = remember { mutableStateOf(true) }
    //var progress by remember { mutableStateOf(true) }

    Column {
        Row {
            ActionChip(text = "busy ${busy.value}", positive = busy.value) {
                busy.value = !busy.value
            }
        }
        BusyBackground(
            modifier = Modifier
                .fillMaxSize(),
            busy = busy
        ) {
            Text(
                """
                Hello,
                here I am
                to conquer
                the world
            """.trimIndent(),
                fontSize = 48.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(align = Alignment.Center)
            )
        }
    }
}
