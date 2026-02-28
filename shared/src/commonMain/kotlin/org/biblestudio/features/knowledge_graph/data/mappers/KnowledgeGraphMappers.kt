package org.biblestudio.features.knowledge_graph.data.mappers

import migrations.Graph_edges
import migrations.Graph_nodes
import org.biblestudio.features.knowledge_graph.domain.entities.EntityType
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEdge
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEntity
import org.biblestudio.features.knowledge_graph.domain.entities.RelationshipType

internal fun Graph_nodes.toGraphEntity(verseIds: List<Long> = emptyList()): GraphEntity = GraphEntity(
    id = id,
    name = name,
    type = EntityType.fromString(type),
    description = description,
    properties = properties,
    verseIds = verseIds
)

internal fun Graph_edges.toGraphEdge(): GraphEdge = GraphEdge(
    id = id,
    sourceId = source_id,
    targetId = target_id,
    relationship = RelationshipType.fromString(relationship),
    weight = weight
)
