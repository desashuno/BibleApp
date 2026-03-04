package org.biblestudio.features.bible_reader.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.entities.VersionVerse
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.features.bible_reader.domain.repositories.TextComparisonRepository

/**
 * Default [TextComparisonComponent] that loads parallel verse texts
 * and computes word-level diffs between selected versions.
 */
internal class DefaultTextComparisonComponent(
    componentContext: ComponentContext,
    private val repository: TextComparisonRepository,
    private val bibleRepository: BibleRepository,
    private val verseBus: VerseBus
) : TextComparisonComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(TextComparisonState())
    override val state: StateFlow<TextComparisonState> = _state.asStateFlow()

    init {
        loadAvailableBibles()
        observeVerseBus()
    }

    private fun loadAvailableBibles() {
        scope.launch {
            bibleRepository.getActiveBibles()
                .recoverCatching { bibleRepository.getAvailableBibles().getOrThrow() }
                .onSuccess { bibles ->
                    _state.update { it.copy(availableBibles = bibles) }
                }
        }
    }

    override fun loadComparison(globalVerseId: Long) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.getVersesForComparison(globalVerseId)
                .onSuccess { comparison ->
                    val versions = _state.value.selectedVersions.ifEmpty {
                        comparison.versions.keys.toList()
                    }
                    val diffs = computeDiff(comparison.versions, versions)
                    _state.update {
                        it.copy(
                            comparison = comparison,
                            selectedVersions = versions,
                            diffHighlights = diffs,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    Napier.e("Failed to load comparison", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load comparison.")
                    }
                }
        }
    }

    override fun setViewMode(mode: ComparisonViewMode) {
        _state.update { it.copy(viewMode = mode) }
    }

    override fun setSelectedVersions(versions: List<String>) {
        _state.update { it.copy(selectedVersions = versions) }
        _state.value.comparison?.let { comparison ->
            val diffs = computeDiff(comparison.versions, versions)
            _state.update { it.copy(diffHighlights = diffs) }
        }
    }

    override fun nextVerse() {
        val currentId = _state.value.comparison?.globalVerseId ?: return
        scope.launch {
            bibleRepository.getNextVerseId(currentId)
                .onSuccess { nextId -> if (nextId != null) loadComparison(nextId) }
        }
    }

    override fun previousVerse() {
        val currentId = _state.value.comparison?.globalVerseId ?: return
        scope.launch {
            bibleRepository.getPreviousVerseId(currentId)
                .onSuccess { prevId -> if (prevId != null) loadComparison(prevId) }
        }
    }

    override fun copyComparisonText(): String {
        val s = _state.value
        val comparison = s.comparison ?: return ""
        val ref = org.biblestudio.core.util.VerseRefFormatter.format(comparison.globalVerseId)
        return buildString {
            appendLine(ref)
            appendLine()
            val versions = if (s.selectedVersions.isEmpty()) {
                comparison.versions
            } else {
                comparison.versions.filterKeys { it in s.selectedVersions }
            }
            for ((abbr, payload) in versions) {
                appendLine("[$abbr] ${payload.text}")
            }
        }.trim()
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events.collect { event ->
                if (event is LinkEvent.VerseSelected) {
                    loadComparison(event.globalVerseId.toLong())
                }
            }
        }
    }

    companion object {
        @Suppress("ReturnCount")
        internal fun computeDiff(versions: Map<String, VersionVerse>, selected: List<String>): List<DiffSegment> {
            if (selected.size < 2) return emptyList()
            val textA = versions[selected[0]]?.text ?: return emptyList()
            val textB = versions[selected[1]]?.text ?: return emptyList()
            return wordDiff(textA.split(" "), textB.split(" "))
        }
    }
}
