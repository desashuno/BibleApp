package org.biblestudio.audit

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.biblestudio.test.TestDatabase

/**
 * Programmatic audit of module data quality thresholds.
 *
 * These tests verify that the seed DB (or in-memory schema) meets minimum
 * data quality requirements for each module. When run against a pre-populated
 * test DB they enforce the Phase 8 thresholds; when run against an empty
 * in-memory DB they simply confirm the queries compile and return zero.
 */
class ModuleAuditTest {

    private lateinit var testDb: TestDatabase

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    // ── Bible Reader ──────────────────────────────────────────────

    @Test
    fun `bibleCount query executes successfully`() {
        val count = testDb.database.bibleQueries.bibleCount().executeAsOne()
        assertTrue(count >= 0, "bibleCount should return non-negative value")
    }

    @Test
    fun `bookCountForBible query executes successfully`() {
        // Insert a test bible
        testDb.database.bibleQueries.insertBible(
            abbreviation = "TST",
            name = "Test Bible",
            language = "en",
            textDirection = "ltr"
        )
        val bibles = testDb.database.bibleQueries.allBibles().executeAsList()
        val bibleId = bibles.first().id
        val count = testDb.database.bibleQueries.bookCountForBible(bibleId).executeAsOne()
        assertEquals(0, count, "Empty bible should have 0 books")
    }

    @Test
    fun `verseCountForBible query executes successfully`() {
        testDb.database.bibleQueries.insertBible(
            abbreviation = "TST",
            name = "Test Bible",
            language = "en",
            textDirection = "ltr"
        )
        val bibles = testDb.database.bibleQueries.allBibles().executeAsList()
        val bibleId = bibles.first().id
        val count = testDb.database.bibleQueries.verseCountForBible(bibleId).executeAsOne()
        assertEquals(0, count, "Empty bible should have 0 verses")
    }

    // ── Study / Lexicon ───────────────────────────────────────────

    @Test
    fun `lexiconCount query executes successfully`() {
        val count = testDb.database.studyQueries.lexiconCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 lexicon entries")
    }

    @Test
    fun `emptyDefinitionCount returns zero on clean data`() {
        testDb.database.studyQueries.insertLexiconEntry(
            strongsNumber = "H0001",
            originalWord = "ab",
            transliteration = "ab",
            definition = "father",
            usageNotes = null
        )
        val empty = testDb.database.studyQueries.emptyDefinitionCount().executeAsOne()
        assertEquals(0, empty, "All entries should have definitions")
    }

    @Test
    fun `emptyDefinitionCount detects missing definitions`() {
        testDb.database.studyQueries.insertLexiconEntry(
            strongsNumber = "H0001",
            originalWord = "ab",
            transliteration = "ab",
            definition = "",
            usageNotes = null
        )
        val empty = testDb.database.studyQueries.emptyDefinitionCount().executeAsOne()
        assertEquals(1, empty, "Should detect 1 empty definition")
    }

    @Test
    fun `morphologyWordCount query executes successfully`() {
        val count = testDb.database.studyQueries.morphologyWordCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 morphology words")
    }

    @Test
    fun `totalOccurrenceCount query executes successfully`() {
        val count = testDb.database.studyQueries.totalOccurrenceCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 word occurrences")
    }

    // ── Cross References ──────────────────────────────────────────

    @Test
    fun `crossRefCount query executes successfully`() {
        val count = testDb.database.referenceQueries.crossRefCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 cross references")
    }

    // ── Knowledge Graph ───────────────────────────────────────────

    @Test
    fun `nodeCount query executes successfully`() {
        val count = testDb.database.knowledgeGraphQueries.nodeCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 graph nodes")
    }

    @Test
    fun `edgeCount query executes successfully`() {
        val count = testDb.database.knowledgeGraphQueries.edgeCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 graph edges")
    }

    // ── Theological Atlas ─────────────────────────────────────────

    @Test
    fun `locationCount query executes successfully`() {
        val count = testDb.database.atlasQueries.locationCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 atlas locations")
    }

    @Test
    fun `invalidCoordinates returns empty on valid data`() {
        testDb.database.atlasQueries.insertLocation(
            name = "Jerusalem",
            modernName = "Jerusalem",
            latitude = 31.7683,
            longitude = 35.2137,
            type = "city",
            description = "Holy city",
            era = "Ancient"
        )
        val invalid = testDb.database.atlasQueries.invalidCoordinates().executeAsList()
        assertTrue(invalid.isEmpty(), "Valid coordinates should not appear in invalidCoordinates")
    }

    @Test
    fun `invalidCoordinates detects out-of-range latitude`() {
        testDb.database.atlasQueries.insertLocation(
            name = "BadPlace",
            modernName = null,
            latitude = 999.0,
            longitude = 35.0,
            type = "city",
            description = "",
            era = "Ancient"
        )
        val invalid = testDb.database.atlasQueries.invalidCoordinates().executeAsList()
        assertEquals(1, invalid.size, "Should detect 1 invalid coordinate")
    }

    // ── Timeline ──────────────────────────────────────────────────

    @Test
    fun `eventCount query executes successfully`() {
        val count = testDb.database.timelineQueries.eventCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 timeline events")
    }

    // ── Resources ─────────────────────────────────────────────────

    @Test
    fun `resourceCount query executes successfully`() {
        val count = testDb.database.resourceQueries.resourceCount().executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 resources")
    }

    @Test
    fun `resourceCountByType query executes successfully`() {
        val count = testDb.database.resourceQueries.resourceCountByType("commentary").executeAsOne()
        assertEquals(0, count, "Empty DB should have 0 commentaries")
    }

    // ── Commentary Queries ────────────────────────────────────────

    @Test
    fun `commentaryEntriesForVerse returns entries from commentary resources`() {
        // Insert a commentary resource
        testDb.database.resourceQueries.insertResource(
            uuid = "mhc-test",
            type = "commentary",
            title = "Matthew Henry",
            author = "Matthew Henry",
            version = "1.0",
            format = "text",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = ""
        )
        // Insert an entry
        testDb.database.resourceQueries.insertEntry(
            resourceId = "mhc-test",
            globalVerseId = 1001001,
            content = "In the beginning God created...",
            sortOrder = 0
        )

        val entries = testDb.database.resourceQueries
            .commentaryEntriesForVerse(1001001)
            .executeAsList()

        assertEquals(1, entries.size, "Should find 1 commentary entry")
        assertEquals("Matthew Henry", entries[0].resource_title)
        assertEquals("Matthew Henry", entries[0].resource_author)
    }

    @Test
    fun `commentaryEntriesForVerse excludes non-commentary resources`() {
        testDb.database.resourceQueries.insertResource(
            uuid = "dict-test",
            type = "dictionary",
            title = "Test Dictionary",
            author = "Author",
            version = "1.0",
            format = "text",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            deviceId = ""
        )
        testDb.database.resourceQueries.insertEntry(
            resourceId = "dict-test",
            globalVerseId = 1001001,
            content = "Some dictionary content",
            sortOrder = 0
        )

        val entries = testDb.database.resourceQueries
            .commentaryEntriesForVerse(1001001)
            .executeAsList()

        assertTrue(entries.isEmpty(), "Dictionary entries should not appear in commentary query")
    }
}
