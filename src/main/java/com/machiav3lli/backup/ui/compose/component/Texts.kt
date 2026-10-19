package com.machiav3lli.backup.ui.compose.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

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
            style = MaterialTheme.typography.bodyMedium.copy(MaterialTheme.colorScheme.onSurface)
        )
    }
}