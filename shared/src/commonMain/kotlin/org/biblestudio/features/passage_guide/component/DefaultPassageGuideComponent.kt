package org.biblestudio.features.passage_guide.component

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
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.passage_guide.domain.repositories.PassageGuideRepository

/**
 * Default [PassageGuideComponent] that subscribes to VerseBus
 * [LinkEvent.VerseSelected] / [LinkEvent.PassageSelected] and builds
 * an aggregated passage report from multiple repositories.
 */
class DefaultPassageGuideComponent(
    componentContext: ComponentContext,
    private val repository: PassageGuideRepository,
    private val verseBus: VerseBus
) : PassageGuideComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(PassageGuideState())
    override val state: StateFlow<PassageGuideState> = _state.asStateFlow()

    init {
        observeVerseBus()
    }

    override fun onRefSelected(crossRef: CrossReference) {
        verseBus.publish(LinkEvent.VerseSelected(crossRef.targetVerseId.toInt()))
    }

    override fun onWordSelected(strongsNumber: String) {
        verseBus.publish(LinkEvent.StrongsSelected(strongsNumber))
    }

    override fun onSectionToggle(sectionId: String) {
        _state.update { current ->
            val sections = current.expandedSections.toMutableSet()
            if (sections.contains(sectionId)) {
                sections.remove(sectionId)
            } else {
                sections.add(sectionId)
            }
            current.copy(expandedSections = sections)
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events.collect { event ->
                when (event) {
                    is LinkEvent.VerseSelected -> loadReport(event.globalVerseId.toLong())
                    is LinkEvent.PassageSelected -> loadReport(event.startVerseId.toLong())
                    else -> { /* ignore other events */ }
                }
            }
        }
    }

    private fun loadReport(globalVerseId: Long) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            repository.buildReport(globalVerseId)
                .onSuccess { report ->
                    _state.update {
                        it.copy(isLoading = false, report = report)
                    }
                    Napier.d("PassageGuide loaded report for verse $globalVerseId")
                }
                .onFailure { e ->
                    Napier.e("Failed to build passage report", e)
                    _state.update {
                        it.copy(isLoading = false, error = "Could not load passage guide.")
                    }
                }
        }
    }
}
