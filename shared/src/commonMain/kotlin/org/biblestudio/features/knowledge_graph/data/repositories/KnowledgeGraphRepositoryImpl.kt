package org.biblestudio.features.knowledge_graph.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.knowledge_graph.data.mappers.toGraphEdge
import org.biblestudio.features.knowledge_graph.data.mappers.toGraphEntity
import org.biblestudio.features.knowledge_graph.domain.entities.GraphCluster
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEdge
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEntity
import org.biblestudio.features.knowledge_graph.domain.repositories.KnowledgeGraphRepository

internal class KnowledgeGraphRepositoryImpl(
    private val database: BibleStudioDatabase
) : KnowledgeGraphRepository {

    override suspend fun getEntity(entityId: Long): Result<GraphEntity?> = runCatching {
        val node = database.knowledgeGraphQueries
            .getNodeById(entityId)
            .executeAsOneOrNull() ?: return@runCatching null

        val verseIds = database.knowledgeGraphQueries
            .getVersesForNode(entityId)
            .executeAsList()
            .map { it.global_verse_id }

        node.toGraphEntity(verseIds)
    }

    override suspend fun getEntitiesForVerse(globalVerseId: Long): Result<List<GraphEntity>> = runCatching {
        database.knowledgeGraphQueries
            .getNodesForVerse(globalVerseId)
            .executeAsList()
            .map { it.toGraphEntity() }
    }

    override suspend fun getRelated(entityId: Long, depth: Int): Result<GraphCluster> = runCatching {
        val visitedNodeIds = mutableSetOf<Long>()
        val collectedNodes = mutableListOf<GraphEntity>()
        val collectedEdges = mutableListOf<GraphEdge>()
        val frontier = mutableListOf(entityId)

        for (hop in 0 until depth) {
            val nextFrontier = mutableListOf<Long>()
            for (currentId in frontier) {
                if (!visitedNodeIds.add(currentId)) continue

                val node = database.knowledgeGraphQueries
                    .getNodeById(currentId)
                    .executeAsOneOrNull()
                if (node != null) {
                    val verseIds = database.knowledgeGraphQueries
                        .getVersesForNode(currentId)
                        .executeAsList()
                        .map { it.global_verse_id }
                    collectedNodes.add(node.toGraphEntity(verseIds))
                }

                val edges = database.knowledgeGraphQueries
                    .getAllEdgesForNode(currentId)
                    .executeAsList()
                    .map { it.toGraphEdge() }

                for (edge in edges) {
                    if (collectedEdges.none { it.id == edge.id }) {
                        collectedEdges.add(edge)
                    }
                    val neighborId = if (edge.sourceId == currentId) edge.targetId else edge.sourceId
                    if (neighborId !in visitedNodeIds) {
                        nextFrontier.add(neighborId)
                    }
                }
            }
            frontier.clear()
            frontier.addAll(nextFrontier)
        }

        // Collect remaining frontier nodes (last hop boundary)
        for (nodeId in frontier) {
            if (visitedNodeIds.add(nodeId)) {
                val node = database.knowledgeGraphQueries
                    .getNodeById(nodeId)
                    .executeAsOneOrNull()
                if (node != null) {
                    collectedNodes.add(node.toGraphEntity())
                }
            }
        }

        GraphCluster(
            centerEntityId = entityId,
            nodes = collectedNodes,
            edges = collectedEdges,
            depth = depth
        )
    }

    override suspend fun searchEntities(query: String, maxResults: Long): Result<List<GraphEntity>> = runCatching {
        database.knowledgeGraphQueries
            .searchNodes(query = query, maxResults = maxResults)
            .executeAsList()
            .map { it.toGraphEntity() }
    }

    override suspend fun getEntitiesByType(type: String): Result<List<GraphEntity>> = runCatching {
        database.knowledgeGraphQueries
            .getNodesByType(type)
            .executeAsList()
            .map { it.toGraphEntity() }
    }

    override suspend fun getNodeCount(): Result<Long> = runCatching {
        database.knowledgeGraphQueries
            .nodeCount()
            .executeAsOne()
    }
}
