package org.biblestudio.features.morphology_interlinear.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
 * Default [ReverseInterlinearComponent] that maps English translation tokens
 * back to original-language morphology using pre-computed alignment data.
 */
internal class DefaultReverseInterlinearComponent(
    componentContext: ComponentContext,
    private val repository: MorphologyRepository,
    private val parsingDecoder: ParsingDecoder,
    private val verseBus: VerseBus
) : ReverseInterlinearComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(ReverseInterlinearState())
    override val state: StateFlow<ReverseInterlinearState> = _state.asStateFlow()

    init {
        observeVerseBus()
    }

    override fun onTokenSelected(token: AlignedToken) {
        _state.update { it.copy(selectedToken = token) }
        token.morphWord?.let {
            verseBus.publish(LinkEvent.StrongsSelected(it.strongsNumber))
        }
    }

    override fun clearSelection() {
        _state.update { it.copy(selectedToken = null) }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    loadAlignment(event.globalVerseId.toLong())
                }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun loadAlignment(globalVerseId: Long) {
        _state.update {
            it.copy(isLoading = true, error = null, verse = globalVerseId, selectedToken = null)
        }
        scope.launch {
            try {
                val tokens = coroutineScope {
                    val alignDeferred = async {
                        repository.getAlignmentForVerse(globalVerseId)
                    }
                    val morphDeferred = async {
                        repository.getMorphWords(globalVerseId)
                    }

                    val alignments = alignDeferred.await().getOrDefault(emptyList())
                    val morphWords = morphDeferred.await().getOrDefault(emptyList())

                    // Index morph words by (globalVerseId, wordPosition) for fast lookup
                    val morphByPosition = morphWords.associateBy { it.wordPosition }

                    alignments.map { alignment ->
                        val morph = morphByPosition[alignment.originalPosition]
                        AlignedToken(
                            englishToken = alignment.englishToken,
                            morphWord = morph,
                            decodedParsing = morph?.let { parsingDecoder.decode(it.parsingCode) }
                        )
                    }
                }
                _state.update {
                    it.copy(isLoading = false, alignedTokens = tokens)
                }
                Napier.d("ReverseInterlinear loaded ${tokens.size} aligned tokens for verse $globalVerseId")
            } catch (e: RuntimeException) {
                Napier.e("Failed to load alignment", e)
                _state.update {
                    it.copy(isLoading = false, error = "Could not load alignment data.")
                }
            }
        }
    }
}
