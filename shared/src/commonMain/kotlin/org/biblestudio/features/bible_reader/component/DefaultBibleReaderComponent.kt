package org.biblestudio.features.bible_reader.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository

/**
 * Default [BibleReaderComponent] backed by Decompose lifecycle, [BibleRepository]
 * for data access, and [VerseBus] for cross-pane communication.
 */
class DefaultBibleReaderComponent(
    componentContext: ComponentContext,
    private val repository: BibleRepository,
    private val verseBus: VerseBus,
    private val highlightRepository: HighlightRepository
) : BibleReaderComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(BibleReaderState())
    override val state: StateFlow<BibleReaderState> = _state.asStateFlow()

    init {
        loadBibles()
        observeVerseBus()
    }

    override fun selectBible(bibleId: Long) {
        scope.launch {
            repository.getAvailableBibles()
                .onSuccess { bibles ->
                    val bible = bibles.firstOrNull { it.id == bibleId } ?: return@onSuccess
                    _state.update { it.copy(currentBible = bible) }
                    loadBooksForBible(bibleId)
                }
        }
    }

    override fun goToChapter(bookId: Long, chapter: Long) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.getChapters(bookId)
                .onSuccess { chapters ->
                    _state.update { it.copy(chapterCount = chapters.size.toLong()) }
                }

            repository.getVerses(bookId, chapter)
                .onSuccess { verses ->
                    val book = _state.value.books.firstOrNull { it.id == bookId }
                    _state.update {
                        it.copy(
                            currentBook = book,
                            currentChapter = chapter,
                            verses = verses,
                            scrollToVerseId = null,
                            isLoading = false,
                            error = null
                        )
                    }
                    loadHighlightsForVerses(verses)
                    Napier.d("Loaded ${verses.size} verses for book=$bookId ch=$chapter")
                }
                .onFailure { e ->
                    Napier.e("Failed to load verses", e)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Could not load chapter."
                        )
                    }
                }
        }
    }

    override fun onVerseTapped(verse: Verse) {
        verseBus.publish(LinkEvent.VerseSelected(verse.globalVerseId.toInt()))
    }

    override fun nextChapter() {
        val s = _state.value
        val book = s.currentBook ?: return
        if (s.currentChapter < s.chapterCount) {
            goToChapter(book.id, s.currentChapter + 1)
        }
    }

    override fun previousChapter() {
        val s = _state.value
        val book = s.currentBook ?: return
        if (s.currentChapter > 1) {
            goToChapter(book.id, s.currentChapter - 1)
        }
    }

    override fun toggleBookPicker() {
        _state.update { it.copy(showBookPicker = !it.showBookPicker) }
    }

    override fun onVerseLongPressed(verse: Verse) {
        val current = _state.value.selectedVerseRange
        val newRange = if (current == null) {
            VerseSelectionRange(verse.globalVerseId, verse.globalVerseId)
        } else {
            val minId = minOf(current.startVerseId, verse.globalVerseId)
            val maxId = maxOf(current.endVerseId, verse.globalVerseId)
            VerseSelectionRange(minId, maxId)
        }
        _state.update { it.copy(selectedVerseRange = newRange) }
    }

    override fun clearSelection() {
        _state.update { it.copy(selectedVerseRange = null) }
    }

    private fun loadBibles() {
        scope.launch {
            repository.getAvailableBibles()
                .onSuccess { bibles ->
                    _state.update { it.copy(bibles = bibles) }
                    bibles.firstOrNull()?.let { bible ->
                        _state.update { it.copy(currentBible = bible) }
                        loadBooksForBible(bible.id)
                    }
                }
        }
    }

    private fun loadBooksForBible(bibleId: Long) {
        scope.launch {
            repository.getBooks(bibleId)
                .onSuccess { books ->
                    _state.update { it.copy(books = books) }
                    books.firstOrNull()?.let { book ->
                        goToChapter(book.id, 1)
                    }
                }
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events.collect { event ->
                when (event) {
                    is LinkEvent.VerseSelected -> scrollToVerse(event.globalVerseId.toLong())
                    is LinkEvent.PassageSelected -> loadPassageRange(
                        event.startVerseId.toLong(),
                        event.endVerseId.toLong()
                    )
                    else -> { /* ignore other events */ }
                }
            }
        }
    }

    private fun scrollToVerse(globalVerseId: Long) {
        _state.update { it.copy(scrollToVerseId = globalVerseId) }
    }

    private fun loadPassageRange(startId: Long, endId: Long) {
        scope.launch {
            repository.getVersesInRange(startId, endId)
                .onSuccess { verses ->
                    _state.update {
                        it.copy(
                            verses = verses,
                            scrollToVerseId = startId,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun loadHighlightsForVerses(verses: List<Verse>) {
        if (verses.isEmpty()) return
        val startId = verses.first().globalVerseId
        val endId = verses.last().globalVerseId
        scope.launch {
            highlightRepository.getHighlightsForVerseRange(startId, endId)
                .onSuccess { highlights ->
                    val grouped = highlights.groupBy { it.globalVerseId }
                    _state.update { it.copy(highlights = grouped) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load highlights", e)
                }
        }
    }
}
