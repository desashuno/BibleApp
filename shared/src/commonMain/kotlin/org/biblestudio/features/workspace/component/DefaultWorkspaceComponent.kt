package org.biblestudio.features.workspace.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.biblestudio.core.error.AppError
import org.biblestudio.features.workspace.domain.entities.WorkspaceLayout
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.WorkspacePreset
import org.biblestudio.features.workspace.domain.model.WorkspaceState
import org.biblestudio.features.workspace.domain.repositories.WorkspaceRepository

private const val AUTO_SAVE_DEBOUNCE_MS = 2_000L

/**
 * Default implementation of [WorkspaceComponent] backed by Decompose lifecycle
 * and [WorkspaceRepository] for persistence.
 *
 * Layout mutations trigger a debounced auto-save (2 s). Calling [saveWorkspace]
 * flushes the save immediately.
 */
@Suppress("TooManyFunctions")
class DefaultWorkspaceComponent(
    componentContext: ComponentContext,
    private val repository: WorkspaceRepository
) : WorkspaceComponent, ComponentContext by componentContext {

    private val json = Json { prettyPrint = false }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(WorkspaceState())
    override val state: StateFlow<WorkspaceState> = _state.asStateFlow()

    private var currentWorkspaceId: String? = null
    private var saveJob: Job? = null

    // ── Public API ──────────────────────────────────────────

    override fun loadWorkspace(workspaceId: String) {
        currentWorkspaceId = workspaceId
        _state.update { it.copy(loading = true, error = null) }

        scope.launch {
            repository.getLayout(workspaceId)
                .onSuccess { layout ->
                    val node = if (layout != null) {
                        deserializeLayout(layout.layoutJson)
                    } else {
                        WorkspacePreset.Default.toLayout()
                    }
                    val workspace = repository.getByUuid(workspaceId).getOrNull()
                    _state.update {
                        it.copy(
                            layout = node,
                            workspaceName = workspace?.name ?: "Default",
                            loading = false,
                            error = null
                        )
                    }
                    Napier.d("Workspace '$workspaceId' loaded")
                }
                .onFailure { e ->
                    Napier.e("Failed to load workspace '$workspaceId'", e)
                    _state.update {
                        it.copy(
                            layout = WorkspacePreset.Default.toLayout(),
                            loading = false,
                            error = AppError.Database(
                                userMessage = "Could not load workspace.",
                                debugMessage = e.message ?: "Unknown error"
                            )
                        )
                    }
                }
        }
    }

    override fun updateLayout(layout: LayoutNode) {
        _state.update { it.copy(layout = layout, error = null) }
        scheduleSave()
    }

    override fun addPane(paneType: String) {
        _state.update { current ->
            val newLeaf = LayoutNode.Leaf(paneType = paneType)
            val newLayout = addLeafToTree(current.layout, newLeaf)
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    override fun removePane(paneType: String) {
        _state.update { current ->
            val newLayout = removePaneFromTree(current.layout, paneType)
                ?: WorkspacePreset.Default.toLayout()
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    @Suppress("MagicNumber")
    override fun resizeSplit(path: List<Int>, newRatio: Float) {
        val clampedRatio = newRatio.coerceIn(0.1f, 0.9f)
        _state.update { current ->
            val newLayout = updateNodeAtPath(current.layout, path) { node ->
                if (node is LayoutNode.Split) node.copy(ratio = clampedRatio) else node
            }
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    override fun movePane(from: List<Int>, to: List<Int>) {
        // Extract the node at `from`, remove it, then insert at `to`.
        val currentLayout = _state.value.layout
        val sourceNode = nodeAtPath(currentLayout, from)
        if (sourceNode == null || sourceNode !is LayoutNode.Leaf) return

        val withoutSource = removePaneFromTree(currentLayout, sourceNode.paneType)
            ?: WorkspacePreset.Default.toLayout()

        val newLayout = insertLeafAtPath(withoutSource, to, sourceNode)
        _state.update { it.copy(layout = newLayout, error = null) }
        scheduleSave()
    }

    override fun switchTab(path: List<Int>, index: Int) {
        _state.update { current ->
            val newLayout = updateNodeAtPath(current.layout, path) { node ->
                if (node is LayoutNode.Tabs) {
                    val clamped = index.coerceIn(0, (node.children.size - 1).coerceAtLeast(0))
                    node.copy(activeIndex = clamped)
                } else {
                    node
                }
            }
            current.copy(layout = newLayout, error = null)
        }
        // Tab switch is UI-only; no save needed.
    }

    override fun applyPreset(preset: WorkspacePreset) {
        _state.update {
            it.copy(layout = preset.toLayout(), error = null)
        }
        scheduleSave()
        Napier.i("Applied preset: ${preset.displayName}")
    }

    override fun saveWorkspace() {
        saveJob?.cancel()
        persistNow()
    }

    // ── Tree Helpers ────────────────────────────────────────

    /**
     * Adds [leaf] to the right side of the existing tree via a horizontal split.
     */
    @Suppress("MagicNumber")
    private fun addLeafToTree(root: LayoutNode, leaf: LayoutNode.Leaf): LayoutNode {
        return when (root) {
            is LayoutNode.Leaf -> LayoutNode.Split(
                axis = org.biblestudio.features.workspace.domain.model.SplitAxis.Horizontal,
                ratio = 0.5f,
                first = root,
                second = leaf
            )
            is LayoutNode.Tabs -> LayoutNode.Split(
                axis = org.biblestudio.features.workspace.domain.model.SplitAxis.Horizontal,
                ratio = 0.5f,
                first = root,
                second = leaf
            )
            is LayoutNode.Split -> root.copy(
                second = addLeafToTree(root.second, leaf)
            )
        }
    }

    /**
     * Removes all leaves matching [paneType]. Returns `null` if the tree
     * collapses to nothing.
     */
    private fun removePaneFromTree(root: LayoutNode, paneType: String): LayoutNode? {
        return when (root) {
            is LayoutNode.Leaf -> if (root.paneType == paneType) null else root
            is LayoutNode.Tabs -> {
                val filtered = root.children.filter { it.paneType != paneType }
                when {
                    filtered.isEmpty() -> null
                    filtered.size == 1 -> filtered.first()
                    else -> root.copy(
                        children = filtered,
                        activeIndex = root.activeIndex.coerceAtMost(filtered.size - 1)
                    )
                }
            }
            is LayoutNode.Split -> {
                val first = removePaneFromTree(root.first, paneType)
                val second = removePaneFromTree(root.second, paneType)
                when {
                    first == null && second == null -> null
                    first == null -> second
                    second == null -> first
                    else -> root.copy(first = first, second = second)
                }
            }
        }
    }

    /** Returns the [LayoutNode] at a tree [path], or null. */
    private fun nodeAtPath(root: LayoutNode, path: List<Int>): LayoutNode? {
        if (path.isEmpty()) return root
        return when (root) {
            is LayoutNode.Split -> {
                when (path.first()) {
                    0 -> nodeAtPath(root.first, path.drop(1))
                    1 -> nodeAtPath(root.second, path.drop(1))
                    else -> null
                }
            }
            is LayoutNode.Tabs -> {
                val idx = path.first()
                if (idx in root.children.indices) {
                    nodeAtPath(root.children[idx], path.drop(1))
                } else {
                    null
                }
            }
            is LayoutNode.Leaf -> null
        }
    }

    /** Replaces the node at [path] using [transform]. */
    private fun updateNodeAtPath(root: LayoutNode, path: List<Int>, transform: (LayoutNode) -> LayoutNode): LayoutNode {
        if (path.isEmpty()) return transform(root)
        return when (root) {
            is LayoutNode.Split -> when (path.first()) {
                0 -> root.copy(first = updateNodeAtPath(root.first, path.drop(1), transform))
                1 -> root.copy(second = updateNodeAtPath(root.second, path.drop(1), transform))
                else -> root
            }
            is LayoutNode.Tabs -> {
                val idx = path.first()
                if (idx in root.children.indices) {
                    val updated = updateNodeAtPath(root.children[idx], path.drop(1), transform)
                    if (updated is LayoutNode.Leaf) {
                        root.copy(children = root.children.toMutableList().apply { set(idx, updated) })
                    } else {
                        root
                    }
                } else {
                    root
                }
            }
            is LayoutNode.Leaf -> root
        }
    }

    /** Inserts [leaf] at [path] in the tree, splitting if necessary. */
    @Suppress("MagicNumber")
    private fun insertLeafAtPath(root: LayoutNode, path: List<Int>, leaf: LayoutNode.Leaf): LayoutNode {
        if (path.isEmpty()) return addLeafToTree(root, leaf)
        return when (root) {
            is LayoutNode.Split -> when (path.first()) {
                0 -> root.copy(first = insertLeafAtPath(root.first, path.drop(1), leaf))
                1 -> root.copy(second = insertLeafAtPath(root.second, path.drop(1), leaf))
                else -> root
            }
            else -> addLeafToTree(root, leaf)
        }
    }

    // ── Persistence ─────────────────────────────────────────

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(AUTO_SAVE_DEBOUNCE_MS)
            persistNow()
        }
    }

    private fun persistNow() {
        val wsId = currentWorkspaceId ?: return
        val layoutJson = json.encodeToString(LayoutNode.serializer(), _state.value.layout)
        scope.launch {
            repository.saveLayout(
                WorkspaceLayout(
                    id = 0,
                    workspaceId = wsId,
                    layoutJson = layoutJson,
                    updatedAt = autoSaveTimestamp
                )
            ).onFailure { e ->
                Napier.e("Auto-save failed for workspace '$wsId'", e)
            }
        }
    }

    /** Placeholder timestamp — will be replaced by a proper Clock injection. */
    private val autoSaveTimestamp = "auto-save"

    private fun deserializeLayout(jsonStr: String): LayoutNode {
        return try {
            json.decodeFromString(LayoutNode.serializer(), jsonStr)
        } catch (
            @Suppress("TooGenericExceptionCaught")
            e: Exception
        ) {
            Napier.e("Failed to deserialize layout, falling back to default", e)
            WorkspacePreset.Default.toLayout()
        }
    }
}
