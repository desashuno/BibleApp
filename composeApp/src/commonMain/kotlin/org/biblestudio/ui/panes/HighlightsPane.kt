package org.biblestudio.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.highlights.component.HighlightState
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.entities.HighlightColor
import org.biblestudio.ui.theme.AppColors
import org.biblestudio.ui.theme.Spacing

/**
 * Highlights manager pane: colour palette + highlight list.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun HighlightsPane(
    stateFlow: StateFlow<HighlightState>,
    onColorSelected: (HighlightColor) -> Unit,
    onDeleteHighlight: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.Space16)
    ) {
        Text(
            text = "Highlights",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = Spacing.Space8)
        )

        // ── Colour palette ──
        ColorPalette(
            selectedColor = state.selectedColor,
            onColorSelected = onColorSelected
        )

        Spacer(modifier = Modifier.height(Spacing.Space16))

        // ── Highlight list ──
        if (state.highlights.isEmpty()) {
            Text(
                text = "No highlights for this verse",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else {
            LazyColumn {
                items(state.highlights, key = { it.uuid }) { highlight ->
                    HighlightRow(
                        highlight = highlight,
                        onDelete = { onDeleteHighlight(highlight.uuid) }
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun ColorPalette(selectedColor: HighlightColor, onColorSelected: (HighlightColor) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space8),
        modifier = Modifier.fillMaxWidth()
    ) {
        HighlightColor.entries.forEach { color ->
            val paletteColor = AppColors.highlights[color.index.toInt()]
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(paletteColor)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun HighlightRow(highlight: Highlight, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.Space4)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.Space8)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AppColors.highlights[highlight.colorIndex.toInt()])
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.Space8)
            ) {
                Text(
                    text = VerseRefFormatter.format(highlight.globalVerseId),
                    style = MaterialTheme.typography.bodyMedium
                )
                val range = if (highlight.endOffset == -1L) {
                    "Whole verse"
                } else {
                    "Offset ${highlight.startOffset}–${highlight.endOffset}"
                }
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
