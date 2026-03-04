package org.biblestudio.audit

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.biblestudio.test.TestDatabase

/**
 * Cross-module validation tests that verify referential integrity between
 * modules sharing data. These tests ensure that verse links, entity references,
 * and index tables point to valid records.
 *
 * Against an empty in-memory DB these pass trivially (zero orphans).
 * Against a populated seed DB they catch real integrity violations.
 */
class CrossModuleValidationTest {

    private lateinit var testDb: TestDatabase

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    // ── Cross-References ↔ Verses ─────────────────────────────────

    @Test
    fun `no orphaned cross-reference source verse IDs`() {
        val orphans = testDb.database.referenceQueries
            .orphanedCrossRefSources(maxResults = 10)
            .executeAsList()
        assertTrue(
            orphans.isEmpty(),
            "Found ${orphans.size} cross-ref source_verse_ids not in verses table: $orphans"
        )
    }

    @Test
    fun `no orphaned cross-reference target verse IDs`() {
        val orphans = testDb.database.referenceQueries
            .orphanedCrossRefTargets(maxResults = 10)
            .executeAsList()
        assertTrue(
            orphans.isEmpty(),
            "Found ${orphans.size} cross-ref target_verse_ids not in verses table: $orphans"
        )
    }

    // ── Morphology ↔ Lexicon ──────────────────────────────────────

    @Test
    fun `no orphaned morphology Strongs numbers without lexicon entries`() {
        val orphans = testDb.database.studyQueries
            .orphanedMorphologyStrongs(maxResults = 10)
            .executeAsList()
        assertTrue(
            orphans.isEmpty(),
            "Found ${orphans.size} morphology strongs_numbers without lexicon entries: $orphans"
        )
    }

    // ── Knowledge Graph Node-Verse Links ──────────────────────────

    @Test
    fun `no orphaned graph node-verse links`() {
        val orphans = testDb.database.knowledgeGraphQueries
            .orphanedNodeVerseLinks(maxResults = 10)
            .executeAsList()
        assertTrue(
            orphans.isEmpty(),
            "Found ${orphans.size} graph_node_verses with invalid node_id"
        )
    }

    @Test
    fun `graph node-verse link detects orphans when node is deleted`() {
        // Insert a node and link, then delete the node.
        // Note: SQLite foreign keys (CASCADE) are OFF by default in tests,
        // so the verse link becomes an orphan — exactly what the detection query should find.
        testDb.database.knowledgeGraphQueries.insertNode(
            name = "TestNode",
            type = "Person",
            description = "",
            properties = "{}"
        )
        val nodeId = testDb.database.knowledgeGraphQueries.lastInsertNodeId().executeAsOne()
        testDb.database.knowledgeGraphQueries.insertNodeVerse(
            nodeId = nodeId,
            globalVerseId = 1001001
        )
        testDb.database.knowledgeGraphQueries.deleteNode(nodeId)

        val orphans = testDb.database.knowledgeGraphQueries
            .orphanedNodeVerseLinks(maxResults = 10)
            .executeAsList()

        // Without PRAGMA foreign_keys = ON, CASCADE does not fire,
        // so the orphan detection query should find the dangling link.
        assertEquals(1, orphans.size, "Orphan detection should find the dangling verse link")
        assertEquals(nodeId, orphans[0].node_id)
    }

    // ── Atlas Location-Verse Links ────────────────────────────────

    @Test
    fun `no orphaned atlas location-verse links`() {
        val orphans = testDb.database.atlasQueries
            .orphanedLocationVerseLinks(maxResults = 10)
            .executeAsList()
        assertTrue(
            orphans.isEmpty(),
            "Found ${orphans.size} atlas_location_verses with invalid location_id"
        )
    }

    // ── Timeline Event-Verse Links ────────────────────────────────

    @Test
    fun `no orphaned timeline event-verse links`() {
        val orphans = testDb.database.timelineQueries
            .orphanedEventVerseLinks(maxResults = 10)
            .executeAsList()
        assertTrue(
            orphans.isEmpty(),
            "Found ${orphans.size} timeline_event_verses with invalid event_id"
        )
    }

    // ── Atlas Coordinate Validation ───────────────────────────────

    @Test
    fun `no atlas locations with invalid coordinates`() {
        val invalid = testDb.database.atlasQueries
            .invalidCoordinates()
            .executeAsList()
        assertTrue(
            invalid.isEmpty(),
            "Found ${invalid.size} locations with out-of-range coordinates: " +
                "${invalid.map { "${it.name}: (${it.latitude}, ${it.longitude})" }}"
        )
    }

    // ── Entities (Place) ↔ Atlas Locations ────────────────────────

    @Test
    fun `Place-type graph nodes are consistent with atlas locations`() {
        // This test verifies that when both modules have data, Place entities
        // match atlas locations by name. On empty DB this passes trivially.
        val placeNodes = testDb.database.knowledgeGraphQueries
            .getNodesByType("Place")
            .executeAsList()
        val locationNames = testDb.database.atlasQueries
            .getAllLocations()
            .executeAsList()
            .map { it.name.lowercase() }
            .toSet()

        if (placeNodes.isNotEmpty() && locationNames.isNotEmpty()) {
            val matched = placeNodes.count { it.name.lowercase() in locationNames }
            val matchRate = matched.toDouble() / placeNodes.size
            assertTrue(
                matchRate >= 0.5,
                "Less than 50% of Place nodes have matching atlas locations " +
                    "($matched/${placeNodes.size} = ${(matchRate * 100).toInt()}%)"
            )
        }
    }

    // ── Entities (Event) ↔ Timeline Events ────────────────────────

    @Test
    fun `Event-type graph nodes are consistent with timeline events`() {
        val eventNodes = testDb.database.knowledgeGraphQueries
            .getNodesByType("Event")
            .executeAsList()
        val timelineEventTitles = testDb.database.timelineQueries
            .getAllEvents()
            .executeAsList()
            .map { it.title.lowercase() }
            .toSet()

        if (eventNodes.isNotEmpty() && timelineEventTitles.isNotEmpty()) {
            val matched = eventNodes.count { it.name.lowercase() in timelineEventTitles }
            val matchRate = matched.toDouble() / eventNodes.size
            assertTrue(
                matchRate >= 0.3,
                "Less than 30% of Event nodes have matching timeline events " +
                    "($matched/${eventNodes.size} = ${(matchRate * 100).toInt()}%)"
            )
        }
    }
}
