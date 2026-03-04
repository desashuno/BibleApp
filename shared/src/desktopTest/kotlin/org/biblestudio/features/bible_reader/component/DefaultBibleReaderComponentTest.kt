package org.biblestudio.features.bible_reader.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.entities.Bible
import org.biblestudio.features.bible_reader.domain.entities.Book
import org.biblestudio.features.bible_reader.domain.entities.Chapter
import org.biblestudio.features.bible_reader.domain.entities.Verse
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository
import org.biblestudio.core.study.CrossReference
import org.biblestudio.core.study.MorphWord
import org.biblestudio.core.study.WordOccurrence
import org.biblestudio.features.bookmarks_history.domain.entities.Bookmark
import org.biblestudio.features.bookmarks_history.domain.entities.BookmarkFolder
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository
import org.biblestudio.features.morphology_interlinear.domain.entities.AlignmentEntry
import org.biblestudio.features.morphology_interlinear.domain.entities.MorphologyData
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository
import org.biblestudio.features.settings.domain.entities.AppSetting
import org.biblestudio.features.settings.domain.repositories.SettingsRepository
import org.biblestudio.test.testComponentContext

class DefaultBibleReaderComponentTest {

    private val fakeBibleRepo = object : BibleRepository {
        override suspend fun getAvailableBibles(): Result<List<Bible>> = Result.success(emptyList())

        override suspend fun getBooks(bibleId: Long): Result<List<Book>> = Result.success(emptyList())

        override suspend fun getChapters(bookId: Long): Result<List<Chapter>> = Result.success(emptyList())

        override suspend fun getVerses(bookId: Long, chapter: Long): Result<List<Verse>> = Result.success(emptyList())

        override suspend fun getVerseByGlobalId(globalVerseId: Long): Result<Verse?> = Result.success(null)

        override suspend fun getVersesInRange(startId: Long, endId: Long): Result<List<Verse>> =
            Result.success(emptyList())

        override suspend fun getVersesForBook(bookId: Long): Result<List<Verse>> = Result.success(emptyList())

        override suspend fun searchVerses(query: String, maxResults: Long): Result<List<Verse>> =
            Result.success(emptyList())

        override suspend fun getActiveBibles(): Result<List<Bible>> = Result.success(emptyList())

        override suspend fun getAvailableBiblesByLanguage(languageCode: String): Result<List<Bible>> =
            Result.success(emptyList())

        override suspend fun getActiveBiblesByLanguage(languageCode: String): Result<List<Bible>> =
            Result.success(emptyList())

        override suspend fun getNextVerseId(currentId: Long): Result<Long?> = Result.success(null)

        override suspend fun getPreviousVerseId(currentId: Long): Result<Long?> = Result.success(null)

        override fun watchBibles(): Flow<List<Bible>> = emptyFlow()
    }

    private val fakeHighlightRepo = object : HighlightRepository {
        override suspend fun getHighlightsForVerse(globalVerseId: Long): Result<List<Highlight>> =
            Result.success(emptyList())

        override suspend fun getAll(): Result<List<Highlight>> = Result.success(emptyList())

        override suspend fun getHighlightsForVerseRange(startVerseId: Long, endVerseId: Long): Result<List<Highlight>> =
            Result.success(emptyList())

        override suspend fun create(highlight: Highlight): Result<Unit> = Result.success(Unit)

        override suspend fun update(highlight: Highlight): Result<Unit> = Result.success(Unit)

        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = Result.success(Unit)

        override fun watchHighlightsForVerse(globalVerseId: Long): Flow<List<Highlight>> = emptyFlow()
    }

    private val fakeCrossRefRepo = object : CrossRefRepository {
        override suspend fun getRefsFromVerse(globalVerseId: Long): Result<List<CrossReference>> =
            Result.success(emptyList())
        override suspend fun getRefsToVerse(globalVerseId: Long): Result<List<CrossReference>> =
            Result.success(emptyList())
        override suspend fun getAllForVerse(globalVerseId: Long): Result<List<CrossReference>> =
            Result.success(emptyList())
        override suspend fun loadTskData(): Result<Int> = Result.success(0)
    }

    private val fakeMorphologyRepo = object : MorphologyRepository {
        override suspend fun getMorphologyForVerse(globalVerseId: Long): Result<List<MorphologyData>> =
            Result.success(emptyList())
        override suspend fun getMorphWords(globalVerseId: Long): Result<List<MorphWord>> =
            Result.success(emptyList())
        override suspend fun getWordsByStrongs(strongsNumber: String): Result<List<MorphWord>> =
            Result.success(emptyList())
        override suspend fun getOccurrences(strongsNumber: String, limit: Long, offset: Long): Result<List<WordOccurrence>> =
            Result.success(emptyList())
        override suspend fun getOccurrenceCount(strongsNumber: String): Result<Long> = Result.success(0L)
        override suspend fun getAlignmentForVerse(globalVerseId: Long): Result<List<AlignmentEntry>> =
            Result.success(emptyList())
    }

    private val fakeSettingsRepo = object : SettingsRepository {
        override suspend fun getSetting(key: String): Result<AppSetting?> = Result.success(null)
        override suspend fun getAll(): Result<List<AppSetting>> = Result.success(emptyList())
        override suspend fun getByCategory(category: String): Result<List<AppSetting>> = Result.success(emptyList())
        override suspend fun setSetting(setting: AppSetting): Result<Unit> = Result.success(Unit)
        override suspend fun delete(key: String): Result<Unit> = Result.success(Unit)
        override fun watchAll(): Flow<List<AppSetting>> = emptyFlow()
    }

    private val fakeBookmarkRepo = object : BookmarkRepository {
        override suspend fun getBookmarksForVerse(globalVerseId: Long): Result<List<Bookmark>> =
            Result.success(emptyList())
        override suspend fun getByFolder(folderId: String): Result<List<Bookmark>> = Result.success(emptyList())
        override suspend fun getAll(): Result<List<Bookmark>> = Result.success(emptyList())
        override suspend fun create(bookmark: Bookmark): Result<Unit> = Result.success(Unit)
        override suspend fun update(bookmark: Bookmark): Result<Unit> = Result.success(Unit)
        override suspend fun delete(uuid: String, deletedAt: String): Result<Unit> = Result.success(Unit)
        override fun watchAll(): Flow<List<Bookmark>> = emptyFlow()
        override suspend fun getAllFolders(): Result<List<BookmarkFolder>> = Result.success(emptyList())
        override suspend fun getFoldersByParent(parentId: String): Result<List<BookmarkFolder>> =
            Result.success(emptyList())
        override suspend fun getRootFolders(): Result<List<BookmarkFolder>> = Result.success(emptyList())
        override suspend fun createFolder(folder: BookmarkFolder): Result<Unit> = Result.success(Unit)
        override suspend fun updateFolder(folder: BookmarkFolder): Result<Unit> = Result.success(Unit)
        override suspend fun deleteFolder(uuid: String, deletedAt: String): Result<Unit> = Result.success(Unit)
    }

    private fun createComponent(
        verseBus: VerseBus = VerseBus(),
        settingsRepository: SettingsRepository = fakeSettingsRepo
    ): DefaultBibleReaderComponent {
        val context = testComponentContext()
        return DefaultBibleReaderComponent(
            componentContext = context,
            repository = fakeBibleRepo,
            verseBus = verseBus,
            highlightRepository = fakeHighlightRepo,
            crossRefRepository = fakeCrossRefRepo,
            morphologyRepository = fakeMorphologyRepo,
            settingsRepository = settingsRepository,
            bookmarkRepository = fakeBookmarkRepo
        )
    }

    @Test
    fun `toggleShowVerseNumbers flips from default true to false`() {
        val component = createComponent()
        // Initially null (uses global default)
        assertNull(component.state.value.showVerseNumbers)

        component.toggleShowVerseNumbers()
        // First toggle: null defaults to true, flips to false
        assertEquals(false, component.state.value.showVerseNumbers)

        component.toggleShowVerseNumbers()
        assertEquals(true, component.state.value.showVerseNumbers)
    }

    @Test
    fun `toggleRedLetter flips from default false to true`() {
        val component = createComponent()
        assertNull(component.state.value.redLetter)

        component.toggleRedLetter()
        // null defaults to false, flips to true
        assertEquals(true, component.state.value.redLetter)

        component.toggleRedLetter()
        assertEquals(false, component.state.value.redLetter)
    }

    @Test
    fun `toggleRedLetter resolves global value when per-pane is null`() {
        val settingsRepo = object : SettingsRepository {
            override suspend fun getSetting(key: String): Result<AppSetting?> = Result.success(
                AppSetting(
                    key = "red_letter",
                    value = "true",
                    type = "boolean",
                    category = "reading"
                )
            )

            override suspend fun getAll(): Result<List<AppSetting>> = Result.success(
                listOf(
                    AppSetting(
                        key = "red_letter",
                        value = "true",
                        type = "boolean",
                        category = "reading"
                    )
                )
            )

            override suspend fun getByCategory(category: String): Result<List<AppSetting>> = Result.success(emptyList())

            override suspend fun setSetting(setting: AppSetting): Result<Unit> = Result.success(Unit)

            override suspend fun delete(key: String): Result<Unit> = Result.success(Unit)

            override fun watchAll(): Flow<List<AppSetting>> = flowOf(
                listOf(
                    AppSetting(
                        key = "red_letter",
                        value = "true",
                        type = "boolean",
                        category = "reading"
                    )
                )
            )
        }

        val component = createComponent(settingsRepository = settingsRepo)
        assertNull(component.state.value.redLetter)

        Thread.sleep(50)

        component.toggleRedLetter()

        assertEquals(false, component.state.value.redLetter)
    }

    @Test
    fun `toggleParagraphMode flips from default false to true`() {
        val component = createComponent()
        assertNull(component.state.value.paragraphMode)

        component.toggleParagraphMode()
        assertEquals(true, component.state.value.paragraphMode)

        component.toggleParagraphMode()
        assertEquals(false, component.state.value.paragraphMode)
    }

    @Test
    fun `adjustFontSize increases and decreases within bounds`() {
        val component = createComponent()
        assertNull(component.state.value.fontSize)

        // First adjust uses default of 16
        component.adjustFontSize(1)
        assertEquals(17, component.state.value.fontSize)

        component.adjustFontSize(-2)
        assertEquals(15, component.state.value.fontSize)
    }

    @Test
    fun `adjustFontSize clamps at minimum 12`() {
        val component = createComponent()

        // Go down from default 16 by a large amount
        component.adjustFontSize(-20)
        assertEquals(12, component.state.value.fontSize)
    }

    @Test
    fun `adjustFontSize clamps at maximum 28`() {
        val component = createComponent()

        component.adjustFontSize(50)
        assertEquals(28, component.state.value.fontSize)
    }

    @Test
    fun `toggleReaderToolbar flips visibility`() {
        val component = createComponent()
        // Default is true
        assertTrue(component.state.value.showReaderToolbar)

        component.toggleReaderToolbar()
        assertFalse(component.state.value.showReaderToolbar)

        component.toggleReaderToolbar()
        assertTrue(component.state.value.showReaderToolbar)
    }

    @Test
    fun `onCrossReferenceTapped publishes VerseSelected event`() {
        val verseBus = VerseBus()
        val component = createComponent(verseBus = verseBus)

        val targetVerseId = 1_002_005L
        component.onCrossReferenceTapped(targetVerseId)

        val event = verseBus.current
        assertTrue(event is LinkEvent.VerseSelected)
        assertEquals(targetVerseId.toInt(), (event as LinkEvent.VerseSelected).globalVerseId)
    }

    @Test
    fun `setContinuousScroll updates state`() {
        val component = createComponent()
        assertFalse(component.state.value.continuousScroll)

        component.setContinuousScroll(true)
        assertTrue(component.state.value.continuousScroll)

        component.setContinuousScroll(false)
        assertFalse(component.state.value.continuousScroll)
    }
}
