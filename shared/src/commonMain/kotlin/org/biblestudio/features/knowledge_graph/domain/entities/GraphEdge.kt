package org.biblestudio.features.knowledge_graph.domain.entities

/**
 * Represents a directed relationship between two nodes in the knowledge graph.
 *
 * @param id Database primary key.
 * @param sourceId ID of the source [GraphEntity].
 * @param targetId ID of the target [GraphEntity].
 * @param relationship Type of the relationship.
 * @param weight Confidence or importance weight (0.0–1.0).
 */
data class GraphEdge(
    val id: Long,
    val sourceId: Long,
    val targetId: Long,
    val relationship: RelationshipType,
    val weight: Double
)
