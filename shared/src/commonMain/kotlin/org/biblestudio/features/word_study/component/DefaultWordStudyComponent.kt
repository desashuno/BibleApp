package org.biblestudio.features.word_study.component

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
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository

/**
 * Default [WordStudyComponent] that subscribes to VerseBus [LinkEvent.StrongsSelected]
 * and loads lexicon data, occurrences, and related words.
 */
class DefaultWordStudyComponent(
    componentContext: ComponentContext,
    private val repository: WordStudyRepository,
    private val verseBus: VerseBus
) : WordStudyComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(WordStudyState())
    override val state: StateFlow<WordStudyState> = _state.asStateFlow()

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
        _state.update { it.copy(isLoading = true, error = null) }

        val entryResult = repository.lookupByStrongs(strongsNumber)
        val occResult = repository.getOccurrences(strongsNumber)
        val countResult = repository.getOccurrenceCount(strongsNumber)
        val relatedResult = repository.getRelatedWords(strongsNumber)

        if (entryResult.isFailure) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = entryResult.exceptionOrNull()?.message ?: "Failed to load entry"
                )
            }
            return
        }

        _state.update {
            it.copy(
                isLoading = false,
                entry = entryResult.getOrNull(),
                occurrences = occResult.getOrDefault(emptyList()),
                occurrenceCount = countResult.getOrDefault(0L).toInt(),
                relatedWords = relatedResult.getOrDefault(emptyList()),
                error = null
            )
        }

        Napier.d("WordStudy loaded: $strongsNumber")
    }
}
