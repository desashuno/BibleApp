package org.biblestudio.features.knowledge_graph.domain.repositories

import org.biblestudio.features.knowledge_graph.domain.entities.GraphCluster
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEntity

/**
 * Repository for querying the biblical knowledge graph.
 */
interface KnowledgeGraphRepository {

    /** Returns a single entity by ID, or null if not found. */
    suspend fun getEntity(entityId: Long): Result<GraphEntity?>

    /** Returns entities linked to a specific verse. */
    suspend fun getEntitiesForVerse(globalVerseId: Long): Result<List<GraphEntity>>

    /** BFS traversal returning a cluster of entities up to [depth] hops from [entityId]. */
    suspend fun getRelated(entityId: Long, depth: Int = 2): Result<GraphCluster>

    /** Full-text search on entity name and description. */
    suspend fun searchEntities(query: String, maxResults: Long = 50): Result<List<GraphEntity>>

    /** Returns all entities of a given type. */
    suspend fun getEntitiesByType(type: String): Result<List<GraphEntity>>

    /** Returns total node count. */
    suspend fun getNodeCount(): Result<Long>
}
