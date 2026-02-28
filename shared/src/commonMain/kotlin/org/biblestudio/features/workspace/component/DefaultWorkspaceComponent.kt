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
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.biblestudio.core.error.AppError
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.workspace.domain.entities.WorkspaceLayout
import org.biblestudio.features.workspace.domain.model.LayoutNode
import org.biblestudio.features.workspace.domain.model.PanePlacement
import org.biblestudio.features.workspace.domain.model.SplitAxis
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
    private val repository: WorkspaceRepository,
    private val verseBus: VerseBus
) : WorkspaceComponent, ComponentContext by componentContext {

    private val json = Json { prettyPrint = false }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(WorkspaceState())
    override val state: StateFlow<WorkspaceState> = _state.asStateFlow()

    private var currentWorkspaceId: String? = null
    private var saveJob: Job? = null

    init {
        // Subscribe to VerseBus: focus the Bible Reader pane when a verse is selected
        scope.launch {
            verseBus.events.collect { event ->
                when (event) {
                    is LinkEvent.VerseSelected,
                    is LinkEvent.PassageSelected,
                    is LinkEvent.SearchResult -> focusPaneByType("bible-reader")
                    else -> { /* No tab focus needed for Strongs/Resource events */ }
                }
            }
        }
    }

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
            val existing = current.layout
            val newLayout = if (existing != null) addLeafToTree(existing, newLeaf) else newLeaf
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    override fun removePane(paneType: String) {
        _state.update { current ->
            val layout = current.layout ?: return@update current
            val newLayout = removePaneFromTree(layout, paneType)
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    override fun removePaneAtPath(path: List<Int>) {
        _state.update { current ->
            val layout = current.layout ?: return@update current
            val newLayout = removeNodeAtPath(layout, path)
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    @Suppress("MagicNumber")
    override fun resizeSplit(path: List<Int>, newRatio: Float) {
        val clampedRatio = newRatio.coerceIn(0.1f, 0.9f)
        _state.update { current ->
            val layout = current.layout ?: return@update current
            val newLayout = updateNodeAtPath(layout, path) { node ->
                if (node is LayoutNode.Split) node.copy(ratio = clampedRatio) else node
            }
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    override fun movePane(from: List<Int>, to: List<Int>) {
        // Extract the node at `from`, remove it, then insert at `to`.
        val currentLayout = _state.value.layout ?: return
        val sourceNode = nodeAtPath(currentLayout, from)
        if (sourceNode == null || sourceNode !is LayoutNode.Leaf) return

        val withoutSource = removePaneFromTree(currentLayout, sourceNode.paneType)
        if (withoutSource == null) {
            Napier.w("movePane: removing source collapsed the entire tree, aborting move")
            _state.update {
                it.copy(
                    error = AppError.Validation(
                        userMessage = "Cannot move: this is the only pane in the workspace.",
                        debugMessage = "removePaneFromTree returned null during movePane"
                    )
                )
            }
            return
        }

        val newLayout = insertLeafAtPath(withoutSource, to, sourceNode)
        _state.update { it.copy(layout = newLayout, error = null) }
        scheduleSave()
    }

    override fun switchTab(path: List<Int>, index: Int) {
        _state.update { current ->
            val layout = current.layout ?: return@update current
            val newLayout = updateNodeAtPath(layout, path) { node ->
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

    override fun reorderTab(path: List<Int>, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        _state.update { current ->
            val layout = current.layout ?: return@update current
            val newLayout = updateNodeAtPath(layout, path) { node ->
                if (node is LayoutNode.Tabs) {
                    val validFrom = fromIndex.coerceIn(0, node.children.lastIndex)
                    val validTo = toIndex.coerceIn(0, node.children.lastIndex)
                    if (validFrom == validTo) return@updateNodeAtPath node
                    val children = node.children.toMutableList()
                    val moved = children.removeAt(validFrom)
                    children.add(validTo, moved)
                    val newActive = when (node.activeIndex) {
                        validFrom -> validTo
                        in minOf(validFrom, validTo)..maxOf(validFrom, validTo) -> {
                            if (validFrom < validTo) node.activeIndex - 1 else node.activeIndex + 1
                        }
                        else -> node.activeIndex
                    }
                    node.copy(
                        children = children,
                        activeIndex = newActive.coerceIn(0, children.lastIndex)
                    )
                } else {
                    node
                }
            }
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    @Suppress("MagicNumber")
    override fun rearrangePane(fromPath: List<Int>, targetPath: List<Int>, placement: PanePlacement) {
        if (fromPath == targetPath) return

        val currentLayout = _state.value.layout ?: return
        val sourceNode = nodeAtPath(currentLayout, fromPath)
        if (sourceNode == null || sourceNode !is LayoutNode.Leaf) {
            Napier.w("rearrangePane: source at $fromPath is not a Leaf")
            return
        }

        // When the target is a Leaf inside a Tabs and placement is directional
        // (not TAB), redirect to the Tabs node itself so wrapWithPlacement
        // creates a Split around the whole Tabs group instead of failing.
        val tabsPath = detectTabsAncestor(currentLayout, targetPath, placement)
        val effectiveTargetPath = tabsPath ?: targetPath

        // Step 1: Wrap the target node with a copy of the source according to placement
        val targetNode = nodeAtPath(currentLayout, effectiveTargetPath) ?: return
        val wrapped = wrapWithPlacement(targetNode, sourceNode, placement)
        val treeWithWrapped = updateNodeAtPath(currentLayout, effectiveTargetPath) { wrapped }

        // Step 2: Calculate the adjusted source path after wrapping
        val adjustedSourcePath = adjustSourcePathAfterWrap(fromPath, effectiveTargetPath, placement)

        // Step 3: Remove the original source from the wrapped tree
        val result = removeNodeAtPath(treeWithWrapped, adjustedSourcePath)

        if (result != null) {
            _state.update { it.copy(layout = result, error = null) }
            scheduleSave()
        } else {
            Napier.w("rearrangePane: tree collapsed to null after rearrangement")
        }
    }

    override fun applyPreset(preset: WorkspacePreset) {
        _state.update {
            it.copy(layout = preset.toLayout(), error = null)
        }
        scheduleSave()
        Napier.i("Applied preset: ${preset.displayName}")
    }

    @Suppress("MagicNumber")
    override fun splitPane(existingPaneType: String, newPaneType: String, axis: SplitAxis) {
        _state.update { current ->
            val layout = current.layout ?: return@update current
            val newLeaf = LayoutNode.Leaf(paneType = newPaneType)
            val newLayout = splitNodeByType(layout, existingPaneType, newLeaf, axis)
            current.copy(layout = newLayout, error = null)
        }
        scheduleSave()
    }

    override fun saveWorkspace() {
        saveJob?.cancel()
        persistNow()
    }

    override fun loadActiveWorkspace(onComplete: (() -> Unit)?) {
        scope.launch {
            repository.getActive()
                .onSuccess { active ->
                    if (active != null) {
                        loadWorkspace(active.uuid)
                    } else {
                        // No active workspace — create a default one
                        val uuid = generateUuid()
                        val now = Clock.System.now().toString()
                        val workspace = org.biblestudio.features.workspace.domain.entities.Workspace(
                            uuid = uuid,
                            name = "Default",
                            isActive = true,
                            createdAt = now,
                            updatedAt = now,
                            deviceId = "local"
                        )
                        repository.create(workspace)
                        repository.setActive(uuid, now, "local")
                        loadWorkspace(uuid)
                    }
                    onComplete?.invoke()
                }
                .onFailure { e ->
                    Napier.e("Failed to load active workspace", e)
                    // Fallback: show default layout without persistence
                    _state.update {
                        it.copy(
                            layout = WorkspacePreset.Default.toLayout(),
                            workspaceName = "Default",
                            loading = false
                        )
                    }
                    onComplete?.invoke()
                }
        }
    }

    override fun createWorkspace(name: String, onComplete: (() -> Unit)?) {
        scope.launch {
            // Save current workspace layout first
            persistNow()

            val uuid = generateUuid()
            val now = Clock.System.now().toString()
            val workspace = org.biblestudio.features.workspace.domain.entities.Workspace(
                uuid = uuid,
                name = name,
                isActive = false,
                createdAt = now,
                updatedAt = now,
                deviceId = "local"
            )
            repository.create(workspace)
                .onSuccess {
                    repository.setActive(uuid, now, "local")
                    loadWorkspace(uuid)
                }
                .onFailure { e ->
                    Napier.e("Failed to create workspace '$name'", e)
                }
            onComplete?.invoke()
        }
    }

    override fun deleteWorkspace(id: String, onComplete: (() -> Unit)?) {
        scope.launch {
            val now = Clock.System.now().toString()
            repository.delete(id, now)
                .onSuccess {
                    if (currentWorkspaceId == id) {
                        loadActiveWorkspace()
                    }
                }
                .onFailure { e ->
                    Napier.e("Failed to delete workspace '$id'", e)
                }
            onComplete?.invoke()
        }
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

    /**
     * Removes the node at [path], collapsing parent splits/tabs as needed.
     * Returns `null` if the entire tree collapses.
     */
    private fun removeNodeAtPath(root: LayoutNode, path: List<Int>): LayoutNode? {
        if (path.isEmpty()) return null // Remove this node
        return when (root) {
            is LayoutNode.Split -> when (path.first()) {
                0 -> {
                    val newFirst = removeNodeAtPath(root.first, path.drop(1))
                    if (newFirst == null) root.second else root.copy(first = newFirst)
                }
                1 -> {
                    val newSecond = removeNodeAtPath(root.second, path.drop(1))
                    if (newSecond == null) root.first else root.copy(second = newSecond)
                }
                else -> root
            }
            is LayoutNode.Tabs -> {
                val idx = path.first()
                if (idx in root.children.indices && path.size == 1) {
                    val filtered = root.children.toMutableList().apply { removeAt(idx) }
                    when {
                        filtered.isEmpty() -> null
                        filtered.size == 1 -> filtered.first()
                        else -> root.copy(
                            children = filtered,
                            activeIndex = root.activeIndex.coerceAtMost(filtered.size - 1)
                        )
                    }
                } else {
                    root
                }
            }
            is LayoutNode.Leaf -> root
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

    /**
     * Finds the first [Leaf] or [Tabs] node matching [targetType] and wraps
     * it in a [Split] with [newLeaf] on the given [axis].
     */
    @Suppress("MagicNumber")
    private fun splitNodeByType(
        node: LayoutNode,
        targetType: String,
        newLeaf: LayoutNode.Leaf,
        axis: SplitAxis
    ): LayoutNode {
        return when (node) {
            is LayoutNode.Leaf -> if (node.paneType == targetType) {
                LayoutNode.Split(axis = axis, ratio = 0.5f, first = node, second = newLeaf)
            } else {
                node
            }
            is LayoutNode.Split -> {
                // Try first child first; only split the first match found
                val firstAttempt = splitNodeByType(node.first, targetType, newLeaf, axis)
                if (firstAttempt !== node.first) {
                    node.copy(first = firstAttempt)
                } else {
                    node.copy(second = splitNodeByType(node.second, targetType, newLeaf, axis))
                }
            }
            is LayoutNode.Tabs -> {
                if (node.children.any { it.paneType == targetType }) {
                    LayoutNode.Split(axis = axis, ratio = 0.5f, first = node, second = newLeaf)
                } else {
                    node
                }
            }
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

    // ── Drag-Drop Helpers ────────────────────────────────────

    @Suppress("MagicNumber")
    private fun wrapWithPlacement(
        target: LayoutNode,
        source: LayoutNode.Leaf,
        placement: PanePlacement
    ): LayoutNode = when (placement) {
        PanePlacement.LEFT -> LayoutNode.Split(SplitAxis.Horizontal, 0.5f, source, target)
        PanePlacement.RIGHT -> LayoutNode.Split(SplitAxis.Horizontal, 0.5f, target, source)
        PanePlacement.ABOVE -> LayoutNode.Split(SplitAxis.Vertical, 0.5f, source, target)
        PanePlacement.BELOW -> LayoutNode.Split(SplitAxis.Vertical, 0.5f, target, source)
        PanePlacement.TAB -> when (target) {
            is LayoutNode.Leaf -> LayoutNode.Tabs(listOf(target, source), activeIndex = 1)
            is LayoutNode.Tabs -> target.copy(
                children = target.children + source,
                activeIndex = target.children.size
            )
            else -> LayoutNode.Split(SplitAxis.Horizontal, 0.5f, target, source)
        }
    }

    private fun adjustSourcePathAfterWrap(
        fromPath: List<Int>,
        targetPath: List<Int>,
        placement: PanePlacement
    ): List<Int> {
        // Check if source is inside the target (targetPath is a prefix of fromPath)
        if (targetPath.size <= fromPath.size &&
            fromPath.subList(0, targetPath.size) == targetPath
        ) {
            return when (placement) {
                PanePlacement.TAB -> fromPath // Tabs preserve inner structure
                PanePlacement.LEFT, PanePlacement.ABOVE -> {
                    // Original target is at index 1 in the new Split
                    targetPath + 1 + fromPath.subList(targetPath.size, fromPath.size)
                }
                PanePlacement.RIGHT, PanePlacement.BELOW -> {
                    // Original target is at index 0 in the new Split
                    targetPath + 0 + fromPath.subList(targetPath.size, fromPath.size)
                }
            }
        }
        return fromPath
    }

    /**
     * If [targetPath] points to a Leaf inside a Tabs node, returns the path
     * to the parent Tabs node so the operation is applied to the whole group.
     *
     * - Directional placements (LEFT/RIGHT/ABOVE/BELOW): wraps the Tabs in a Split.
     * - TAB placement: appends the source as a new tab in the existing group
     *   (avoids creating a nested Tabs that `updateNodeAtPath` would discard).
     *
     * Returns `null` when no redirect is needed (parent is not Tabs).
     */
    private fun detectTabsAncestor(
        root: LayoutNode,
        targetPath: List<Int>,
        @Suppress("UNUSED_PARAMETER") placement: PanePlacement
    ): List<Int>? {
        if (targetPath.isEmpty()) return null
        val parentPath = targetPath.dropLast(1)
        val parentNode = nodeAtPath(root, parentPath)
        return if (parentNode is LayoutNode.Tabs) parentPath else null
    }

    // ── Cross-pane Focus ─────────────────────────────────────

    /**
     * Activates all tabs along the path to a leaf with the given [paneType].
     * Does nothing if no such leaf exists in the current layout.
     */
    private fun focusPaneByType(paneType: String) {
        _state.update { current ->
            val layout = current.layout ?: return@update current
            val focused = activateLeafInTree(layout, paneType)
            if (focused != null) current.copy(layout = focused) else current
        }
    }

    /**
     * Recursively searches for a [Leaf] with [paneType] and activates
     * any [Tabs] node along the path. Returns the updated subtree, or `null`
     * if no matching leaf was found.
     */
    private fun activateLeafInTree(node: LayoutNode, paneType: String): LayoutNode? {
        return when (node) {
            is LayoutNode.Leaf -> if (node.paneType == paneType) node else null
            is LayoutNode.Split -> {
                val first = activateLeafInTree(node.first, paneType)
                if (first != null) return node.copy(first = first)
                val second = activateLeafInTree(node.second, paneType)
                if (second != null) return node.copy(second = second)
                null
            }
            is LayoutNode.Tabs -> {
                for ((index, child) in node.children.withIndex()) {
                    if (child.paneType == paneType) {
                        return node.copy(activeIndex = index)
                    }
                }
                null
            }
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
        val layout = _state.value.layout ?: return
        val layoutJson = json.encodeToString(LayoutNode.serializer(), layout)
        scope.launch {
            repository.saveLayout(
                WorkspaceLayout(
                    id = 0,
                    workspaceId = wsId,
                    layoutJson = layoutJson,
                    updatedAt = Clock.System.now().toString()
                )
            ).onFailure { e ->
                Napier.e("Auto-save failed for workspace '$wsId'", e)
            }
        }
    }

    private fun generateUuid(): String {
        val chars = "abcdef0123456789"
        val segments = listOf(UUID_SEGMENT_8, UUID_SEGMENT_4, UUID_SEGMENT_4, UUID_SEGMENT_4, UUID_SEGMENT_12)
        return segments.joinToString("-") { len ->
            (1..len).map { chars.random() }.joinToString("")
        }
    }

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

    companion object {
        private const val UUID_SEGMENT_8 = 8
        private const val UUID_SEGMENT_4 = 4
        private const val UUID_SEGMENT_12 = 12
    }
}
