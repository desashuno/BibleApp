package org.biblestudio.features.cross_references.component

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
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository

/**
 * Default [CrossReferenceComponent] that subscribes to VerseBus and
 * loads cross-references for the currently selected verse.
 */
class DefaultCrossReferenceComponent(
    componentContext: ComponentContext,
    private val repository: CrossRefRepository,
    private val verseBus: VerseBus,
    private val bibleRepository: BibleRepository? = null
) : CrossReferenceComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(CrossReferenceState())
    override val state: StateFlow<CrossReferenceState> = _state.asStateFlow()

    init {
        observeVerseBus()
    }

    override fun loadReferences(globalVerseId: Long) {
        _state.update { it.copy(sourceVerseId = globalVerseId, isLoading = true, error = null) }
        scope.launch {
            repository.getAllForVerse(globalVerseId)
                .onSuccess { refs ->
                    _state.update {
                        it.copy(references = refs, isLoading = false)
                    }
                    Napier.d("Loaded ${refs.size} cross-references for verse $globalVerseId")
                }
                .onFailure { e ->
                    Napier.e("Failed to load cross-references", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load cross-references.")
                    }
                }
        }
    }

    override fun onReferenceTapped(reference: CrossReference) {
        verseBus.publish(LinkEvent.VerseSelected(reference.targetVerseId.toInt()))
    }

    override fun toggleExpansion(reference: CrossReference) {
        val current = _state.value.expandedVerseTexts
        if (current.containsKey(reference.targetVerseId)) {
            // Collapse: remove from map
            _state.update { it.copy(expandedVerseTexts = current - reference.targetVerseId) }
        } else {
            // Expand: load verse text
            scope.launch {
                bibleRepository?.getVerseByGlobalId(reference.targetVerseId)
                    ?.onSuccess { verse ->
                        val text = verse?.text ?: "Verse text not available"
                        _state.update {
                            it.copy(expandedVerseTexts = it.expandedVerseTexts + (reference.targetVerseId to text))
                        }
                    }
                    ?: run {
                        _state.update {
                            it.copy(
                                expandedVerseTexts = it.expandedVerseTexts +
                                    (reference.targetVerseId to "Verse text not available")
                            )
                        }
                    }
            }
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events.collect { event ->
                if (event is LinkEvent.VerseSelected) {
                    loadReferences(event.globalVerseId.toLong())
                }
            }
        }
    }
}
