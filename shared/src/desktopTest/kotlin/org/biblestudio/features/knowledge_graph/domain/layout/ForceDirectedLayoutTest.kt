package org.biblestudio.features.knowledge_graph.domain.layout

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ForceDirectedLayoutTest {

    @Test
    fun `empty graph returns empty map`() {
        val layout = ForceDirectedLayout()
        val result = layout.compute(emptyList(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single node is placed at center`() {
        val layout = ForceDirectedLayout(width = 800f, height = 600f)
        val result = layout.compute(listOf(1L), emptyList())
        assertEquals(1, result.size)
        assertEquals(400f, result[1L]!!.x)
        assertEquals(300f, result[1L]!!.y)
    }

    @Test
    fun `multiple nodes produce non-overlapping positions`() {
        val nodeIds = (1L..10L).toList()
        val edges = listOf(1L to 2L, 2L to 3L, 3L to 4L, 4L to 5L, 1L to 6L, 6L to 7L)
        val layout = ForceDirectedLayout(width = 800f, height = 600f, iterations = 100)
        val positions = layout.compute(nodeIds, edges)

        assertEquals(10, positions.size)

        // Check no two nodes overlap (minimum distance > 1 pixel)
        val positionList = positions.values.toList()
        for (i in positionList.indices) {
            for (j in i + 1 until positionList.size) {
                val dx = positionList[i].x - positionList[j].x
                val dy = positionList[i].y - positionList[j].y
                val dist = sqrt(dx * dx + dy * dy)
                assertTrue(dist > 1f, "Nodes $i and $j overlap at distance $dist")
            }
        }
    }

    @Test
    fun `all nodes are within canvas bounds`() {
        val width = 600f
        val height = 400f
        val nodeIds = (1L..20L).toList()
        val edges = (1L..19L).map { it to it + 1 }
        val layout = ForceDirectedLayout(width = width, height = height)
        val positions = layout.compute(nodeIds, edges)

        for ((id, pos) in positions) {
            assertTrue(pos.x >= 0f && pos.x <= width, "Node $id x=${pos.x} out of bounds")
            assertTrue(pos.y >= 0f && pos.y <= height, "Node $id y=${pos.y} out of bounds")
        }
    }

    @Test
    fun `connected nodes are closer than disconnected nodes`() {
        val nodeIds = listOf(1L, 2L, 3L)
        val edges = listOf(1L to 2L) // 1-2 connected, 3 isolated
        val layout = ForceDirectedLayout(width = 800f, height = 600f, iterations = 100)
        val positions = layout.compute(nodeIds, edges)

        val dist12 = distance(positions[1L]!!, positions[2L]!!)
        val dist13 = distance(positions[1L]!!, positions[3L]!!)
        val dist23 = distance(positions[2L]!!, positions[3L]!!)

        // Connected pair should be closer than at least one disconnected pair
        assertTrue(
            dist12 < dist13 || dist12 < dist23,
            "Connected nodes (dist=$dist12) should be closer than disconnected (dist13=$dist13, dist23=$dist23)"
        )
    }

    private fun distance(a: NodePosition, b: NodePosition): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }
}
