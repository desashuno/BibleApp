package org.biblestudio.features.knowledge_graph.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.test.TestDatabase

class KnowledgeGraphRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: KnowledgeGraphRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = KnowledgeGraphRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun insertNode(name: String, type: String, description: String = ""): Long {
        testDb.database.knowledgeGraphQueries.insertNode(
            name = name,
            type = type,
            description = description,
            properties = "{}"
        )
        return testDb.database.knowledgeGraphQueries.lastInsertNodeId().executeAsOne()
    }

    private fun insertEdge(sourceId: Long, targetId: Long, relationship: String, weight: Double = 1.0) {
        testDb.database.knowledgeGraphQueries.insertEdge(
            sourceId = sourceId,
            targetId = targetId,
            relationship = relationship,
            weight = weight
        )
    }

    private fun insertNodeVerse(nodeId: Long, globalVerseId: Long) {
        testDb.database.knowledgeGraphQueries.insertNodeVerse(
            nodeId = nodeId,
            globalVerseId = globalVerseId
        )
    }

    @Test
    fun `getEntity returns entity with verse IDs`() = runTest {
        val id = insertNode("Abraham", "Person", "Father of many nations")
        insertNodeVerse(id, 1001001)
        insertNodeVerse(id, 1012001)

        val entity = repo.getEntity(id).getOrThrow()
        assertNotNull(entity)
        assertEquals("Abraham", entity.name)
        assertEquals(2, entity.verseIds.size)
    }

    @Test
    fun `getEntity returns null for non-existent ID`() = runTest {
        val entity = repo.getEntity(99999).getOrThrow()
        assertNull(entity)
    }

    @Test
    fun `getEntitiesForVerse returns linked entities`() = runTest {
        val id1 = insertNode("Jerusalem", "Place")
        val id2 = insertNode("David", "Person")
        insertNodeVerse(id1, 1001001)
        insertNodeVerse(id2, 1001001)

        val entities = repo.getEntitiesForVerse(1001001).getOrThrow()
        assertEquals(2, entities.size)
    }

    @Test
    fun `BFS traversal returns correct cluster at depth 2`() = runTest {
        // A -> B -> C -> D
        val a = insertNode("A", "Person")
        val b = insertNode("B", "Person")
        val c = insertNode("C", "Person")
        val d = insertNode("D", "Person")
        insertEdge(a, b, "RelatedTo")
        insertEdge(b, c, "RelatedTo")
        insertEdge(c, d, "RelatedTo")

        val cluster = repo.getRelated(a, depth = 2).getOrThrow()

        // depth 2: A (hop 0) -> B (hop 1) -> C (hop 2), D should NOT be traversed edges-wise but C's edge to D adds D to boundary
        assertTrue(cluster.nodes.any { it.name == "A" })
        assertTrue(cluster.nodes.any { it.name == "B" })
        assertTrue(cluster.nodes.any { it.name == "C" })
        assertEquals(a, cluster.centerEntityId)
        assertEquals(2, cluster.depth)
    }

    @Test
    fun `BFS traversal at depth 1 returns only direct neighbors`() = runTest {
        val a = insertNode("A", "Person")
        val b = insertNode("B", "Person")
        val c = insertNode("C", "Person")
        insertEdge(a, b, "RelatedTo")
        insertEdge(b, c, "RelatedTo")

        val cluster = repo.getRelated(a, depth = 1).getOrThrow()
        assertTrue(cluster.nodes.any { it.name == "A" })
        assertTrue(cluster.nodes.any { it.name == "B" })
    }

    @Test
    fun `searchEntities finds entity by partial name`() = runTest {
        insertNode("Abraham", "Person", "Father of many nations")
        insertNode("Moses", "Person", "Leader of the Exodus")

        val results = repo.searchEntities("Abra*").getOrThrow()
        assertEquals(1, results.size)
        assertEquals("Abraham", results.first().name)
    }

    @Test
    fun `searchEntities finds entity by description`() = runTest {
        insertNode("Abraham", "Person", "Father of many nations")

        val results = repo.searchEntities("nations*").getOrThrow()
        assertEquals(1, results.size)
    }

    @Test
    fun `getEntitiesByType filters correctly`() = runTest {
        insertNode("Abraham", "Person")
        insertNode("Jerusalem", "Place")
        insertNode("Moses", "Person")

        val persons = repo.getEntitiesByType("Person").getOrThrow()
        assertEquals(2, persons.size)

        val places = repo.getEntitiesByType("Place").getOrThrow()
        assertEquals(1, places.size)
    }

    @Test
    fun `getNodeCount returns correct count`() = runTest {
        insertNode("A", "Person")
        insertNode("B", "Place")

        val count = repo.getNodeCount().getOrThrow()
        assertEquals(2, count)
    }
}
