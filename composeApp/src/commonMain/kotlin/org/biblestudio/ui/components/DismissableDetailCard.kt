package org.biblestudio.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.biblestudio.ui.theme.Spacing

/**
 * Card with a title, a dismiss (✕) button, and a content slot.
 *
 * Used for entity detail cards, timeline event cards, and location detail cards.
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun DismissableDetailCard(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.Space8),
    ) {
        Column(modifier = Modifier.padding(Spacing.Space16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "✕",
                    modifier = Modifier.clickable(onClick = onDismiss).padding(Spacing.Space4),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            content()
        }
    }
}
