package org.biblestudio.ui.panes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.component.BibleReaderState
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.ui.theme.AppColors
import org.biblestudio.ui.theme.Spacing

/**
 * Bible Reader pane: displays chapter text with verse numbers in a scrollable list.
 *
 * - Verse numbers are superscript in the accent color
 * - Tapping a verse triggers [onVerseTapped]
 * - Long-pressing a verse starts/extends selection via [onVerseLongPressed]
 * - Book/chapter picker bottom sheet when header is tapped
 * - Automatically scrolls to [BibleReaderState.scrollToVerseId] when set
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderPane(
    stateFlow: StateFlow<BibleReaderState>,
    onVerseTapped: (Verse) -> Unit,
    onVerseLongPressed: (Verse) -> Unit,
    onBookChapterSelected: (bookId: Long, chapter: Long) -> Unit,
    onToggleBookPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll when scrollToVerseId changes
    LaunchedEffect(state.scrollToVerseId) {
        val targetId = state.scrollToVerseId ?: return@LaunchedEffect
        val index = state.verses.indexOfFirst { it.globalVerseId == targetId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    // Book/chapter picker bottom sheet
    if (state.showBookPicker) {
        BookChapterPickerSheet(
            books = state.books,
            currentBookId = state.currentBook?.id,
            onBookChapterSelected = { bookId, chapter ->
                onBookChapterSelected(bookId, chapter)
                onToggleBookPicker()
            },
            onDismiss = onToggleBookPicker
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Chapter header (tappable to open book picker)
        val book = state.currentBook
        if (book != null) {
            Text(
                text = "${book.name} ${state.currentChapter}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable(onClick = onToggleBookPicker)
                    .padding(Spacing.Space16)
            )
            HorizontalDivider()
        }

        if (state.isLoading) {
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
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Space16)
            ) {
                items(state.verses, key = { it.id }) { verse ->
                    val isSelected = state.selectedVerseRange?.let { range ->
                        verse.globalVerseId in range.startVerseId..range.endVerseId
                    } ?: false
                    val verseHighlights = state.highlights[verse.globalVerseId].orEmpty()
                    VerseRow(
                        verse = verse,
                        isSelected = isSelected,
                        highlights = verseHighlights,
                        onClick = { onVerseTapped(verse) },
                        onLongClick = { onVerseLongPressed(verse) }
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming", "MagicNumber", "LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerseRow(
    verse: Verse,
    isSelected: Boolean,
    highlights: List<Highlight>,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript
            )
        ) {
            append("${verse.verseNumber} ")
        }
        val text = verse.text
        if (highlights.isEmpty()) {
            append(text)
        } else {
            // Apply highlight spans; for whole-verse highlights paint entire text
            val wholeVerse = highlights.firstOrNull { it.endOffset == -1L }
            if (wholeVerse != null) {
                val colorIdx = wholeVerse.colorIndex.toInt()
                    .coerceIn(0, AppColors.highlights.lastIndex)
                val color = AppColors.highlights[colorIdx]
                withStyle(SpanStyle(background = color)) {
                    append(text)
                }
            } else {
                // Sub-verse highlights: sort by startOffset and paint segments
                val sorted = highlights.sortedBy { it.startOffset }
                var cursor = 0
                for (hl in sorted) {
                    val start = hl.startOffset.toInt().coerceIn(0, text.length)
                    val end = hl.endOffset.toInt().coerceIn(start, text.length)
                    if (cursor < start) append(text.substring(cursor, start))
                    val colorIdx = hl.colorIndex.toInt()
                        .coerceIn(0, AppColors.highlights.lastIndex)
                    val color = AppColors.highlights[colorIdx]
                    withStyle(SpanStyle(background = color)) {
                        append(text.substring(start, end))
                    }
                    cursor = end
                }
                if (cursor < text.length) append(text.substring(cursor))
            }
        }
    }

    val background = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = Spacing.Space4)
    ) {
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(Spacing.Space2))
}

/**
 * Bottom sheet with a two-step picker: select book → select chapter.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookChapterPickerSheet(
    books: List<Book>,
    currentBookId: Long?,
    onBookChapterSelected: (bookId: Long, chapter: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Space16)
        ) {
            if (selectedBook == null) {
                // Step 1: Book list
                Text(
                    text = "Select Book",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Spacing.Space12)
                )
                LazyColumn {
                    items(books, key = { it.id }) { book ->
                        val isCurrent = book.id == currentBookId
                        Surface(
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ) {
                            Text(
                                text = book.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBook = book }
                                    .padding(
                                        horizontal = Spacing.Space16,
                                        vertical = Spacing.Space12
                                    )
                            )
                        }
                        HorizontalDivider()
                    }
                }
            } else {
                // Step 2: Chapter grid
                val book = selectedBook!!
                Text(
                    text = "${book.name} — Select Chapter",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Spacing.Space12)
                )
                @Suppress("MagicNumber")
                LazyVerticalGrid(columns = GridCells.Fixed(5)) {
                    items((1..book.chapterCount.toInt()).toList()) { chapter ->
                        Surface(
                            modifier = Modifier
                                .padding(Spacing.Space4)
                                .clickable {
                                    onBookChapterSelected(book.id, chapter.toLong())
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = chapter.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(Spacing.Space12)
                            )
                        }
                    }
                }
            }
        }
    }
}
