package org.biblestudio.features.morphology_interlinear.component

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
import org.biblestudio.features.morphology_interlinear.domain.ParsingDecoder
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository

/**
 * Default [InterlinearComponent] that subscribes to VerseBus
 * [LinkEvent.VerseSelected] and loads morphological word data.
 */
internal class DefaultInterlinearComponent(
    componentContext: ComponentContext,
    private val repository: MorphologyRepository,
    private val parsingDecoder: ParsingDecoder,
    private val verseBus: VerseBus
) : InterlinearComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(InterlinearState())
    override val state: StateFlow<InterlinearState> = _state.asStateFlow()

    init {
        observeVerseBus()
    }

    override fun onWordSelected(word: org.biblestudio.core.study.MorphWord) {
        verseBus.publish(LinkEvent.StrongsSelected(word.strongsNumber))
    }

    override fun onDisplayModeChanged(mode: InterlinearDisplayMode) {
        _state.update { it.copy(displayMode = mode) }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    loadMorphology(event.globalVerseId.toLong())
                }
        }
    }

    private fun loadMorphology(globalVerseId: Long) {
        _state.update { it.copy(isLoading = true, error = null, verse = globalVerseId) }
        scope.launch {
            repository.getMorphWords(globalVerseId)
                .onSuccess { words ->
                    val parsings = words
                        .map { it.parsingCode }
                        .distinct()
                        .associateWith { parsingDecoder.decode(it) }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            words = words,
                            decodedParsings = parsings
                        )
                    }
                    Napier.d("Interlinear loaded ${words.size} words for verse $globalVerseId")
                }
                .onFailure { e ->
                    Napier.e("Failed to load morphology", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load morphology data.")
                    }
                }
        }
    }
}
