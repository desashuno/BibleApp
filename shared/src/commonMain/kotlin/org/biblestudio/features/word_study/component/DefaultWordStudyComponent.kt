package org.biblestudio.features.word_study.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository

/**
 * Default [WordStudyComponent] that subscribes to VerseBus [LinkEvent.StrongsSelected]
 * and loads lexicon data, occurrences (paginated), and related words.
 */
internal class DefaultWordStudyComponent(
    componentContext: ComponentContext,
    private val repository: WordStudyRepository,
    private val verseBus: VerseBus,
) : WordStudyComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(WordStudyState())
    override val state: StateFlow<WordStudyState> = _state.asStateFlow()

    private var currentStrongsNumber: String = ""

    init {
        observeVerseBus()
    }

    override fun onOccurrenceSelected(globalVerseId: Int) {
        verseBus.publish(LinkEvent.VerseSelected(globalVerseId))
    }

    override fun onSearchLexicon(query: String) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.searchLexicon(query)
                .onSuccess { results ->
                    _state.update { it.copy(relatedWords = results, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("searchLexicon failed", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    override fun onLoadMoreOccurrences() {
        val current = _state.value
        if (!current.hasMoreOccurrences || current.isLoading) return

        scope.launch {
            val nextPage = current.occurrencePage + 1
            val offset = nextPage * PAGE_SIZE
            repository.getOccurrences(currentStrongsNumber, PAGE_SIZE, offset)
                .onSuccess { newOccurrences ->
                    val totalCount = _state.value.occurrenceCount.toLong()
                    _state.update { state ->
                        state.copy(
                            occurrences = state.occurrences + newOccurrences,
                            occurrencePage = nextPage,
                            hasMoreOccurrences = (nextPage + 1) * PAGE_SIZE < totalCount,
                        )
                    }
                }
                .onFailure { e ->
                    Napier.e("loadMoreOccurrences failed", e)
                }
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.StrongsSelected>()
                .collect { event ->
                    loadEntry(event.strongsNumber)
                }
        }
    }

    private suspend fun loadEntry(strongsNumber: String) {
        currentStrongsNumber = strongsNumber
        _state.update { it.copy(isLoading = true, error = null) }

        val entryResult = repository.lookupByStrongs(strongsNumber)
        val occResult = repository.getOccurrences(strongsNumber, PAGE_SIZE, 0)
        val countResult = repository.getOccurrenceCount(strongsNumber)
        val relatedResult = repository.getRelatedWords(strongsNumber)

        if (entryResult.isFailure) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = entryResult.exceptionOrNull()?.message ?: "Failed to load entry",
                )
            }
            return
        }

        val totalCount = countResult.getOrDefault(0L)
        _state.update {
            it.copy(
                isLoading = false,
                entry = entryResult.getOrNull(),
                occurrences = occResult.getOrDefault(emptyList()),
                occurrenceCount = totalCount.toInt(),
                occurrencePage = 0,
                hasMoreOccurrences = totalCount > PAGE_SIZE,
                relatedWords = relatedResult.getOrDefault(emptyList()),
                error = null,
            )
        }

        Napier.d("WordStudy loaded: $strongsNumber (${totalCount} occurrences)")
    }

    companion object {
        private const val PAGE_SIZE = 100L
    }
}
