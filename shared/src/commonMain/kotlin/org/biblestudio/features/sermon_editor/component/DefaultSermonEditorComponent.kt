package org.biblestudio.features.sermon_editor.component

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
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.sermon_editor.domain.entities.Sermon
import org.biblestudio.features.sermon_editor.domain.entities.SermonSection
import org.biblestudio.features.sermon_editor.domain.entities.SermonStatus
import org.biblestudio.features.sermon_editor.domain.repositories.SermonRepository

/**
 * Default [SermonEditorComponent] with auto-save, word-count, and section management.
 */
@Suppress("TooManyFunctions")
class DefaultSermonEditorComponent(
    componentContext: ComponentContext,
    private val repository: SermonRepository,
    private val verseBus: VerseBus
) : SermonEditorComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(SermonEditorState())
    override val state: StateFlow<SermonEditorState> = _state.asStateFlow()
    private var saveJob: Job? = null

    init {
        loadSermons()
        observeVerseBus()
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    _state.update { it.copy(currentVerseId = event.globalVerseId.toLong()) }
                }
        }
    }

    override fun onSermonSelected(uuid: String) {
        scope.launch {
            repository.getByUuid(uuid).onSuccess { sermon ->
                if (sermon != null) {
                    repository.getSections(sermon.uuid).onSuccess { sections ->
                        val sorted = sections.sortedBy { it.sortOrder }
                        val wc = computeWordCount(sorted)
                        _state.update {
                            it.copy(
                                activeSermon = sermon,
                                sections = sorted,
                                editTitle = sermon.title,
                                editScriptureRef = sermon.scriptureRef,
                                status = SermonStatus.fromString(sermon.status),
                                isDirty = false,
                                wordCount = wc,
                                estimatedMinutes = estimateMinutes(wc)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onTitleChanged(title: String) {
        _state.update { it.copy(editTitle = title, isDirty = true) }
        scheduleAutoSave()
    }

    override fun onScriptureRefChanged(ref: String) {
        _state.update { it.copy(editScriptureRef = ref, isDirty = true) }
        scheduleAutoSave()
    }

    override fun onStatusChanged(status: SermonStatus) {
        _state.update { it.copy(status = status, isDirty = true) }
        scheduleAutoSave()
    }

    override fun onNewSermon() {
        val now = Clock.System.now().toString()
        val uuid = generateUuid()
        val sermon = Sermon(
            uuid = uuid,
            title = "",
            scriptureRef = "",
            createdAt = now,
            updatedAt = now,
            status = SermonStatus.Draft.raw,
            deviceId = ""
        )
        scope.launch {
            repository.create(sermon).onSuccess {
                _state.update {
                    it.copy(
                        activeSermon = sermon,
                        sections = emptyList(),
                        editTitle = "",
                        editScriptureRef = "",
                        status = SermonStatus.Draft,
                        isDirty = false,
                        wordCount = 0,
                        estimatedMinutes = 0
                    )
                }
                loadSermons()
            }
        }
    }

    override fun onDeleteSermon(uuid: String) {
        val now = Clock.System.now().toString()
        scope.launch {
            repository.delete(uuid, now).onSuccess {
                if (_state.value.activeSermon?.uuid == uuid) {
                    _state.update {
                        it.copy(
                            activeSermon = null,
                            sections = emptyList(),
                            editTitle = "",
                            editScriptureRef = ""
                        )
                    }
                }
                loadSermons()
            }
        }
    }

    override fun onSectionContentChanged(sectionId: Long, content: String) {
        val sections = _state.value.sections.map { s ->
            if (s.id == sectionId) s.copy(content = content) else s
        }
        val wc = computeWordCount(sections)
        _state.update {
            it.copy(
                sections = sections,
                isDirty = true,
                wordCount = wc,
                estimatedMinutes = estimateMinutes(wc)
            )
        }
        scheduleAutoSave()
    }

    override fun onAddSection(type: String) {
        val sermonId = _state.value.activeSermon?.uuid ?: return
        val nextOrder = (_state.value.sections.maxOfOrNull { it.sortOrder } ?: 0) + 1
        val section = SermonSection(
            id = 0,
            sermonId = sermonId,
            type = type,
            content = "",
            sortOrder = nextOrder
        )
        scope.launch {
            repository.createSection(section).onSuccess {
                reloadSections(sermonId)
            }
        }
    }

    override fun onDeleteSection(sectionId: Long) {
        val sermonId = _state.value.activeSermon?.uuid ?: return
        scope.launch {
            repository.deleteSection(sectionId).onSuccess {
                reloadSections(sermonId)
            }
        }
    }

    override fun onMoveSectionUp(sectionId: Long) {
        reorderSection(sectionId, delta = -1)
    }

    override fun onMoveSectionDown(sectionId: Long) {
        reorderSection(sectionId, delta = 1)
    }

    @Suppress("MagicNumber")
    override fun onInsertScriptureReference(globalVerseId: Long) {
        val sections = _state.value.sections
        if (sections.isEmpty()) return
        // Decode BBCCCVVV → human-readable reference stub
        val book = (globalVerseId / 1_000_000).toInt()
        val chapter = ((globalVerseId % 1_000_000) / 1_000).toInt()
        val verse = (globalVerseId % 1_000).toInt()
        val refText = " [${book}:${chapter}:${verse}] "

        // Append to the last section's content
        val lastSection = sections.last()
        val updated = lastSection.copy(content = lastSection.content + refText)
        val updatedSections = sections.toMutableList().apply { set(lastIndex, updated) }
        val wc = computeWordCount(updatedSections)
        _state.update {
            it.copy(
                sections = updatedSections,
                isDirty = true,
                wordCount = wc,
                estimatedMinutes = estimateMinutes(wc)
            )
        }
        scheduleAutoSave()
    }

    override fun onExportMarkdown(): String {
        val s = _state.value
        val sb = StringBuilder()
        sb.appendLine("# ${s.editTitle}")
        if (s.editScriptureRef.isNotBlank()) {
            sb.appendLine("**Scripture:** ${s.editScriptureRef}")
        }
        sb.appendLine()
        for (section in s.sections) {
            sb.appendLine("## ${section.type.replaceFirstChar { it.uppercase() }}")
            sb.appendLine(section.content)
            sb.appendLine()
        }
        sb.appendLine("---")
        sb.appendLine("_Word count: ${s.wordCount} • Est. ${s.estimatedMinutes} min_")
        return sb.toString()
    }

    override fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length >= 2) {
            scope.launch {
                repository.getAll().onSuccess { all ->
                    val filtered = all.filter {
                        it.title.contains(query, ignoreCase = true) ||
                            it.scriptureRef.contains(query, ignoreCase = true)
                    }
                    _state.update { it.copy(searchResults = filtered) }
                }
            }
        } else {
            _state.update { it.copy(searchResults = emptyList()) }
        }
    }

    private fun loadSermons() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getAll()
                .onSuccess { sermons ->
                    _state.update { it.copy(sermons = sermons, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load sermons", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun reloadSections(sermonId: String) {
        scope.launch {
            repository.getSections(sermonId).onSuccess { sections ->
                val sorted = sections.sortedBy { it.sortOrder }
                val wc = computeWordCount(sorted)
                _state.update {
                    it.copy(
                        sections = sorted,
                        wordCount = wc,
                        estimatedMinutes = estimateMinutes(wc)
                    )
                }
            }
        }
    }

    private fun reorderSection(sectionId: Long, delta: Int) {
        val sermonId = _state.value.activeSermon?.uuid ?: return
        val current = _state.value.sections.toMutableList()
        val idx = current.indexOfFirst { it.id == sectionId }
        if (idx < 0) return
        val newIdx = (idx + delta).coerceIn(0, current.lastIndex)
        if (idx == newIdx) return
        val moved = current.removeAt(idx)
        current.add(newIdx, moved)
        scope.launch {
            current.forEachIndexed { i, s ->
                repository.updateSection(s.copy(sortOrder = i.toLong()))
            }
            reloadSections(sermonId)
        }
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(AUTO_SAVE_DELAY_MS)
            performSave()
        }
    }

    private suspend fun performSave() {
        val s = _state.value
        val sermon = s.activeSermon ?: return
        if (!s.isDirty) return

        _state.update { it.copy(isSaving = true) }
        val now = Clock.System.now().toString()
        val updated = sermon.copy(
            title = s.editTitle,
            scriptureRef = s.editScriptureRef,
            status = s.status.raw,
            updatedAt = now
        )
        repository.update(updated)
            .onSuccess {
                // Also persist section content changes
                s.sections.forEach { section ->
                    repository.updateSection(section)
                }
                _state.update { it.copy(activeSermon = updated, isDirty = false, isSaving = false) }
                loadSermons()
                Napier.d("Sermon auto-saved: ${updated.uuid}")
            }
            .onFailure { e ->
                Napier.e("Auto-save failed", e)
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
    }

    private fun generateUuid(): String {
        val chars = "0123456789abcdef"
        val template = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
        return template.map { c ->
            when (c) {
                'x' -> chars.random()
                'y' -> chars["89ab".random().digitToInt(16)]
                else -> c
            }
        }.joinToString("")
    }

    companion object {
        internal const val AUTO_SAVE_DELAY_MS = 2000L
        private const val WORDS_PER_MINUTE = 150

        internal fun computeWordCount(sections: List<SermonSection>): Int = sections.sumOf { section ->
            section.content.split("\\s+".toRegex()).count { it.isNotBlank() }
        }

        internal fun estimateMinutes(wordCount: Int): Int =
            if (wordCount == 0) 0 else (wordCount + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE
    }
}
