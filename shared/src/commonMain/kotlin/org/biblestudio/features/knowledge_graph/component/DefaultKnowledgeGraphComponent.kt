package org.biblestudio.features.knowledge_graph.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.knowledge_graph.domain.entities.EntityType
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEntity
import org.biblestudio.features.knowledge_graph.domain.repositories.KnowledgeGraphRepository

/**
 * Default [KnowledgeGraphComponent] managing graph exploration and entity selection.
 */
internal class DefaultKnowledgeGraphComponent(
    componentContext: ComponentContext,
    private val repository: KnowledgeGraphRepository,
    private val verseBus: VerseBus
) : KnowledgeGraphComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(KnowledgeGraphState())
    override val state: StateFlow<KnowledgeGraphState> = _state.asStateFlow()

    init {
        observeVerseBus()
    }

    override fun onEntitySelected(entity: GraphEntity) {
        _state.update { it.copy(selectedEntity = entity) }
        loadCluster(entity.id, _state.value.depth)
    }

    override fun onDepthChanged(depth: Int) {
        val clamped = depth.coerceIn(MIN_DEPTH, MAX_DEPTH)
        _state.update { it.copy(depth = clamped) }
        _state.value.centerEntity?.let { center ->
            loadCluster(center.id, clamped)
        }
    }

    override fun onSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        scope.launch {
            repository.searchEntities(query)
                .onSuccess { results ->
                    _state.update { it.copy(searchResults = results) }
                }
                .onFailure { e ->
                    Napier.e("Failed to search entities", e)
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    override fun onFilterByType(type: EntityType) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getEntitiesByType(type.name)
                .onSuccess { entities ->
                    _state.update { it.copy(searchResults = entities, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to filter entities by type", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    override fun onClearSelection() {
        _state.update { it.copy(selectedEntity = null) }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    loadEntitiesForVerse(event.globalVerseId.toLong())
                }
        }
    }

    private fun loadEntitiesForVerse(globalVerseId: Long) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getEntitiesForVerse(globalVerseId)
                .onSuccess { entities ->
                    _state.update { it.copy(searchResults = entities, isLoading = false) }
                    // Auto-select the first entity if available
                    entities.firstOrNull()?.let { first ->
                        _state.update { it.copy(centerEntity = first, selectedEntity = first) }
                        loadCluster(first.id, _state.value.depth)
                    }
                }
                .onFailure { e ->
                    Napier.e("Failed to load entities for verse", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadCluster(entityId: Long, depth: Int) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getRelated(entityId, depth)
                .onSuccess { cluster ->
                    val nodeMap = cluster.nodes.associateBy { it.id }
                    val edgesUi = cluster.edges.map { edge ->
                        GraphEdgeUi(
                            id = edge.id,
                            sourceId = edge.sourceId,
                            targetId = edge.targetId,
                            sourceName = nodeMap[edge.sourceId]?.name ?: "?",
                            targetName = nodeMap[edge.targetId]?.name ?: "?",
                            relationship = edge.relationship.displayName,
                            weight = edge.weight
                        )
                    }
                    val centerNode = nodeMap[entityId]
                    _state.update {
                        it.copy(
                            centerEntity = centerNode ?: it.centerEntity,
                            clusterNodes = cluster.nodes,
                            clusterEdges = edgesUi,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    Napier.e("Failed to load graph cluster", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    companion object {
        private const val MIN_DEPTH = 1
        private const val MAX_DEPTH = 4
    }
}
