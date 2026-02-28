package org.biblestudio.features.knowledge_graph.domain.layout

import kotlin.math.sqrt

/**
 * 2D position of a node in the layout.
 */
data class NodePosition(val x: Float, val y: Float)

/**
 * Fruchterman-Reingold force-directed layout algorithm.
 *
 * Given a set of node IDs and edges (pairs of node IDs), computes
 * non-overlapping 2D positions that visualise the graph structure.
 *
 * @param width  Canvas width in logical pixels.
 * @param height Canvas height in logical pixels.
 * @param iterations Number of simulation steps (more = better quality, slower).
 */
class ForceDirectedLayout(
    private val width: Float = 800f,
    private val height: Float = 600f,
    private val iterations: Int = DEFAULT_ITERATIONS
) {

    /**
     * Computes positions for each node.
     *
     * @param nodeIds List of unique node identifiers.
     * @param edges   List of (sourceId, targetId) pairs.
     * @return Map from node ID to its computed [NodePosition].
     */
    fun compute(
        nodeIds: List<Long>,
        edges: List<Pair<Long, Long>>
    ): Map<Long, NodePosition> {
        if (nodeIds.isEmpty()) return emptyMap()
        if (nodeIds.size == 1) return mapOf(nodeIds.first() to NodePosition(width / 2f, height / 2f))

        val area = width * height
        val k = sqrt(area / nodeIds.size.toFloat())

        // Initialise positions in a circle
        val positions = mutableMapOf<Long, FloatArray>()
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) * INITIAL_RADIUS_FACTOR
        nodeIds.forEachIndexed { i, id ->
            val angle = 2f * Math.PI.toFloat() * i / nodeIds.size
            positions[id] = floatArrayOf(
                cx + radius * kotlin.math.cos(angle),
                cy + radius * kotlin.math.sin(angle)
            )
        }

        val displacements = mutableMapOf<Long, FloatArray>()
        nodeIds.forEach { id -> displacements[id] = floatArrayOf(0f, 0f) }

        var temperature = width / TEMP_DIVISOR

        for (iter in 0 until iterations) {
            // Reset displacements
            nodeIds.forEach { id -> displacements[id] = floatArrayOf(0f, 0f) }

            // Repulsive forces between all pairs
            for (i in nodeIds.indices) {
                for (j in i + 1 until nodeIds.size) {
                    val u = nodeIds[i]
                    val v = nodeIds[j]
                    val pu = positions[u]!!
                    val pv = positions[v]!!
                    val dx = pu[0] - pv[0]
                    val dy = pu[1] - pv[1]
                    val dist = maxOf(sqrt(dx * dx + dy * dy), MIN_DIST)
                    val repForce = k * k / dist
                    val fx = dx / dist * repForce
                    val fy = dy / dist * repForce
                    displacements[u]!![0] += fx
                    displacements[u]!![1] += fy
                    displacements[v]!![0] -= fx
                    displacements[v]!![1] -= fy
                }
            }

            // Attractive forces along edges
            for ((u, v) in edges) {
                val pu = positions[u] ?: continue
                val pv = positions[v] ?: continue
                val dx = pu[0] - pv[0]
                val dy = pu[1] - pv[1]
                val dist = maxOf(sqrt(dx * dx + dy * dy), MIN_DIST)
                val attForce = dist * dist / k
                val fx = dx / dist * attForce
                val fy = dy / dist * attForce
                displacements[u]!![0] -= fx
                displacements[u]!![1] -= fy
                displacements[v]!![0] += fx
                displacements[v]!![1] += fy
            }

            // Apply displacements clamped by temperature
            for (id in nodeIds) {
                val disp = displacements[id]!!
                val dispLen = maxOf(sqrt(disp[0] * disp[0] + disp[1] * disp[1]), MIN_DIST)
                val pos = positions[id]!!
                pos[0] += disp[0] / dispLen * minOf(dispLen, temperature)
                pos[1] += disp[1] / dispLen * minOf(dispLen, temperature)
                // Keep within bounds with margin
                pos[0] = pos[0].coerceIn(MARGIN, width - MARGIN)
                pos[1] = pos[1].coerceIn(MARGIN, height - MARGIN)
            }

            temperature *= COOLING_FACTOR
        }

        return positions.mapValues { (_, pos) -> NodePosition(pos[0], pos[1]) }
    }

    companion object {
        private const val DEFAULT_ITERATIONS = 50
        private const val MIN_DIST = 0.01f
        private const val MARGIN = 30f
        private const val INITIAL_RADIUS_FACTOR = 0.3f
        private const val TEMP_DIVISOR = 10f
        private const val COOLING_FACTOR = 0.95f
    }
}
