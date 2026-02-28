package org.biblestudio.ui.panes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.search.component.SearchScope
import org.biblestudio.features.search.component.SearchState
import org.biblestudio.ui.theme.Spacing

/**
 * Search pane: query field, scope tabs, results list, and history section.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun SearchPane(
    stateFlow: StateFlow<SearchState>,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    onScopeChanged: (SearchScope) -> Unit,
    onResultTapped: (Verse) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val scopes = SearchScope.entries

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Space16)
        )

        // Scope tabs
        ScrollableTabRow(
            selectedTabIndex = scopes.indexOf(state.scope),
            edgePadding = Spacing.Space16,
            modifier = Modifier.fillMaxWidth()
        ) {
            scopes.forEach { scope ->
                Tab(
                    selected = state.scope == scope,
                    onClick = { onScopeChanged(scope) },
                    text = { Text(scope.name) }
                )
            }
        }

        HorizontalDivider()

        if (state.isSearching) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.Space24)
            )
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(Spacing.Space16)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Results
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
                        SearchResultRow(
                            verse = verse,
                            query = state.query,
                            onClick = { onResultTapped(verse) }
                        )
                    }
                }

                // History
                if (state.query.isBlank() && state.history.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.Space16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable(onClick = onClearHistory)
                            )
                        }
                    }
                    items(state.history, key = { it.id }) { entry ->
                        Text(
                            text = entry.query,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQueryChanged(entry.query) }
                                .padding(
                                    horizontal = Spacing.Space16,
                                    vertical = Spacing.Space8
                                )
                        )
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun SearchResultRow(verse: Verse, query: String = "", onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Text(
            text = VerseRefFormatter.format(verse.globalVerseId),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(Spacing.Space2))
        if (query.isNotBlank()) {
            Text(
                text = highlightMatches(verse.text, query),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        } else {
            Text(
                text = verse.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Space16))
}

/**
 * Builds an [AnnotatedString] that highlights all case-insensitive occurrences
 * of [query] terms in [text].
 */
@Composable
private fun highlightMatches(text: String, query: String): AnnotatedString {
    val highlightStyle = SpanStyle(
        background = MaterialTheme.colorScheme.primaryContainer,
        fontWeight = FontWeight.Bold
    )
    return buildAnnotatedString {
        val terms = query.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val lowerText = text.lowercase()

        // Collect all match ranges
        val ranges = mutableListOf<IntRange>()
        for (term in terms) {
            val lowerTerm = term.lowercase()
            var startIndex = 0
            while (true) {
                val found = lowerText.indexOf(lowerTerm, startIndex)
                if (found < 0) break
                ranges.add(found until (found + lowerTerm.length))
                startIndex = found + 1
            }
        }

        // Merge overlapping ranges and sort
        val merged = mergeRanges(ranges)
        var cursor = 0
        for (range in merged) {
            if (cursor < range.first) {
                append(text.substring(cursor, range.first))
            }
            withStyle(highlightStyle) {
                append(text.substring(range.first, range.last + 1))
            }
            cursor = range.last + 1
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}

private fun mergeRanges(ranges: List<IntRange>): List<IntRange> {
    if (ranges.isEmpty()) return emptyList()
    val sorted = ranges.sortedBy { it.first }
    val result = mutableListOf(sorted[0])
    for (r in sorted.drop(1)) {
        val last = result.last()
        if (r.first <= last.last + 1) {
            result[result.lastIndex] = last.first..maxOf(last.last, r.last)
        } else {
            result.add(r)
        }
    }
    return result
}
