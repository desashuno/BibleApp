package org.biblestudio.ui.panes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.navigation.OpenMode
import org.biblestudio.core.pane_registry.PaneType
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.features.bible_reader.component.BibleReaderState
import org.biblestudio.features.bible_reader.component.SelectedWord
import org.biblestudio.features.bible_reader.component.VerseSelectionRange
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.core.study.CrossReference
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.ui.theme.AnimationDurations
import org.biblestudio.ui.theme.AppColors
import org.biblestudio.ui.theme.LocalContinuousScroll
import org.biblestudio.ui.theme.LocalParagraphMode
import org.biblestudio.ui.theme.LocalRedLetter
import org.biblestudio.ui.theme.LocalShowVerseNumbers
import org.biblestudio.ui.theme.Spacing
import org.biblestudio.ui.theme.scaledBodyStyle
import org.biblestudio.ui.util.extractRedLetterRanges
import org.biblestudio.ui.workspace.LocalNavigateToPane

/**
 * Bible Reader pane: displays chapter text with verse numbers in a scrollable list.
 *
 * - Verse numbers are superscript in the accent color
 * - Tapping a verse triggers [onVerseTapped]
 * - Long-pressing a verse starts/extends selection via [onVerseLongPressed]
 * - Book/chapter picker bottom sheet when header is tapped
 * - Automatically scrolls to [BibleReaderState.scrollToVerseId] when set
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList", "CyclomaticComplexMethod")
@Composable
fun BibleReaderPane(
    stateFlow: StateFlow<BibleReaderState>,
    onVerseTapped: (Verse) -> Unit,
    onVerseLongPressed: (Verse) -> Unit,
    onBookChapterSelected: (bookId: Long, chapter: Long) -> Unit,
    onToggleBookPicker: () -> Unit,
    onCopySelection: (() -> String)? = null,
    onClearSelection: (() -> Unit)? = null,
    onHighlightSelection: ((Int) -> Unit)? = null,
    onBookmarkVerse: (() -> Unit)? = null,
    onWordTapped: ((Verse, String, Int) -> Unit)? = null,
    onSearchWord: ((String) -> Unit)? = null,
    onStudyWord: ((String) -> Unit)? = null,
    onDismissWordPopup: (() -> Unit)? = null,
    onToggleShowVerseNumbers: (() -> Unit)? = null,
    onToggleRedLetter: (() -> Unit)? = null,
    onToggleParagraphMode: (() -> Unit)? = null,
    onToggleContinuousScroll: (() -> Unit)? = null,
    onAdjustFontSize: ((Int) -> Unit)? = null,
    onCrossReferenceTapped: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val navigateToPane = LocalNavigateToPane.current

    // Context menu state
    var contextMenuVerse by remember { mutableStateOf<Verse?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    // Auto-scroll when scrollToVerseId changes
    LaunchedEffect(state.scrollToVerseId) {
        val targetId = state.scrollToVerseId ?: return@LaunchedEffect
        val index = state.verses.indexOfFirst { it.globalVerseId == targetId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    // Book/chapter picker dialog
    if (state.showBookPicker) {
        BookChapterPickerDialog(
            books = state.books,
            currentBookId = state.currentBook?.id,
            onBookChapterSelected = { bookId, chapter ->
                onBookChapterSelected(bookId, chapter)
                onToggleBookPicker()
            },
            onDismiss = onToggleBookPicker
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
        // Word action popup
        if (state.selectedWord != null) {
            WordActionPopup(
                selectedWord = state.selectedWord!!,
                onSearchWord = onSearchWord,
                onStudyWord = onStudyWord,
                onDismiss = onDismissWordPopup ?: {},
                clipboardManager = clipboardManager
            )
        }

        // Per-pane reader toolbar
        if (state.showReaderToolbar) {
            ReaderToolbar(
                state = state,
                onToggleShowVerseNumbers = onToggleShowVerseNumbers,
                onToggleRedLetter = onToggleRedLetter,
                onToggleParagraphMode = onToggleParagraphMode,
                onToggleContinuousScroll = onToggleContinuousScroll,
                onAdjustFontSize = onAdjustFontSize
            )
        }

        // Verse context menu (right-click)
        if (showContextMenu && contextMenuVerse != null) {
            VerseContextMenu(
                verse = contextMenuVerse!!,
                state = state,
                expanded = showContextMenu,
                onDismiss = { showContextMenu = false },
                clipboardManager = clipboardManager,
                onHighlightSelection = onHighlightSelection,
                onBookmarkVerse = onBookmarkVerse,
                onSearchWord = onSearchWord,
                onStudyWord = onStudyWord,
                navigateToPane = navigateToPane
            )
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
                    listState = listState
                )
            } else if (continuousScroll) {
                ContinuousScrollView(
                    verses = state.verses,
                    bookName = state.currentBook?.name ?: "",
                    selectedVerseRange = state.selectedVerseRange,
                    highlights = state.highlights,
                    crossReferences = state.crossReferences,
                    listState = listState,
                    onVerseTapped = onVerseTapped,
                    onVerseLongPressed = onVerseLongPressed,
                    onWordTapped = onWordTapped,
                    onRightClick = { verse ->
                        contextMenuVerse = verse
                        showContextMenu = true
                    },
                    onCrossRefTapped = onCrossReferenceTapped
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
                            onLongClick = { onVerseLongPressed(verse) },
                            onWordTapped = onWordTapped?.let { callback ->
                                { word, index -> callback(verse, word, index) }
                            },
                            onRightClick = {
                                contextMenuVerse = verse
                                showContextMenu = true
                            },
                            onCrossRefTapped = onCrossReferenceTapped
                        )
                    }
                }
            }
        }
    }
        AnimatedVisibility(
            visible = state.selectedVerseRange != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.Space16),
            enter = slideInVertically(tween(AnimationDurations.MEDIUM, easing = AnimationDurations.EaseDecelerate)) { it } +
                fadeIn(tween(AnimationDurations.MEDIUM)),
            exit = slideOutVertically(tween(AnimationDurations.MEDIUM, easing = AnimationDurations.EaseAccelerate)) { it } +
                fadeOut(tween(AnimationDurations.MEDIUM))
        ) {
            SelectionBottomBar(
                onCopySelection = onCopySelection,
                onClearSelection = onClearSelection,
                onHighlightSelection = onHighlightSelection,
                onBookmarkVerse = onBookmarkVerse,
                clipboardManager = clipboardManager
            )
        }
    }
}

/**
 * Floating selection bottom bar with Highlight, Bookmark, Share, Copy, and Clear icon buttons.
 */
@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun SelectionBottomBar(
    onCopySelection: (() -> String)?,
    onClearSelection: (() -> Unit)?,
    onHighlightSelection: ((Int) -> Unit)?,
    onBookmarkVerse: (() -> Unit)?,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.Space8, vertical = Spacing.Space4),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Space4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Highlight icon button with color picker dropdown
            if (onHighlightSelection != null) {
                Box {
                    IconButton(onClick = { showColorPicker = true }) {
                        Icon(
                            Icons.Default.FormatColorFill,
                            contentDescription = "Highlight",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showColorPicker,
                        onDismissRequest = { showColorPicker = false }
                    ) {
                        AppColors.highlights.forEachIndexed { index, color ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(color)
                                        )
                                        Spacer(Modifier.padding(start = Spacing.Space8))
                                        Text("Color ${index + 1}")
                                    }
                                },
                                onClick = {
                                    onHighlightSelection(index)
                                    showColorPicker = false
                                }
                            )
                        }
                    }
                }
            }

            // Bookmark icon button
            if (onBookmarkVerse != null) {
                IconButton(onClick = onBookmarkVerse) {
                    Icon(
                        Icons.Default.BookmarkAdd,
                        contentDescription = "Bookmark",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Share and Copy icon buttons
            if (onCopySelection != null) {
                IconButton(onClick = {
                    val text = onCopySelection()
                    clipboardManager.setText(AnnotatedString(text))
                }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = {
                    val text = onCopySelection()
                    clipboardManager.setText(AnnotatedString(text))
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(20.dp))
                }
            }

            // Clear icon button
            if (onClearSelection != null) {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear selection", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/**
 * Popup menu shown when a word is tapped, offering Search, Word Study, and Copy.
 */
@Suppress("ktlint:standard:function-naming", "LongParameterList")
@Composable
private fun WordActionPopup(
    selectedWord: SelectedWord,
    onSearchWord: ((String) -> Unit)?,
    onStudyWord: ((String) -> Unit)?,
    onDismiss: () -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Column {
            // Header: word + optional morphology
            Text(
                text = selectedWord.word,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            val morph = selectedWord.morphWord
            if (morph != null) {
                Text(
                    text = "${morph.lemma} (${morph.strongsNumber}) — ${morph.gloss}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.height(Spacing.Space4))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Space4)) {
                // Search
                if (onSearchWord != null) {
                    FilledTonalButton(onClick = {
                        onSearchWord(selectedWord.word)
                        onDismiss()
                    }) {
                        Text("Search")
                    }
                }
                // Word Study (only if Strong's available)
                if (onStudyWord != null && morph?.strongsNumber?.isNotBlank() == true) {
                    FilledTonalButton(onClick = {
                        onStudyWord(morph.strongsNumber)
                        onDismiss()
                    }) {
                        Text("Word Study")
                    }
                }
                // Copy word
                FilledTonalButton(onClick = {
                    clipboardManager.setText(AnnotatedString(selectedWord.word))
                    onDismiss()
                }) {
                    Text("Copy")
                }
                // Dismiss
                FilledTonalButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Paragraph mode: renders all verses as a single flowing text with inline verse numbers.
 */
@Suppress("ktlint:standard:function-naming", "MagicNumber", "LongMethod")
@Composable
private fun ParagraphView(
    verses: List<Verse>,
    highlights: Map<Long, List<Highlight>>,
    listState: androidx.compose.foundation.lazy.LazyListState
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
            val text = verse.text
            val redRanges = if (redLetter) extractRedLetterRanges(verse.htmlText, text) else emptyList()
            val verseHighlights = highlights[verse.globalVerseId].orEmpty()

            if (verseHighlights.isEmpty()) {
                appendTextWithRanges(
                    text = text,
                    redRanges = redRanges,
                    redStyle = SpanStyle(color = redColor)
                )
            } else {
                val wholeVerse = verseHighlights.firstOrNull { it.endOffset == -1L }
                if (wholeVerse != null) {
                    val colorIdx = wholeVerse.colorIndex.toInt()
                        .coerceIn(0, AppColors.highlights.lastIndex)
                    val color = AppColors.highlights[colorIdx]
                    appendTextWithRanges(
                        text = text,
                        redRanges = redRanges,
                        normalStyle = SpanStyle(background = color),
                        redStyle = SpanStyle(background = color, color = redColor)
                    )
                } else {
                    appendTextWithRanges(
                        text = text,
                        redRanges = redRanges,
                        redStyle = SpanStyle(color = redColor)
                    )
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
                color = MaterialTheme.colorScheme.onSurface
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
    bookName: String = "",
    selectedVerseRange: VerseSelectionRange?,
    highlights: Map<Long, List<Highlight>>,
    crossReferences: Map<Long, List<CrossReference>>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onVerseTapped: (Verse) -> Unit,
    onVerseLongPressed: (Verse) -> Unit,
    onWordTapped: ((Verse, String, Int) -> Unit)? = null,
    onRightClick: ((Verse) -> Unit)? = null,
    onCrossRefTapped: ((Long) -> Unit)? = null
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
                    text = if (bookName.isBlank()) "Chapter $chapter" else "$bookName $chapter",
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
                    onLongClick = { onVerseLongPressed(verse) },
                    onWordTapped = onWordTapped?.let { callback ->
                        { word, index -> callback(verse, word, index) }
                    },
                    onRightClick = onRightClick?.let { callback ->
                        { callback(verse) }
                    },
                    onCrossRefTapped = onCrossRefTapped
                )
            }
        }
    }
}

@Suppress(
    "ktlint:standard:function-naming",
    "MagicNumber",
    "LongMethod",
    "LongParameterList",
    "CyclomaticComplexMethod"
)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VerseRow(
    verse: Verse,
    isSelected: Boolean,
    highlights: List<Highlight>,
    crossRefs: List<CrossReference> = emptyList(),
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onWordTapped: ((String, Int) -> Unit)? = null,
    onRightClick: (() -> Unit)? = null,
    onCrossRefTapped: ((Long) -> Unit)? = null
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
        val redRanges = if (redLetter) extractRedLetterRanges(verse.htmlText, text) else emptyList()

        // Build word-annotated text for word-level tap support
        val words = text.split(" ")
        var wordIdx = 0
        var verseCharOffset = 0
        for ((i, word) in words.withIndex()) {
            if (i > 0) {
                append(" ")
                verseCharOffset += 1
            }
            wordIdx++
            val wordStart = this.length
            // Apply highlight/red-letter styling
            val hlStyle = resolveHighlightStyle(
                charOffset = verseCharOffset,
                wordLength = word.length,
                highlights = highlights,
                redRanges = redRanges,
                redColor = redColor
            )
            if (hlStyle != null) {
                withStyle(hlStyle) { append(word) }
            } else {
                append(word)
            }
            verseCharOffset += word.length
            // Annotate each word for click detection
            if (onWordTapped != null) {
                addStringAnnotation("WORD", "$wordIdx:$word", wordStart, this.length)
            }
        }

    }

    val textStyle = scaledBodyStyle().copy(color = MaterialTheme.colorScheme.onSurface)

    // Right-click modifier for context menu
    val rightClickModifier = if (onRightClick != null) {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Press &&
                        event.buttons.isSecondaryPressed
                    ) {
                        event.changes.forEach { it.consume() }
                        onRightClick()
                    }
                }
            }
        }
    } else {
        Modifier
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Selection indicator: 4dp left border
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                )
        )
        // Verse content with subtle tint when selected
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    } else {
                        Color.Transparent
                    }
                )
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .then(rightClickModifier)
                .padding(vertical = Spacing.Space4)
        ) {
            if (onWordTapped != null || onCrossRefTapped != null) {
                @Suppress("DEPRECATION")
                ClickableText(
                    text = annotatedText,
                    style = textStyle,
                    onClick = { offset ->
                        // Check XREF annotations first
                        annotatedText.getStringAnnotations("XREF", offset, offset)
                            .firstOrNull()?.let { ann ->
                                val targetId = ann.item.toLongOrNull() ?: return@let
                                onCrossRefTapped?.invoke(targetId)
                                return@ClickableText
                            }
                        // Then check WORD annotations
                        if (onWordTapped != null) {
                            annotatedText.getStringAnnotations("WORD", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    val parts = annotation.item.split(":", limit = 2)
                                    if (parts.size == 2) {
                                        val idx = parts[0].toIntOrNull() ?: return@let
                                        onWordTapped(parts[1], idx)
                                    }
                                } ?: onClick()
                        } else {
                            onClick()
                        }
                    }
                )
            } else {
                Text(
                    text = annotatedText,
                    style = textStyle
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(Spacing.Space2))
}

/**
 * Resolves the combined highlight + red-letter style for a word based on whole-verse highlights.
 */
@Suppress("MagicNumber")
@Composable
private fun resolveHighlightStyle(
    charOffset: Int,
    wordLength: Int,
    highlights: List<Highlight>,
    redRanges: List<IntRange>,
    redColor: androidx.compose.ui.graphics.Color
): SpanStyle? {
    val isWordsOfJesus = isRangeInRed(charOffset, wordLength, redRanges)
    val wholeVerse = highlights.firstOrNull { it.endOffset == -1L }
    return when {
        wholeVerse != null -> {
            val colorIdx = wholeVerse.colorIndex.toInt().coerceIn(0, AppColors.highlights.lastIndex)
            val color = AppColors.highlights[colorIdx]
            if (isWordsOfJesus) {
                SpanStyle(background = color, color = redColor)
            } else {
                SpanStyle(background = color)
            }
        }
        isWordsOfJesus -> SpanStyle(color = redColor)
        else -> null
    }
}

private fun isRangeInRed(charOffset: Int, wordLength: Int, redRanges: List<IntRange>): Boolean {
    if (wordLength <= 0) return false
    val end = charOffset + wordLength - 1
    return redRanges.any { range ->
        range.first <= end && range.last >= charOffset
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendTextWithRanges(
    text: String,
    redRanges: List<IntRange>,
    normalStyle: SpanStyle? = null,
    redStyle: SpanStyle
) {
    if (text.isEmpty()) return

    var idx = 0
    while (idx < text.length) {
        val inRed = redRanges.any { idx in it }
        var end = idx + 1
        while (end < text.length && redRanges.any { end in it } == inRed) {
            end++
        }

        val chunk = text.substring(idx, end)
        val style = when {
            inRed && normalStyle != null -> normalStyle.merge(redStyle)
            inRed -> redStyle
            else -> normalStyle
        }

        if (style != null) {
            withStyle(style) { append(chunk) }
        } else {
            append(chunk)
        }

        idx = end
    }
}

/**
 * Compact in-pane toolbar with display toggle buttons.
 */
@Suppress("ktlint:standard:function-naming", "LongParameterList", "MagicNumber", "LongMethod")
@Composable
private fun ReaderToolbar(
    state: BibleReaderState,
    onToggleShowVerseNumbers: (() -> Unit)?,
    onToggleRedLetter: (() -> Unit)?,
    onToggleParagraphMode: (() -> Unit)?,
    onToggleContinuousScroll: (() -> Unit)?,
    onAdjustFontSize: ((Int) -> Unit)?
) {
    val showVerseNumbers = LocalShowVerseNumbers.current
    val redLetter = LocalRedLetter.current
    val paragraphMode = LocalParagraphMode.current
    val continuousScroll = state.continuousScroll
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = Spacing.Space8, vertical = Spacing.Space2),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Verse numbers
        if (onToggleShowVerseNumbers != null) {
            IconButton(onClick = onToggleShowVerseNumbers, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.FormatListNumbered,
                    contentDescription = "Verse numbers",
                    tint = if (showVerseNumbers) activeColor else inactiveColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        // Red letter
        if (onToggleRedLetter != null) {
            IconButton(onClick = onToggleRedLetter, modifier = Modifier.size(32.dp)) {
                Text(
                    text = "Red",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (redLetter) MaterialTheme.colorScheme.error else inactiveColor
                )
            }
        }
        // Paragraph mode
        if (onToggleParagraphMode != null) {
            IconButton(onClick = onToggleParagraphMode, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.ViewHeadline,
                    contentDescription = "Paragraph mode",
                    tint = if (paragraphMode) activeColor else inactiveColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        // Continuous scroll
        if (onToggleContinuousScroll != null) {
            IconButton(onClick = onToggleContinuousScroll, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.UnfoldMore,
                    contentDescription = "Continuous scroll",
                    tint = if (continuousScroll) activeColor else inactiveColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        // Font size decrease / increase
        if (onAdjustFontSize != null) {
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onAdjustFontSize(-1) }, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.TextDecrease,
                    contentDescription = "Decrease font",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = { onAdjustFontSize(1) }, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.TextIncrease,
                    contentDescription = "Increase font",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Right-click context menu for a verse with copy, highlight, bookmark,
 * word-level actions, and "Send to" navigation options.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList", "MagicNumber")
@Composable
private fun VerseContextMenu(
    verse: Verse,
    state: BibleReaderState,
    expanded: Boolean,
    onDismiss: () -> Unit,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    onHighlightSelection: ((Int) -> Unit)?,
    onBookmarkVerse: (() -> Unit)?,
    onSearchWord: ((String) -> Unit)?,
    onStudyWord: ((String) -> Unit)?,
    navigateToPane: ((String, OpenMode, LinkEvent) -> Unit)?
) {
    var showHighlightSubmenu by remember { mutableStateOf(false) }
    val verseRef = "${state.currentBook?.name ?: ""} ${state.currentChapter}:${verse.verseNumber}" +
        " (${state.currentBible?.abbreviation ?: ""})"

    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        // Header
        DropdownMenuItem(
            text = {
                Text(
                    text = verseRef,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            enabled = false,
            onClick = {}
        )
        HorizontalDivider()

        // Copy Verse
        DropdownMenuItem(
            text = { Text("Copy Verse") },
            onClick = {
                clipboardManager.setText(AnnotatedString("$verseRef\n${verse.text}"))
                onDismiss()
            }
        )

        // Highlight submenu
        if (onHighlightSelection != null) {
            Box {
                DropdownMenuItem(
                    text = { Text("Highlight") },
                    onClick = { showHighlightSubmenu = true }
                )
                DropdownMenu(
                    expanded = showHighlightSubmenu,
                    onDismissRequest = { showHighlightSubmenu = false }
                ) {
                    AppColors.highlights.forEachIndexed { index, color ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(16.dp).background(color))
                                    Spacer(Modifier.size(Spacing.Space8))
                                    Text("Color ${index + 1}")
                                }
                            },
                            onClick = {
                                onHighlightSelection(index)
                                showHighlightSubmenu = false
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }

        // Bookmark
        if (onBookmarkVerse != null) {
            DropdownMenuItem(
                text = { Text("Bookmark") },
                onClick = {
                    onBookmarkVerse()
                    onDismiss()
                }
            )
        }

        // Word-specific items (if a word was previously selected)
        val selectedWord = state.selectedWord
        if (selectedWord != null) {
            HorizontalDivider()
            if (onSearchWord != null) {
                DropdownMenuItem(
                    text = { Text("Search \"${selectedWord.word}\"") },
                    onClick = {
                        onSearchWord(selectedWord.word)
                        onDismiss()
                    }
                )
            }
            val strongs = selectedWord.morphWord?.strongsNumber
            if (onStudyWord != null && strongs?.isNotBlank() == true) {
                DropdownMenuItem(
                    text = { Text("Study \"${selectedWord.word}\"") },
                    onClick = {
                        onStudyWord(strongs)
                        onDismiss()
                    }
                )
            }
        }

        // "Send to..." section
        if (navigateToPane != null) {
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Send to...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                enabled = false,
                onClick = {}
            )
            val verseEvent = LinkEvent.VerseSelected(verse.globalVerseId.toInt())
            val sendToTargets = listOf(
                PaneType.CROSS_REFERENCES to "Cross References",
                PaneType.WORD_STUDY to "Word Study",
                PaneType.PASSAGE_GUIDE to "Passage Guide",
                PaneType.TEXT_COMPARISON to "Text Comparison",
                PaneType.INTERLINEAR to "Interlinear",
                PaneType.MORPHOLOGY to "Morphology",
                PaneType.EXEGETICAL_GUIDE to "Exegetical Guide",
                PaneType.TIMELINE to "Timeline",
                PaneType.THEOLOGICAL_ATLAS to "Atlas",
                PaneType.KNOWLEDGE_GRAPH to "Knowledge Graph",
                PaneType.COMMENTARY to "Commentary"
            )
            for ((paneType, label) in sendToTargets) {
                DropdownMenuItem(
                    text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        navigateToPane(paneType, OpenMode.SMART, verseEvent)
                        onDismiss()
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Open in New Panel") },
                onClick = {
                    navigateToPane(PaneType.CROSS_REFERENCES, OpenMode.NEW_PANEL, verseEvent)
                    onDismiss()
                }
            )
            DropdownMenuItem(
                text = { Text("Open in New Workspace") },
                onClick = {
                    navigateToPane(PaneType.CROSS_REFERENCES, OpenMode.NEW_WORKSPACE, verseEvent)
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Dialog-based book/chapter picker with testament tabs and an accordion book list.
 *
 * Tabs: "Todo" (all), "Antiguo T." (books 1–39), "Nuevo T." (books 40–66).
 * Only one book is expanded at a time; tapping its row reveals chapter buttons inline.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "MagicNumber")
@Composable
private fun BookChapterPickerDialog(
    books: List<Book>,
    currentBookId: Long?,
    onBookChapterSelected: (bookId: Long, chapter: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var expandedBookId by remember { mutableStateOf<Long?>(currentBookId) }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("Todo", "Antiguo T.", "Nuevo T.")
    val filteredBooks = remember(books, selectedTabIndex, searchQuery) {
        val byTestament = when (selectedTabIndex) {
            1 -> books.filter { it.testament == "OT" }
            2 -> books.filter { it.testament == "NT" }
            else -> books
        }
        if (searchQuery.isBlank()) byTestament
        else byTestament.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxHeight(0.85f),
        ) {
            Column {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Space16, vertical = Spacing.Space12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Ir a\u2026",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Book search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar libro\u2026", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Space16, vertical = Spacing.Space4),
                )

                // Testament tabs
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                // Accordion book list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredBooks, key = { it.id }) { book ->
                        val isCurrentBook = book.id == currentBookId
                        val isExpanded = searchQuery.isNotBlank() || expandedBookId == book.id
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrentBook) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                    )
                                    .clickable {
                                        expandedBookId = if (isExpanded) null else book.id
                                    }
                                    .padding(
                                        horizontal = Spacing.Space16,
                                        vertical = Spacing.Space12,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = book.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            if (isExpanded) {
                                Column(modifier = Modifier.padding(horizontal = Spacing.Space16)) {
                                    for (chapter in 1..book.chapterCount.toInt()) {
                                        OutlinedButton(
                                            onClick = {
                                                onBookChapterSelected(book.id, chapter.toLong())
                                                onDismiss()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = Spacing.Space2),
                                        ) {
                                            Text(chapter.toString())
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
