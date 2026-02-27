package org.biblestudio.features.bible_reader.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.bible_reader.domain.repositories.TextComparisonRepository

/**
 * Default [TextComparisonComponent] that loads parallel verse texts
 * and computes word-level diffs between selected versions.
 */
class DefaultTextComparisonComponent(
    componentContext: ComponentContext,
    private val repository: TextComparisonRepository,
    private val verseBus: VerseBus
) : TextComparisonComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(TextComparisonState())
    override val state: StateFlow<TextComparisonState> = _state.asStateFlow()

    init {
        observeVerseBus()
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
        /**
         * Computes word-level diff segments between the first two selected versions.
         *
         * Uses a simple longest-common-subsequence approach on words.
         */
        @Suppress("ReturnCount")
        internal fun computeDiff(versions: Map<String, String>, selected: List<String>): List<DiffSegment> {
            if (selected.size < 2) return emptyList()
            val textA = versions[selected[0]] ?: return emptyList()
            val textB = versions[selected[1]] ?: return emptyList()
            return wordDiff(textA.split(" "), textB.split(" "))
        }

        /**
         * Simple word-level diff using LCS (Longest Common Subsequence).
         */
        internal fun wordDiff(wordsA: List<String>, wordsB: List<String>): List<DiffSegment> {
            val m = wordsA.size
            val n = wordsB.size
            // Build LCS table
            val dp = Array(m + 1) { IntArray(n + 1) }
            for (i in 1..m) {
                for (j in 1..n) {
                    dp[i][j] = if (wordsA[i - 1] == wordsB[j - 1]) {
                        dp[i - 1][j - 1] + 1
                    } else {
                        maxOf(dp[i - 1][j], dp[i][j - 1])
                    }
                }
            }

            // Backtrack to produce diff segments
            val segments = mutableListOf<DiffSegment>()
            var i = m
            var j = n
            val stack = mutableListOf<DiffSegment>()
            while (i > 0 || j > 0) {
                when {
                    i > 0 && j > 0 && wordsA[i - 1] == wordsB[j - 1] -> {
                        stack.add(DiffSegment(wordsA[i - 1], DiffType.EQUAL))
                        i--
                        j--
                    }
                    j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j]) -> {
                        stack.add(DiffSegment(wordsB[j - 1], DiffType.ADDED))
                        j--
                    }
                    else -> {
                        stack.add(DiffSegment(wordsA[i - 1], DiffType.REMOVED))
                        i--
                    }
                }
            }
            stack.reversed().forEach { segments.add(it) }
            return segments
        }
    }
}
