package org.biblestudio.ui.panes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.bible_reader.component.BibleReaderState
import org.biblestudio.features.bible_reader.component.VerseSelectionRange
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphWord
import org.biblestudio.ui.theme.AppColors
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.ui.theme.LocalContinuousScroll
import org.biblestudio.ui.theme.LocalParagraphMode
import org.biblestudio.ui.theme.LocalRedLetter
import org.biblestudio.ui.theme.LocalShowVerseNumbers
import org.biblestudio.ui.theme.Spacing
import org.biblestudio.ui.theme.scaledBodyStyle

/**
 * Bible Reader pane: displays chapter text with verse numbers in a scrollable list.
 *
 * - Verse numbers are superscript in the accent color
 * - Tapping a verse triggers [onVerseTapped]
 * - Long-pressing a verse starts/extends selection via [onVerseLongPressed]
 * - Book/chapter picker bottom sheet when header is tapped
 * - Automatically scrolls to [BibleReaderState.scrollToVerseId] when set
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReaderPane(
    stateFlow: StateFlow<BibleReaderState>,
    onVerseTapped: (Verse) -> Unit,
    onVerseLongPressed: (Verse) -> Unit,
    onBookChapterSelected: (bookId: Long, chapter: Long) -> Unit,
    onToggleBookPicker: () -> Unit,
    onCopySelection: (() -> String)? = null,
    onClearSelection: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

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
        // Selection action bar
        AnimatedVisibility(visible = state.selectedVerseRange != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = Spacing.Space16, vertical = Spacing.Space4),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onCopySelection != null) {
                    FilledTonalButton(onClick = {
                        val text = onCopySelection()
                        clipboardManager.setText(AnnotatedString(text))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.padding(start = Spacing.Space4))
                        Text("Copy")
                    }
                }
                Spacer(Modifier.padding(start = Spacing.Space8))
                if (onClearSelection != null) {
                    FilledTonalButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(Modifier.padding(start = Spacing.Space4))
                        Text("Clear")
                    }
                }
            }
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
            val paragraphMode = LocalParagraphMode.current
            val continuousScroll = LocalContinuousScroll.current
            if (paragraphMode) {
                ParagraphView(
                    verses = state.verses,
                    highlights = state.highlights,
                    listState = listState,
                )
            } else if (continuousScroll) {
                ContinuousScrollView(
                    verses = state.verses,
                    selectedVerseRange = state.selectedVerseRange,
                    highlights = state.highlights,
                    crossReferences = state.crossReferences,
                    listState = listState,
                    onVerseTapped = onVerseTapped,
                    onVerseLongPressed = onVerseLongPressed,
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
                        val verseCrossRefs = state.crossReferences[verse.globalVerseId].orEmpty()
                        VerseRow(
                            verse = verse,
                            isSelected = isSelected,
                            highlights = verseHighlights,
                            crossRefs = verseCrossRefs,
                            onClick = { onVerseTapped(verse) },
                            onLongClick = { onVerseLongPressed(verse) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Paragraph mode: renders all verses as a single flowing text with inline verse numbers.
 */
@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun ParagraphView(
    verses: List<Verse>,
    highlights: Map<Long, List<Highlight>>,
    listState: androidx.compose.foundation.lazy.LazyListState,
) {
    val showVerseNumbers = LocalShowVerseNumbers.current
    val redLetter = LocalRedLetter.current
    val redColor = MaterialTheme.colorScheme.error

    val annotatedText = buildAnnotatedString {
        for (verse in verses) {
            if (showVerseNumbers) {
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
            }
            val isWordsOfJesus = redLetter && verse.htmlText?.contains("wj") == true
            val verseHighlights = highlights[verse.globalVerseId].orEmpty()
            val text = verse.text

            if (verseHighlights.isEmpty()) {
                if (isWordsOfJesus) {
                    withStyle(SpanStyle(color = redColor)) { append(text) }
                } else {
                    append(text)
                }
            } else {
                val wholeVerse = verseHighlights.firstOrNull { it.endOffset == -1L }
                if (wholeVerse != null) {
                    val colorIdx = wholeVerse.colorIndex.toInt()
                        .coerceIn(0, AppColors.highlights.lastIndex)
                    val color = AppColors.highlights[colorIdx]
                    val style = if (isWordsOfJesus) {
                        SpanStyle(background = color, color = redColor)
                    } else {
                        SpanStyle(background = color)
                    }
                    withStyle(style) { append(text) }
                } else {
                    append(text)
                }
            }
            append(" ")
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Space16)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.Space8))
            Text(
                text = annotatedText,
                style = scaledBodyStyle(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacing.Space8))
        }
    }
}

/**
 * Continuous scroll mode: all verses in a book with chapter divider headers.
 * Groups verses by chapter number (derived from globalVerseId) and inserts dividers.
 */
@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun ContinuousScrollView(
    verses: List<Verse>,
    selectedVerseRange: VerseSelectionRange?,
    highlights: Map<Long, List<Highlight>>,
    crossReferences: Map<Long, List<CrossReference>>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onVerseTapped: (Verse) -> Unit,
    onVerseLongPressed: (Verse) -> Unit,
) {
    // Group verses by chapter number derived from BBCCCVVV global verse ID
    val grouped = remember(verses) {
        verses.groupBy { VerseRefFormatter.chapter(it.globalVerseId) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Space16)
    ) {
        grouped.forEach { (chapter, chapterVerses) ->
            // Chapter divider
            item(key = "chapter-divider-$chapter") {
                Spacer(modifier = Modifier.height(Spacing.Space16))
                Text(
                    text = "Chapter $chapter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = Spacing.Space8)
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacing.Space4))
            }
            // Verses for this chapter
            items(chapterVerses, key = { it.id }) { verse ->
                val isSelected = selectedVerseRange?.let { range ->
                    verse.globalVerseId in range.startVerseId..range.endVerseId
                } ?: false
                val verseHighlights = highlights[verse.globalVerseId].orEmpty()
                val verseCrossRefs = crossReferences[verse.globalVerseId].orEmpty()
                VerseRow(
                    verse = verse,
                    isSelected = isSelected,
                    highlights = verseHighlights,
                    crossRefs = verseCrossRefs,
                    onClick = { onVerseTapped(verse) },
                    onLongClick = { onVerseLongPressed(verse) }
                )
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
    crossRefs: List<CrossReference> = emptyList(),
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val showVerseNumbers = LocalShowVerseNumbers.current
    val redLetter = LocalRedLetter.current
    val redColor = MaterialTheme.colorScheme.error

    val annotatedText = buildAnnotatedString {
        if (showVerseNumbers) {
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
        }
        val text = verse.text

        // Check for Words of Jesus (red-letter) via htmlText
        val isWordsOfJesus = redLetter && verse.htmlText?.contains("wj") == true

        if (highlights.isEmpty()) {
            if (isWordsOfJesus) {
                withStyle(SpanStyle(color = redColor)) { append(text) }
            } else {
                append(text)
            }
        } else {
            // Apply highlight spans; for whole-verse highlights paint entire text
            val wholeVerse = highlights.firstOrNull { it.endOffset == -1L }
            if (wholeVerse != null) {
                val colorIdx = wholeVerse.colorIndex.toInt()
                    .coerceIn(0, AppColors.highlights.lastIndex)
                val color = AppColors.highlights[colorIdx]
                val style = if (isWordsOfJesus) {
                    SpanStyle(background = color, color = redColor)
                } else {
                    SpanStyle(background = color)
                }
                withStyle(style) { append(text) }
            } else {
                // Sub-verse highlights: sort by startOffset and paint segments
                val sorted = highlights.sortedBy { it.startOffset }
                var cursor = 0
                for (hl in sorted) {
                    val start = hl.startOffset.toInt().coerceIn(0, text.length)
                    val end = hl.endOffset.toInt().coerceIn(start, text.length)
                    if (cursor < start) {
                        if (isWordsOfJesus) {
                            withStyle(SpanStyle(color = redColor)) {
                                append(text.substring(cursor, start))
                            }
                        } else {
                            append(text.substring(cursor, start))
                        }
                    }
                    val colorIdx = hl.colorIndex.toInt()
                        .coerceIn(0, AppColors.highlights.lastIndex)
                    val color = AppColors.highlights[colorIdx]
                    val style = if (isWordsOfJesus) {
                        SpanStyle(background = color, color = redColor)
                    } else {
                        SpanStyle(background = color)
                    }
                    withStyle(style) { append(text.substring(start, end)) }
                    cursor = end
                }
                if (cursor < text.length) {
                    if (isWordsOfJesus) {
                        withStyle(SpanStyle(color = redColor)) {
                            append(text.substring(cursor))
                        }
                    } else {
                        append(text.substring(cursor))
                    }
                }
            }
        }

        // Inline cross-reference markers
        if (crossRefs.isNotEmpty()) {
            append(" ")
            withStyle(
                SpanStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript
                )
            ) {
                val labels = ('a'..'z').take(crossRefs.size)
                append("[${labels.joinToString(",")}]")
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
            style = scaledBodyStyle(),
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
