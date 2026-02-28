package org.biblestudio.features.knowledge_graph.domain.entities

/**
 * Represents a node in the biblical knowledge graph.
 *
 * @param id Database primary key.
 * @param name Display name of the entity (e.g., "Abraham", "Jerusalem").
 * @param type Category of the entity.
 * @param description Human-readable description.
 * @param properties JSON-encoded extra attributes.
 * @param verseIds List of BBCCCVVV-encoded verse identifiers linked to this entity.
 */
data class GraphEntity(
    val id: Long,
    val name: String,
    val type: EntityType,
    val description: String,
    val properties: String = "{}",
    val verseIds: List<Long> = emptyList()
)
