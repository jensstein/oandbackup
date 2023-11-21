package com.machiav3lli.backup.ui.compose.item

import androidx.annotation.StringRes
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.machiav3lli.backup.ENABLED_FILTER_DISABLED
import com.machiav3lli.backup.ICON_SIZE_LARGE
import com.machiav3lli.backup.ICON_SIZE_MEDIUM
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.LATEST_FILTER_NEW
import com.machiav3lli.backup.LAUNCHABLE_FILTER_NOT
import com.machiav3lli.backup.MAIN_FILTER_SPECIAL
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.MODE_APK
import com.machiav3lli.backup.MODE_DATA
import com.machiav3lli.backup.MODE_DATA_DE
import com.machiav3lli.backup.MODE_DATA_EXT
import com.machiav3lli.backup.MODE_DATA_MEDIA
import com.machiav3lli.backup.MODE_DATA_OBB
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.SPECIAL_FILTER_ALL
import com.machiav3lli.backup.UPDATED_FILTER_NEW
import com.machiav3lli.backup.UPDATED_FILTER_NOT
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.preferences.pref_busyIconScale
import com.machiav3lli.backup.preferences.pref_busyIconTurnTime
import com.machiav3lli.backup.preferences.pref_hideBackupLabels
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.AsteriskSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.Checks
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.icons.phosphor.Clock
import com.machiav3lli.backup.ui.compose.icons.phosphor.DiamondsFour
import com.machiav3lli.backup.ui.compose.icons.phosphor.FloppyDisk
import com.machiav3lli.backup.ui.compose.icons.phosphor.GameController
import com.machiav3lli.backup.ui.compose.icons.phosphor.HardDrives
import com.machiav3lli.backup.ui.compose.icons.phosphor.Leaf
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldCheckered
import com.machiav3lli.backup.ui.compose.icons.phosphor.Spinner
import com.machiav3lli.backup.ui.compose.icons.phosphor.Star
import com.machiav3lli.backup.ui.compose.icons.phosphor.User
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.compose.theme.ColorAPK
import com.machiav3lli.backup.ui.compose.theme.ColorData
import com.machiav3lli.backup.ui.compose.theme.ColorDeData
import com.machiav3lli.backup.ui.compose.theme.ColorDisabled
import com.machiav3lli.backup.ui.compose.theme.ColorExodus
import com.machiav3lli.backup.ui.compose.theme.ColorExtDATA
import com.machiav3lli.backup.ui.compose.theme.ColorMedia
import com.machiav3lli.backup.ui.compose.theme.ColorOBB
import com.machiav3lli.backup.ui.compose.theme.ColorSpecial
import com.machiav3lli.backup.ui.compose.theme.ColorSystem
import com.machiav3lli.backup.ui.compose.theme.ColorUpdated
import com.machiav3lli.backup.ui.compose.theme.ColorUser
import com.machiav3lli.backup.utils.TraceUtils.beginNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.endNanoTimer
import kotlinx.coroutines.delay

@Composable
fun ButtonIcon(
    icon: ImageVector,
    @StringRes textId: Int,
    tint: Color = LocalContentColor.current,
) {
    beginNanoTimer("btnIcon")
    Image(
        imageVector = icon,
        contentDescription = stringResource(id = textId),
        modifier = Modifier.size(ICON_SIZE_SMALL),
        colorFilter = ColorFilter.tint(tint),
    )
    endNanoTimer("btnIcon")
}

@Composable
fun PrefIcon(
    icon: ImageVector,
    text: String,
    tint: Color? = null,
) {
    Icon(
        imageVector = icon,
        contentDescription = text,
        modifier = Modifier.size(ICON_SIZE_MEDIUM),   //TODO BUTTON_ICON_SIZE?
        tint = tint ?: MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun PackageIcon(
    modifier: Modifier = Modifier,
    item: Package?,
    imageData: Any,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
) {
    beginNanoTimer("pkgIcon.rCAIP")
    Image(
        modifier = modifier
            .size(ICON_SIZE_LARGE)
            .clip(MaterialTheme.shapes.medium),
        painter = cachedAsyncImagePainter(
            model = imageData,
            imageLoader = imageLoader,
            altPainter = placeholderIconPainter(item, imageLoader)
        ),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
    endNanoTimer("pkgIcon.rCAIP")
}

@Composable
fun RefreshButton(
    modifier: Modifier = Modifier,
    size: Dp = ICON_SIZE_SMALL,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    hideIfNotBusy: Boolean = false,
    onClick: () -> Unit = {},
) {
    val isBusy by remember { OABX.busy }

    if (hideIfNotBusy && isBusy.not())
        return

    val (angle, scale) = if (isBusy) {
        val infiniteTransition = rememberInfiniteTransition()

        // Animate from 0f to 1f
        val animationProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = pref_busyIconTurnTime.value,
                    easing = LinearEasing
                )
            )
        )
        val angle = 360f * animationProgress
        val scale = 0.01f * pref_busyIconScale.value
        angle to scale
    } else {
        0f to 1f
    }

    RoundButton(
        description = stringResource(id = R.string.refresh),
        icon = Phosphor.ArrowsClockwise,
        size = size,
        tint = if (isBusy) Color.Red else tint,
        modifier = modifier
            .scale(scale)
            .rotate(angle),
        onClick = onClick
    )
}

object IconCache {

    private var painterCache = mutableMapOf<Any, Painter>()

    fun getIcon(key: Any): Painter? {
        return synchronized(painterCache) {
            painterCache[key]
        }
    }

    fun putIcon(key: Any, painter: Painter) {
        //traceDebug { "icon put $key" }
        synchronized(painterCache) {
            painterCache.put(key, painter)
        }
    }

    fun removeIcon(key: Any) {
        traceDebug { "icon remove $key" }
        synchronized(painterCache) {
            painterCache.remove(key)
        }
    }

    fun clear() {
        synchronized(painterCache) {
            painterCache.clear()
        }
    }

    fun dropAllButUsed(pkgs: List<Package>) {
        val used = pkgs.map { it.iconData }.toSet()
        beginNanoTimer("limitIconCache")
        val keys = synchronized(painterCache) { painterCache.keys.toSet() }
        (keys - used).forEach {
            if (it !is Int) {
                removeIcon(it)
            }
        }
        endNanoTimer("limitIconCache")
    }

    val size: Int
        get() {
            return synchronized(painterCache) {
                painterCache.size
            }
        }
}

@Composable
fun cachedAsyncImagePainter(
    model: Any,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
    altPainter: Painter? = null,
): Painter {
    beginNanoTimer("rmbrCachedAIP")
    var painter = IconCache.getIcon(model)
    if (painter == null) {
        beginNanoTimer("rmbrAIP")
        val request =
            ImageRequest.Builder(LocalContext.current)
                .data(model)
                .size(Size.ORIGINAL)
                .build()
        val rememberedPainter =
            rememberAsyncImagePainter(
                model = request,
                imageLoader = imageLoader,
                onState = {
                    if (it !is AsyncImagePainter.State.Loading)
                        it.painter?.let { painter ->
                            IconCache.putIcon(model, painter)
                        }
                }
            )
        endNanoTimer("rmbrAIP")
        painter = if (rememberedPainter.state is AsyncImagePainter.State.Success) {
            //synchronized(painterCache) { painterCache.put(model, rememberedPainter) }
            rememberedPainter
        } else {
            altPainter ?: rememberedPainter
        }
    }
    endNanoTimer("rmbrCachedAIP")
    return painter
}

@Composable
fun placeholderIconPainter(
    item: Package?,
    imageLoader: ImageLoader = LocalContext.current.imageLoader,
) = cachedAsyncImagePainter(
    when {
        item?.isSpecial == true -> R.drawable.ic_placeholder_special
        item?.isSystem == true  -> R.drawable.ic_placeholder_system
        else                    -> R.drawable.ic_placeholder_user
    },
    imageLoader = imageLoader,
)

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    positive: Boolean = true,
    iconOnSide: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (positive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.tertiary
        ),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall
        )
        if (icon != null) {
            if (iconOnSide) Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
    }
}

@Composable
fun ElevatedActionButton(
    modifier: Modifier = Modifier,
    text: String,
    positive: Boolean = true,
    icon: ImageVector? = null,
    fullWidth: Boolean = false,
    enabled: Boolean = true,
    colored: Boolean = true,
    withText: Boolean = text.isNotEmpty(),
    onClick: () -> Unit,
) {
    ElevatedButton(
        modifier = modifier,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = when {
                !colored -> MaterialTheme.colorScheme.onSurfaceVariant
                positive -> MaterialTheme.colorScheme.onPrimaryContainer
                else     -> MaterialTheme.colorScheme.onTertiaryContainer
            },
            containerColor = when {
                !colored -> MaterialTheme.colorScheme.surfaceVariant
                positive -> MaterialTheme.colorScheme.primaryContainer
                else     -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        enabled = enabled,
        onClick = onClick
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        }
        if (withText)
            Text(
                modifier = when {
                    fullWidth -> Modifier.weight(1f)
                    else      -> Modifier.padding(start = 8.dp)
                },
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val showTooltip = remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showTooltip.value = true },
                enabled = enabled,
            ),
        colors = ListItemDefaults.colors(
            /*headlineColor = (if (isSystemInDarkTheme()) contentColor.brighter(0.3f)
            else contentColor.darker(0.3f)).copy(alpha = 0.8f),
            leadingIconColor = (if (isSystemInDarkTheme()) contentColor.brighter(0.3f)
            else contentColor.darker(0.3f)).copy(alpha = 0.8f),*/
            leadingIconColor = contentColor,
            headlineColor = contentColor,
            containerColor = containerColor,
        ),
        leadingContent = {
            Icon(imageVector = icon, contentDescription = description)
        },
        headlineContent = {
            Text(
                text = description,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall
            )

            if (showTooltip.value) {
                Tooltip(description, showTooltip)
            }
        }
    )
}

@Composable
fun Tooltip(
    text: String,
    openPopup: MutableState<Boolean>,
) {
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, 100),
    ) {
        LaunchedEffect(key1 = Unit) {
            delay(3000)
            openPopup.value = false
        }

        Box {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun RoundButton(
    modifier: Modifier = Modifier,
    size: Dp = ICON_SIZE_SMALL,
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    description: String = "",
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(size),
            imageVector = icon,
            tint = tint,
            contentDescription = description
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StateChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    color: Color,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val openPopup = remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { openPopup.value = true }
            ),
        contentColor = if (checked) MaterialTheme.colorScheme.background else color,
        color = if (checked) color else Color.Transparent,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, color),
    ) {
        Icon(
            modifier = Modifier.padding(8.dp),
            imageVector = icon,
            contentDescription = text,
        )

        if (openPopup.value) {
            Tooltip(text, openPopup)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckChip(
    modifier: Modifier = Modifier,
    checked: Boolean,
    textId: Int,
    checkedTextId: Int,
    onCheckedChange: (Boolean) -> Unit,
) {
    val (checked, check) = remember(checked) { mutableStateOf(checked) }   //TODO hg42 should probably be removed like for MultiChips

    FilterChip(
        modifier = modifier,
        selected = checked,
        colors = FilterChipDefaults.filterChipColors(
            labelColor = MaterialTheme.colorScheme.onBackground,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconColor = MaterialTheme.colorScheme.onBackground,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = Color.Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.medium,
        leadingIcon = {
            if (checked) ButtonIcon(Phosphor.Checks, R.string.enabled)
        },
        onClick = {
            onCheckedChange(!checked)
            check(!checked)
        },
        label = {
            Row(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(text = stringResource(id = if (checked) checkedTextId else textId))
            }
        }
    )
}

@Composable
fun ActionChip(
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector? = null,
    positive: Boolean,
    fullWidth: Boolean = false,
    onClick: () -> Unit = {},
) {
    AssistChip(
        modifier = modifier,
        label = {
            Text(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .ifThen(fullWidth) {
                        fillMaxWidth()
                    },
                text = text,
                textAlign = TextAlign.Center,
            )
        },
        leadingIcon = {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = text
                )
            }
        },
        shape = MaterialTheme.shapes.large,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (positive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
            leadingIconContentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        border = null,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchChip(
    firstTextId: Int,
    firstIcon: ImageVector,
    secondTextId: Int,
    secondIcon: ImageVector,
    firstSelected: Boolean = true,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (firstSelected, selectFirst) = remember { mutableStateOf(firstSelected) }   //TODO hg42 should probably be removed like for MultiChips

        FilterChip(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = null,
            selected = firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(true)
                selectFirst(true)
            },
            leadingIcon = {
                ButtonIcon(firstIcon, firstTextId)
            },
            label = {
                Text(
                    text = stringResource(id = firstTextId),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        )
        FilterChip(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
            border = null,
            selected = !firstSelected,
            colors = colors,
            onClick = {
                onCheckedChange(false)
                selectFirst(false)
            },
            label = {
                Text(
                    text = stringResource(id = secondTextId),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            },
            trailingIcon = {
                ButtonIcon(secondIcon, secondTextId)
            }
        )
    }
}

@Composable
fun SelectableRow(
    modifier: Modifier = Modifier,
    title: String,
    selectedState: MutableState<Boolean>,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                selectedState.value = true
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedState.value,
            onClick = {
                selectedState.value = true
                onClick()
            },
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(text = title)
    }
}

@Composable
fun CheckableRow(
    title: String,
    checkedState: MutableState<Boolean>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                checkedState.value = !checkedState.value
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState.value,
            onCheckedChange = {
                checkedState.value = it
            },
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
            )
        )
        Text(text = title)
    }
}

@Composable
fun StatefulAnimatedVisibility(
    currentState: Boolean = false,
    enterPositive: EnterTransition,
    exitPositive: ExitTransition,
    enterNegative: EnterTransition,
    exitNegative: ExitTransition,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit),
) {
    AnimatedVisibility(
        visible = currentState,
        enter = enterPositive,
        exit = exitPositive,
        content = expandedView
    )
    AnimatedVisibility(
        visible = !currentState,
        enter = enterNegative,
        exit = exitNegative,
        content = collapsedView
    )
}

@Composable
fun HorizontalExpandingVisibility(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit),
) = StatefulAnimatedVisibility(
    currentState = expanded,
    enterPositive = expandHorizontally(expandFrom = Alignment.End),
    exitPositive = shrinkHorizontally(shrinkTowards = Alignment.End),
    enterNegative = expandHorizontally(expandFrom = Alignment.Start),
    exitNegative = shrinkHorizontally(shrinkTowards = Alignment.Start),
    collapsedView = collapsedView,
    expandedView = expandedView
)

@Composable
fun VerticalFadingVisibility(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit),
) = StatefulAnimatedVisibility(
    currentState = expanded,
    enterPositive = fadeIn() + expandVertically(expandFrom = Alignment.Top),
    exitPositive = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
    enterNegative = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
    exitNegative = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
    collapsedView = collapsedView,
    expandedView = expandedView
)

@Composable
fun ExpandingFadingVisibility(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit),
) {
    val initColor = FloatingActionButtonDefaults.containerColor
    val expandedColor = MaterialTheme.colorScheme.surface
    val bgColor = remember {
        Animatable(initColor)
    }
    LaunchedEffect(key1 = expanded) {
        bgColor.animateTo(if (expanded) expandedColor else initColor)
    }
    Column(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                MaterialTheme.shapes.large
            )
            .background(
                bgColor.value,
                MaterialTheme.shapes.large
            )
    ) {
        StatefulAnimatedVisibility(
            currentState = expanded,
            enterPositive = fadeIn() + expandIn(),
            exitPositive = fadeOut() + shrinkOut(),
            enterNegative = fadeIn() + expandIn(),
            exitNegative = fadeOut() + shrinkOut(),
            collapsedView = collapsedView,
            expandedView = expandedView
        )
    }
}

@Composable
fun RowScope.PackageLabels(
    item: Package,
) {
    beginNanoTimer("pkgLabels")

    if (!pref_hideBackupLabels.value && item.hasBackups) {

        if (item.isUpdated) {
            ButtonIcon(
                Phosphor.CircleWavyWarning, R.string.radio_updated,
                tint = ColorUpdated
            )
        }
        if (item.hasMediaData) {
            ButtonIcon(
                Phosphor.PlayCircle, R.string.radio_mediadata,
                tint = ColorMedia
            )
        }
        if (item.hasObbData) {
            ButtonIcon(
                Phosphor.GameController, R.string.radio_obbdata,
                tint = ColorOBB
            )
        }
        if (item.hasExternalData) {
            ButtonIcon(
                Phosphor.FloppyDisk, R.string.radio_externaldata,
                tint = ColorExtDATA
            )
        }
        if (item.hasDevicesProtectedData) {
            ButtonIcon(
                Phosphor.ShieldCheckered, R.string.radio_deviceprotecteddata,
                tint = ColorDeData
            )
        }
        if (item.hasAppData) {
            ButtonIcon(
                Phosphor.HardDrives, R.string.radio_data,
                tint = ColorData
            )
        }
        if (item.hasApk) {
            ButtonIcon(
                Phosphor.DiamondsFour, R.string.radio_apk,
                tint = ColorAPK
            )
        }
    }

    ButtonIcon(
        when {
            item.isSpecial -> Phosphor.AsteriskSimple
            item.isSystem  -> Phosphor.Spinner
            else           -> Phosphor.User
        },
        R.string.app_s_type_title,
        tint = when {
            !item.isInstalled -> ColorDisabled
            item.isDisabled   -> ColorDisabled
            item.isSpecial    -> ColorSpecial
            item.isSystem     -> ColorSystem
            else              -> ColorUser
        }
    )

    endNanoTimer("pkgLabels")
}

@Composable
fun BackupLabels(
    item: Backup,
) {
    AnimatedVisibility(visible = item.hasMediaData) {
        ButtonIcon(
            Phosphor.PlayCircle, R.string.radio_mediadata,
            tint = ColorMedia
        )
    }
    AnimatedVisibility(visible = item.hasObbData) {
        ButtonIcon(
            Phosphor.GameController, R.string.radio_obbdata,
            tint = ColorOBB
        )
    }
    AnimatedVisibility(visible = item.hasExternalData) {
        ButtonIcon(
            Phosphor.FloppyDisk, R.string.radio_externaldata,
            tint = ColorExtDATA
        )
    }
    AnimatedVisibility(visible = item.hasDevicesProtectedData) {
        ButtonIcon(
            Phosphor.ShieldCheckered, R.string.radio_deviceprotecteddata,
            tint = ColorDeData
        )
    }
    AnimatedVisibility(visible = item.hasAppData) {
        ButtonIcon(
            Phosphor.HardDrives, R.string.radio_data,
            tint = ColorData
        )
    }
    AnimatedVisibility(visible = item.hasApk) {
        ButtonIcon(
            Phosphor.DiamondsFour, R.string.radio_apk,
            tint = ColorAPK
        )
    }
}


@Composable
fun ScheduleTypes(item: Schedule) {
    AnimatedVisibility(visible = item.mode and MODE_DATA_MEDIA == MODE_DATA_MEDIA) {
        ButtonIcon(
            Phosphor.PlayCircle, R.string.radio_mediadata,
            tint = ColorMedia
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_OBB == MODE_DATA_OBB) {
        ButtonIcon(
            Phosphor.GameController, R.string.radio_obbdata,
            tint = ColorOBB
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_EXT == MODE_DATA_EXT) {
        ButtonIcon(
            Phosphor.FloppyDisk, R.string.radio_externaldata,
            tint = ColorExtDATA
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA_DE == MODE_DATA_DE) {
        ButtonIcon(
            Phosphor.ShieldCheckered, R.string.radio_deviceprotecteddata,
            tint = ColorDeData
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_DATA == MODE_DATA) {
        ButtonIcon(
            Phosphor.HardDrives, R.string.radio_data,
            tint = ColorData
        )
    }
    AnimatedVisibility(visible = item.mode and MODE_APK == MODE_APK) {
        ButtonIcon(
            Phosphor.DiamondsFour, R.string.radio_apk,
            tint = ColorAPK
        )
    }
}


@Composable
fun ScheduleFilters(
    item: Schedule,
) {
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) {
        ButtonIcon(
            Phosphor.Spinner, R.string.radio_system,
            tint = ColorSystem
        )
    }
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_USER == MAIN_FILTER_USER) {
        ButtonIcon(
            Phosphor.User, R.string.radio_user,
            tint = ColorUser
        )
    }
    AnimatedVisibility(visible = item.filter and MAIN_FILTER_SPECIAL == MAIN_FILTER_SPECIAL) {
        ButtonIcon(
            Phosphor.AsteriskSimple, R.string.radio_special,
            tint = ColorSpecial
        )
    }
    AnimatedVisibility(visible = item.launchableFilter != SPECIAL_FILTER_ALL) {
        ButtonIcon(
            when (item.launchableFilter) {
                LAUNCHABLE_FILTER_NOT -> Phosphor.ProhibitInset
                else                  -> Phosphor.ArrowSquareOut // LAUNCHABLE_FILTER_LAUNCHABLE
            },

            when (item.launchableFilter) {
                LAUNCHABLE_FILTER_NOT -> R.string.radio_notlaunchable
                else                  -> R.string.radio_launchable // LAUNCHABLE_FILTER_LAUNCHABLE
            },
            tint = ColorOBB,
        )
    }
    AnimatedVisibility(visible = item.updatedFilter != SPECIAL_FILTER_ALL) {
        ButtonIcon(
            when (item.launchableFilter) {
                UPDATED_FILTER_NEW -> Phosphor.Star
                UPDATED_FILTER_NOT -> Phosphor.Clock
                else               -> Phosphor.CircleWavyWarning // UPDATED_FILTER_UPDATED
            },
            when (item.launchableFilter) {
                UPDATED_FILTER_NOT -> R.string.show_old_apps
                UPDATED_FILTER_NEW -> R.string.show_new_apps
                else               -> R.string.show_updated_apps // UPDATED_FILTER_UPDATED
            },
            tint = ColorUpdated,
        )
    }
    AnimatedVisibility(visible = item.enabledFilter != SPECIAL_FILTER_ALL) {
        ButtonIcon(
            when (item.launchableFilter) {
                ENABLED_FILTER_DISABLED -> Phosphor.ProhibitInset
                else                    -> Phosphor.Leaf // ENABLED_FILTER_ENABLED
            },

            when (item.launchableFilter) {
                ENABLED_FILTER_DISABLED -> R.string.showDisabled
                else                    -> R.string.show_enabled_apps // ENABLED_FILTER_ENABLED
            },
            tint = ColorDeData,
        )
    }
    AnimatedVisibility(visible = item.latestFilter != SPECIAL_FILTER_ALL) {
        ButtonIcon(
            when (item.launchableFilter) {
                LATEST_FILTER_NEW -> Phosphor.CircleWavyWarning
                else              -> Phosphor.Clock // LATEST_FILTER_OLD
            },

            when (item.launchableFilter) {
                LATEST_FILTER_NEW -> R.string.show_new_backups
                else              -> R.string.showOldBackups // LATEST_FILTER_OLD
            },
            tint = ColorExodus,
        )
    }
}

@Composable
fun TitleText(
    textId: Int,
    modifier: Modifier = Modifier,
) = Text(
    text = stringResource(id = textId),
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    modifier = modifier
)

@Composable
fun CategoryTitleText(
    textId: Int,
    modifier: Modifier = Modifier,
) = Text(
    modifier = modifier.fillMaxWidth(),
    text = stringResource(id = textId),
    textAlign = TextAlign.Center,
    style = MaterialTheme.typography.titleLarge
)

@Composable
fun DoubleVerticalText(
    upperText: String,
    bottomText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = upperText,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = bottomText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
fun CardSubRow(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit = {},
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        leadingContent = {
            Icon(imageVector = icon, contentDescription = text, tint = iconColor)
        },
        headlineContent = {
            Text(
                text = text,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        },
    )
}