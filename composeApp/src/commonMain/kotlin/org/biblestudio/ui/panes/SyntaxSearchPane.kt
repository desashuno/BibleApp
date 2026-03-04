package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.search.component.SearchState
import org.biblestudio.ui.components.LoadingErrorContent
import org.biblestudio.ui.theme.Spacing

/**
 * Syntax Search pane: advanced morphology-aware search with syntax help popover.
 *
 * Supported syntax:
 * - `[LEMMA:H1234]` — Strong's number lookup
 * - `[POS:Noun]` — Part-of-speech filter
 * - `[WITHIN 3 WORDS]` — Proximity constraint
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun SyntaxSearchPane(
    stateFlow: StateFlow<SearchState>,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onResultTapped: (Verse) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Syntax Search",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(Spacing.Space4),
                modifier = Modifier.clickable { showHelp = !showHelp }
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = Spacing.Space12,
                        vertical = Spacing.Space4
                    )
                )
            }
        }

        // Help popover
        if (showHelp) {
            SyntaxHelpCard()
        }

        // Query field
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            placeholder = { Text("[LEMMA:H1234] [POS:Noun]") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Space16)
        )

        Spacer(modifier = Modifier.height(Spacing.Space8))
        HorizontalDivider()

        // Results
        LoadingErrorContent(
            isLoading = state.isSearching,
            error = state.error,
            data = state.results,
            emptyMessage = if (state.query.isNotBlank()) "No results found." else "",
            modifier = Modifier.fillMaxSize(),
            isEmpty = { it.isEmpty() && state.query.isNotBlank() },
        ) { _ ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (state.results.isNotEmpty()) {
                    item {
                        Text(
                            text = "${state.resultCount} result(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(Spacing.Space16)
                        )
                    }
                    items(state.results, key = { it.id }) { verse ->
                        SyntaxResultRow(verse = verse, onClick = { onResultTapped(verse) })
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SyntaxHelpCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space16)) {
            Text(
                text = "Syntax Reference",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Spacing.Space8)
            )
            SyntaxHelpRow("[LEMMA:H1234]", "Search by Strong's number")
            SyntaxHelpRow("[POS:Noun]", "Search by part of speech")
            SyntaxHelpRow("[WITHIN 3 WORDS]", "Proximity constraint")
            SyntaxHelpRow("[LEMMA:H430] [POS:Verb]", "Combined search (AND)")
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SyntaxHelpRow(syntax: String, description: String) {
    Row(modifier = Modifier.padding(vertical = Spacing.Space2)) {
        Text(
            text = syntax,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(Spacing.Space48 * 4)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun SyntaxResultRow(verse: Verse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Text(
            text = "Verse ${verse.globalVerseId}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.Space2))
        Text(
            text = verse.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Space16))
}
