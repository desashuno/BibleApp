package org.biblestudio.features.knowledge_graph.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.knowledge_graph.domain.entities.EntityType
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEntity

/**
 * Observable state for the Knowledge Graph pane.
 */
data class KnowledgeGraphState(
    val centerEntity: GraphEntity? = null,
    val clusterNodes: List<GraphEntity> = emptyList(),
    val clusterEdges: List<GraphEdgeUi> = emptyList(),
    val selectedEntity: GraphEntity? = null,
    val depth: Int = 2,
    val zoomLevel: Float = 1f,
    val searchQuery: String = "",
    val searchResults: List<GraphEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI-friendly edge representation with resolved node names.
 */
data class GraphEdgeUi(
    val id: Long,
    val sourceId: Long,
    val targetId: Long,
    val sourceName: String,
    val targetName: String,
    val relationship: String,
    val weight: Double
)

/**
 * Business-logic boundary for the Knowledge Graph pane.
 *
 * Subscribes to [LinkEvent.VerseSelected] to load entities for the active
 * verse and provides graph exploration operations.
 */
interface KnowledgeGraphComponent {

    /** The current knowledge graph state observable. */
    val state: StateFlow<KnowledgeGraphState>

    /** Select an entity and make it the center of the graph. */
    fun onEntitySelected(entity: GraphEntity)

    /** Change the BFS traversal depth (1–4 hops). */
    fun onDepthChanged(depth: Int)

    /** Perform a full-text search on entities. */
    fun onSearch(query: String)

    /** Filter entities by type. */
    fun onFilterByType(type: EntityType)

    /** Clear the current selection. */
    fun onClearSelection()
}
