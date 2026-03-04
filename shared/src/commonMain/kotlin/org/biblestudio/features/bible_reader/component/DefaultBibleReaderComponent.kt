package org.biblestudio.features.bible_reader.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.AppConstants
import org.biblestudio.core.util.generateUuid
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository
import org.biblestudio.core.util.nowIso
import org.biblestudio.features.settings.domain.entities.AppSetting
import org.biblestudio.features.settings.domain.repositories.SettingsRepository

/**
 * Default [BibleReaderComponent] backed by Decompose lifecycle, [BibleRepository]
 * for data access, and [VerseBus] for cross-pane communication.
 */
@Suppress("TooManyFunctions")
internal class DefaultBibleReaderComponent(
    componentContext: ComponentContext,
    private val repository: BibleRepository,
    private val verseBus: VerseBus,
    private val highlightRepository: HighlightRepository,
    private val crossRefRepository: CrossRefRepository,
    private val morphologyRepository: MorphologyRepository,
    private val settingsRepository: SettingsRepository,
    private val bookmarkRepository: BookmarkRepository
) : BibleReaderComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(BibleReaderState())
    override val state: StateFlow<BibleReaderState> = _state.asStateFlow()
    private val globalRedLetter = MutableStateFlow(false)

    init {
        loadBibles()
        observeVerseBus()
        restoreLastReadPosition()
        observeGlobalRedLetterSetting()
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
                            currentVerseIndex = 0,
                            isLoading = false,
                            error = null
                        )
                    }
                    loadHighlightsForVerses(verses)
                    loadCrossReferencesForVerses(verses)
                    loadMorphologyForVerses(verses)
                    saveLastReadPosition()
                    Napier.d("Loaded ${verses.size} verses for book=$bookId ch=$chapter")
                }
                .onFailure { e ->
                    Napier.e("Failed to load verses", e)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Could not load chapter (book=$bookId, ch=$chapter): ${e.message}"
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

    override fun nextVerse() {
        val s = _state.value
        if (s.currentVerseIndex < s.verses.size - 1) {
            val newIndex = s.currentVerseIndex + 1
            val verseId = s.verses[newIndex].globalVerseId
            _state.update { it.copy(currentVerseIndex = newIndex, scrollToVerseId = verseId) }
        }
    }

    override fun previousVerse() {
        val s = _state.value
        if (s.currentVerseIndex > 0) {
            val newIndex = s.currentVerseIndex - 1
            val verseId = s.verses[newIndex].globalVerseId
            _state.update { it.copy(currentVerseIndex = newIndex, scrollToVerseId = verseId) }
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

    override fun setContinuousScroll(enabled: Boolean) {
        _state.update { it.copy(continuousScroll = enabled) }
        if (enabled) {
            val bookId = _state.value.currentBook?.id ?: return
            loadBookForContinuousScroll(bookId)
        } else {
            val bookId = _state.value.currentBook?.id ?: return
            goToChapter(bookId, _state.value.currentChapter)
        }
    }

    override fun highlightSelection(colorIndex: Int) {
        val range = _state.value.selectedVerseRange ?: return
        val selectedVerses = _state.value.verses.filter {
            it.globalVerseId in range.startVerseId..range.endVerseId
        }
        scope.launch {
            val now = nowIso()
            for (verse in selectedVerses) {
                val highlight = Highlight(
                    uuid = generateUuid(),
                    globalVerseId = verse.globalVerseId,
                    colorIndex = colorIndex.toLong(),
                    style = AppConstants.HIGHLIGHT_STYLE_BACKGROUND,
                    startOffset = 0,
                    endOffset = -1,
                    createdAt = now,
                    updatedAt = now,
                    deviceId = AppConstants.DEVICE_ID_LOCAL
                )
                highlightRepository.create(highlight)
            }
            loadHighlightsForVerses(_state.value.verses)
            _state.update { it.copy(selectedVerseRange = null) }
        }
    }

    override fun bookmarkVerse() {
        val range = _state.value.selectedVerseRange ?: return
        val verse = _state.value.verses.firstOrNull {
            it.globalVerseId == range.startVerseId
        } ?: return
        val ref = "${_state.value.currentBook?.name ?: ""} ${_state.value.currentChapter}:${verse.verseNumber}"
        scope.launch {
            val now = nowIso()
            bookmarkRepository.create(
                Bookmark(
                    uuid = generateUuid(),
                    globalVerseId = verse.globalVerseId,
                    label = ref,
                    folderId = null,
                    sortOrder = 0,
                    createdAt = now,
                    updatedAt = now,
                    deviceId = AppConstants.DEVICE_ID_LOCAL
                )
            )
            _state.update { it.copy(selectedVerseRange = null) }
        }
    }

    override fun searchWord(word: String) {
        verseBus.publish(LinkEvent.SearchResult(0, word))
    }

    override fun studyWord(strongsNumber: String) {
        verseBus.publish(LinkEvent.StrongsSelected(strongsNumber))
    }

    override fun onWordTapped(verse: Verse, word: String, wordIndex: Int) {
        val morphWords = _state.value.morphology[verse.globalVerseId].orEmpty()
        val matchedMorph = morphWords.firstOrNull {
            it.wordPosition.toInt() == wordIndex
        } ?: morphWords.firstOrNull {
            it.surfaceForm.equals(word, ignoreCase = true)
        }
        _state.update {
            it.copy(
                selectedWord = SelectedWord(
                    word = word,
                    globalVerseId = verse.globalVerseId,
                    wordIndex = wordIndex,
                    morphWord = matchedMorph
                )
            )
        }
    }

    override fun dismissWordPopup() {
        _state.update { it.copy(selectedWord = null) }
    }

    override fun toggleShowVerseNumbers() {
        _state.update { it.copy(showVerseNumbers = !(it.showVerseNumbers ?: true)) }
    }

    override fun toggleRedLetter() {
        val effective = _state.value.redLetter ?: globalRedLetter.value
        _state.update { it.copy(redLetter = !effective) }
    }

    override fun toggleParagraphMode() {
        _state.update { it.copy(paragraphMode = !(it.paragraphMode ?: false)) }
    }

    @Suppress("MagicNumber")
    override fun adjustFontSize(delta: Int) {
        _state.update {
            val current = it.fontSize ?: AppConstants.FONT_SIZE_DEFAULT
            it.copy(fontSize = (current + delta).coerceIn(AppConstants.FONT_SIZE_MIN, AppConstants.FONT_SIZE_MAX))
        }
    }

    override fun toggleReaderToolbar() {
        _state.update { it.copy(showReaderToolbar = !it.showReaderToolbar) }
    }

    override fun onCrossReferenceTapped(targetVerseId: Long) {
        verseBus.publish(LinkEvent.VerseSelected(targetVerseId.toInt()))
    }

    override fun getSelectedVerseText(): String {
        val s = _state.value
        val range = s.selectedVerseRange ?: return ""
        val selectedVerses = s.verses.filter {
            it.globalVerseId in range.startVerseId..range.endVerseId
        }
        if (selectedVerses.isEmpty()) return ""

        val book = s.currentBook?.name ?: ""
        val chapter = s.currentChapter
        val abbr = s.currentBible?.abbreviation ?: ""
        val startVerse = selectedVerses.first().verseNumber
        val endVerse = selectedVerses.last().verseNumber
        val ref = if (startVerse == endVerse) {
            "$book $chapter:$startVerse ($abbr)"
        } else {
            "$book $chapter:$startVerse-$endVerse ($abbr)"
        }
        val text = selectedVerses.joinToString(" ") { it.text }
        return "$ref\n$text"
    }

    private fun loadBibles() {
        scope.launch {
            repository.getActiveBibles()
                .recoverCatching { repository.getAvailableBibles().getOrThrow() }
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
        val index = _state.value.verses.indexOfFirst { it.globalVerseId == globalVerseId }
        _state.update {
            it.copy(
                scrollToVerseId = globalVerseId,
                currentVerseIndex = if (index >= 0) index else it.currentVerseIndex,
            )
        }
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

    private fun loadCrossReferencesForVerses(verses: List<Verse>) {
        if (verses.isEmpty()) return
        scope.launch {
            val xrefMap = mutableMapOf<
                Long,
                List<org.biblestudio.core.study.CrossReference>
                >()
            for (verse in verses) {
                crossRefRepository.getRefsFromVerse(verse.globalVerseId)
                    .onSuccess { refs ->
                        if (refs.isNotEmpty()) {
                            xrefMap[verse.globalVerseId] = refs
                        }
                    }
            }
            _state.update { it.copy(crossReferences = xrefMap) }
        }
    }

    private fun loadBookForContinuousScroll(bookId: Long) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.getVersesForBook(bookId)
                .onSuccess { verses ->
                    _state.update {
                        it.copy(
                            verses = verses,
                            isLoading = false,
                            error = null
                        )
                    }
                    loadHighlightsForVerses(verses)
                    loadCrossReferencesForVerses(verses)
                    loadMorphologyForVerses(verses)
                }
                .onFailure { e ->
                    Napier.e("Failed to load book for continuous scroll", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load book: ${e.message}")
                    }
                }
        }
    }

    private fun saveLastReadPosition() {
        val firstVerse = _state.value.verses.firstOrNull() ?: return
        scope.launch {
            settingsRepository.setSetting(
                AppSetting(
                    key = KEY_LAST_READ_VERSE,
                    value = firstVerse.globalVerseId.toString(),
                    type = AppConstants.SettingType.LONG,
                    category = AppConstants.SettingCategory.READING
                )
            )
        }
    }

    private fun restoreLastReadPosition() {
        scope.launch {
            settingsRepository.getAll()
                .onSuccess { settings ->
                    val lastVerse = settings.firstOrNull { it.key == KEY_LAST_READ_VERSE }
                    val globalId = lastVerse?.value?.toLongOrNull()
                    if (globalId != null) {
                        scrollToVerse(globalId)
                    }
                }
        }
    }

    private fun loadMorphologyForVerses(verses: List<Verse>) {
        if (verses.isEmpty()) return
        scope.launch {
            val morphMap = mutableMapOf<
                Long,
                List<org.biblestudio.core.study.MorphWord>
                >()
            for (verse in verses) {
                morphologyRepository.getMorphWords(verse.globalVerseId)
                    .onSuccess { words ->
                        if (words.isNotEmpty()) {
                            morphMap[verse.globalVerseId] = words
                        }
                    }
            }
            _state.update { it.copy(morphology = morphMap) }
        }
    }

    private fun observeGlobalRedLetterSetting() {
        scope.launch {
            settingsRepository.watchAll().collect { settings ->
                globalRedLetter.value = settings
                    .firstOrNull { it.key == KEY_RED_LETTER }
                    ?.value
                    ?.toBooleanStrictOrNull()
                    ?: false
            }
        }
    }


    companion object {
        private const val KEY_LAST_READ_VERSE = "last_read_verse_id"
        private const val KEY_RED_LETTER = "red_letter"
    }
}
