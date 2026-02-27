package org.biblestudio.features.highlights.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.highlights.domain.entities.Highlight
import org.biblestudio.features.highlights.domain.entities.HighlightColor
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository

/**
 * Default [HighlightComponent] managing highlight CRUD and colour selection.
 */
class DefaultHighlightComponent(
    componentContext: ComponentContext,
    private val repository: HighlightRepository,
    private val verseBus: VerseBus
) : HighlightComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(HighlightState())
    override val state: StateFlow<HighlightState> = _state.asStateFlow()

    private var currentVerseId: Long? = null

    init {
        observeVerseBus()
    }

    override fun onColorSelected(color: HighlightColor) {
        _state.update { it.copy(selectedColor = color) }
    }

    override fun onHighlightVerse(globalVerseId: Long, startOffset: Long, endOffset: Long) {
        val now = Clock.System.now().toString()
        val uuid = generateUuid()
        val highlight = Highlight(
            uuid = uuid,
            globalVerseId = globalVerseId,
            colorIndex = _state.value.selectedColor.index,
            style = "background",
            startOffset = startOffset,
            endOffset = endOffset,
            createdAt = now,
            updatedAt = now,
            deviceId = ""
        )
        scope.launch {
            repository.create(highlight)
                .onSuccess { loadHighlightsForVerse(globalVerseId) }
                .onFailure { e -> Napier.e("Failed to create highlight", e) }
        }
    }

    override fun onDeleteHighlight(uuid: String) {
        val now = Clock.System.now().toString()
        scope.launch {
            repository.delete(uuid, now)
                .onSuccess { currentVerseId?.let { loadHighlightsForVerse(it) } }
                .onFailure { e -> Napier.e("Failed to delete highlight", e) }
        }
    }

    override fun onChangeColor(uuid: String, color: HighlightColor) {
        val existing = _state.value.highlights.firstOrNull { it.uuid == uuid } ?: return
        val now = Clock.System.now().toString()
        val updated = existing.copy(colorIndex = color.index, updatedAt = now)
        scope.launch {
            repository.update(updated)
                .onSuccess { currentVerseId?.let { loadHighlightsForVerse(it) } }
                .onFailure { e -> Napier.e("Failed to update highlight color", e) }
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    currentVerseId = event.globalVerseId.toLong()
                    loadHighlightsForVerse(event.globalVerseId.toLong())
                }
        }
    }

    private fun loadHighlightsForVerse(verseId: Long) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getHighlightsForVerse(verseId)
                .onSuccess { highlights ->
                    _state.update { it.copy(highlights = highlights, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load highlights", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
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
}
