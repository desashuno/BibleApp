package org.biblestudio.features.knowledge_graph.domain.entities

/**
 * Categories of entities in the biblical knowledge graph.
 */
enum class EntityType(val displayName: String) {
    Person("Person"),
    Place("Place"),
    Event("Event"),
    Concept("Concept"),
    Book("Book");

    companion object {
        fun fromString(value: String): EntityType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Concept
    }
}
