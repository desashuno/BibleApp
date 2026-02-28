package org.biblestudio.ui.panes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.knowledge_graph.component.GraphEdgeUi
import org.biblestudio.features.knowledge_graph.component.KnowledgeGraphState
import org.biblestudio.features.knowledge_graph.domain.entities.EntityType
import org.biblestudio.features.knowledge_graph.domain.entities.GraphEntity
import org.biblestudio.features.knowledge_graph.domain.layout.ForceDirectedLayout
import org.biblestudio.features.knowledge_graph.domain.layout.NodePosition
import org.biblestudio.ui.theme.Spacing

/**
 * Knowledge Graph pane: force-directed graph canvas with entity detail card.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun KnowledgeGraphPane(
    stateFlow: StateFlow<KnowledgeGraphState>,
    onEntitySelected: (GraphEntity) -> Unit,
    onDepthChanged: (Int) -> Unit,
    onSearch: (String) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            placeholder = { Text("Search entities…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Space16)
        )

        // Depth slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Depth: ${state.depth}",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(Spacing.Space8))
            Slider(
                value = state.depth.toFloat(),
                onValueChange = { onDepthChanged(it.toInt()) },
                valueRange = 1f..4f,
                steps = 2,
                modifier = Modifier.weight(1f)
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(Spacing.Space24)
            )
        }

        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(Spacing.Space16)
            )
        }

        // Graph canvas or search results
        if (state.clusterNodes.isNotEmpty()) {
            GraphCanvas(
                nodes = state.clusterNodes,
                edges = state.clusterEdges,
                selectedEntity = state.selectedEntity,
                onNodeTapped = onEntitySelected,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        } else if (state.searchResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(state.searchResults, key = { it.id }) { entity ->
                    EntityListItem(entity = entity, onClick = { onEntitySelected(entity) })
                }
            }
        } else if (!state.isLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select a verse or search for entities",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Entity detail card
        state.selectedEntity?.let { entity ->
            HorizontalDivider()
            EntityDetailCard(entity = entity, onDismiss = onClearSelection)
        }
    }
}

@Suppress("ktlint:standard:function-naming", "MagicNumber")
@Composable
private fun GraphCanvas(
    nodes: List<GraphEntity>,
    edges: List<GraphEdgeUi>,
    selectedEntity: GraphEntity?,
    onNodeTapped: (GraphEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val layout = remember(nodes, edges) {
        val nodeIds = nodes.map { it.id }
        val edgePairs = edges.map { it.sourceId to it.targetId }
        ForceDirectedLayout(width = 600f, height = 400f).compute(nodeIds, edgePairs)
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.3f, 5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(nodes) {
                detectTapGestures { tapOffset ->
                    val tapped = findTappedNode(
                        tapOffset = tapOffset,
                        nodes = nodes,
                        positions = layout,
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        nodeRadius = NODE_RADIUS
                    )
                    tapped?.let { onNodeTapped(it) }
                }
            }
    ) {
        // Draw edges
        for (edge in edges) {
            val from = layout[edge.sourceId] ?: continue
            val to = layout[edge.targetId] ?: continue
            drawLine(
                color = Color.Gray,
                start = Offset(from.x * scale + offsetX, from.y * scale + offsetY),
                end = Offset(to.x * scale + offsetX, to.y * scale + offsetY),
                strokeWidth = 1.5f
            )
        }

        // Draw nodes
        for (node in nodes) {
            val pos = layout[node.id] ?: continue
            val center = Offset(pos.x * scale + offsetX, pos.y * scale + offsetY)
            val color = entityColor(node.type)
            val radius = NODE_RADIUS * scale

            drawCircle(color = color, radius = radius, center = center)

            // Selected highlight ring
            if (selectedEntity?.id == node.id) {
                drawCircle(
                    color = Color.White,
                    radius = radius + 3f,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun EntityDetailCard(entity: GraphEntity, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Space8)
    ) {
        Column(modifier = Modifier.padding(Spacing.Space16)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${entityTypeIcon(entity.type)} ${entity.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✕",
                    modifier = Modifier.clickable(onClick = onDismiss).padding(Spacing.Space4),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(Spacing.Space4))
            Text(
                text = entity.type.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (entity.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.Space8))
                Text(
                    text = entity.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (entity.verseIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.Space8))
                Text(
                    text = "Verses: ${entity.verseIds.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun EntityListItem(entity: GraphEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
    ) {
        Text(
            text = "${entityTypeIcon(entity.type)} ${entity.name}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = entity.type.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.Space16))
}

@Suppress("MagicNumber")
private fun entityColor(type: EntityType): Color = when (type) {
    EntityType.Person -> Color(0xFF4CAF50)
    EntityType.Place -> Color(0xFF2196F3)
    EntityType.Event -> Color(0xFFFF9800)
    EntityType.Concept -> Color(0xFF9C27B0)
    EntityType.Book -> Color(0xFF795548)
}

private fun findTappedNode(
    tapOffset: Offset,
    nodes: List<GraphEntity>,
    positions: Map<Long, NodePosition>,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    nodeRadius: Float
): GraphEntity? {
    val hitRadius = nodeRadius * scale + HIT_PADDING
    return nodes.firstOrNull { node ->
        val pos = positions[node.id] ?: return@firstOrNull false
        val cx = pos.x * scale + offsetX
        val cy = pos.y * scale + offsetY
        val dx = tapOffset.x - cx
        val dy = tapOffset.y - cy
        dx * dx + dy * dy <= hitRadius * hitRadius
    }
}

private const val NODE_RADIUS = 16f
private const val HIT_PADDING = 8f

/**
 * Returns a Unicode icon for the given entity type.
 */
private fun entityTypeIcon(type: EntityType): String = when (type) {
    EntityType.Person -> "\uD83D\uDC64"   // 👤
    EntityType.Place -> "\uD83D\uDCCD"    // 📍
    EntityType.Event -> "\uD83D\uDCC5"    // 📅
    EntityType.Concept -> "\uD83D\uDCA1"  // 💡
    EntityType.Book -> "\uD83D\uDCD6"     // 📖
}
