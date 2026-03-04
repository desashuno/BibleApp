package org.biblestudio.ui.workspace

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.biblestudio.features.workspace.domain.model.PanePlacement

/** Drop zone within a pane (edges create splits, center creates tabs). */
enum class DropZone {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    CENTER;

    fun toPanePlacement(): PanePlacement = when (this) {
        LEFT -> PanePlacement.LEFT
        RIGHT -> PanePlacement.RIGHT
        TOP -> PanePlacement.ABOVE
        BOTTOM -> PanePlacement.BELOW
        CENTER -> PanePlacement.TAB
    }
}

/** Info about the pane currently being dragged. */
data class DragInfo(
    val paneType: String,
    val sourcePath: List<Int>
)

/** Info about the current drop target. */
data class DropTargetInfo(
    val path: List<Int>,
    val zone: DropZone
)

private const val EDGE_THRESHOLD = 0.3f

/**
 * Manages drag-and-drop state for workspace pane rearrangement.
 *
 * Each [PaneContainer] registers its window-relative bounds. During a drag,
 * the pointer position is compared against all registered bounds to determine
 * the current [DropTargetInfo].
 */
class WorkspaceDragState {
    var dragInfo by mutableStateOf<DragInfo?>(null)
        private set
    var dropTarget by mutableStateOf<DropTargetInfo?>(null)
        private set
    var pointerPosition by mutableStateOf(Offset.Unspecified)
        private set

    private val paneBounds = mutableStateMapOf<String, Rect>()

    val isDragging: Boolean get() = dragInfo != null

    fun startDrag(paneType: String, sourcePath: List<Int>) {
        dragInfo = DragInfo(paneType, sourcePath)
        dropTarget = null
        pointerPosition = Offset.Unspecified
    }

    fun updatePointer(windowPosition: Offset) {
        val info = dragInfo ?: return
        pointerPosition = windowPosition
        dropTarget = findDropTarget(windowPosition, info.sourcePath)
    }

    fun endDrag(): Pair<DragInfo, DropTargetInfo>? {
        val info = dragInfo
        val target = dropTarget
        dragInfo = null
        dropTarget = null
        pointerPosition = Offset.Unspecified
        return if (info != null && target != null) info to target else null
    }

    fun cancelDrag() {
        dragInfo = null
        dropTarget = null
        pointerPosition = Offset.Unspecified
    }

    fun registerPane(path: List<Int>, bounds: Rect) {
        paneBounds[pathKey(path)] = bounds
    }

    fun unregisterPane(path: List<Int>) {
        paneBounds.remove(pathKey(path))
    }

    private fun findDropTarget(position: Offset, sourcePath: List<Int>): DropTargetInfo? {
        val sourceKey = pathKey(sourcePath)
        for ((key, rect) in paneBounds) {
            if (key == sourceKey) continue
            if (rect.contains(position)) {
                val zone = calculateDropZone(position, rect)
                val path = keyToPath(key)
                return DropTargetInfo(path, zone)
            }
        }
        return null
    }

    private fun calculateDropZone(position: Offset, rect: Rect): DropZone {
        val relX = (position.x - rect.left) / rect.width
        val relY = (position.y - rect.top) / rect.height
        return when {
            relX < EDGE_THRESHOLD -> DropZone.LEFT
            relX > (1f - EDGE_THRESHOLD) -> DropZone.RIGHT
            relY < EDGE_THRESHOLD -> DropZone.TOP
            relY > (1f - EDGE_THRESHOLD) -> DropZone.BOTTOM
            else -> DropZone.CENTER
        }
    }

    private fun pathKey(path: List<Int>): String = path.joinToString(",")

    private fun keyToPath(key: String): List<Int> = if (key.isEmpty()) {
        emptyList()
    } else {
        key.split(",").map { it.toInt() }
    }
}
