package org.biblestudio.features.knowledge_graph.domain.entities

/**
 * A subgraph cluster returned by BFS traversal from a center entity.
 *
 * @param centerEntityId The ID of the entity from which traversal started.
 * @param nodes All entities discovered within the requested depth.
 * @param edges All edges connecting the discovered entities.
 * @param depth The maximum hop depth used for the traversal.
 */
data class GraphCluster(
    val centerEntityId: Long,
    val nodes: List<GraphEntity>,
    val edges: List<GraphEdge>,
    val depth: Int
)
