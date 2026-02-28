package org.biblestudio.features.knowledge_graph.domain.entities

/**
 * Types of relationships between entities in the biblical knowledge graph.
 */
enum class RelationshipType(val displayName: String) {
    RelatedTo("Related To"),
    ChildOf("Child Of"),
    SpouseOf("Spouse Of"),
    RulerOf("Ruler Of"),
    LocatedIn("Located In"),
    MentionedIn("Mentioned In"),
    AuthorOf("Author Of"),
    PartOf("Part Of"),
    PredecessorOf("Predecessor Of"),
    SuccessorOf("Successor Of"),
    ContemporaryOf("Contemporary Of");

    companion object {
        fun fromString(value: String): RelationshipType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: RelatedTo
    }
}
