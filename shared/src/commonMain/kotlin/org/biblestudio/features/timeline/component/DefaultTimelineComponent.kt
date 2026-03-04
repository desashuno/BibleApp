package org.biblestudio.features.timeline.component

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
import org.biblestudio.features.timeline.domain.entities.TimelineCategory
import org.biblestudio.features.timeline.domain.entities.TimelineEvent
import org.biblestudio.features.timeline.domain.repositories.TimelineRepository

/**
 * Default [TimelineComponent] managing event display and navigation.
 */
internal class DefaultTimelineComponent(
    componentContext: ComponentContext,
    private val repository: TimelineRepository,
    private val verseBus: VerseBus
) : TimelineComponent, ComponentContext by componentContext {

    private val scope = componentScope()

    private val _state = MutableStateFlow(TimelineState())
    override val state: StateFlow<TimelineState> = _state.asStateFlow()

    init {
        loadAllEvents()
        observeVerseBus()
    }

    override fun onEventSelected(event: TimelineEvent) {
        _state.update { it.copy(selectedEvent = event) }
        // Publish first verse reference if available
        event.verseIds.firstOrNull()?.let { verseId ->
            verseBus.publish(LinkEvent.VerseSelected(verseId.toInt()))
        }
    }

    override fun onZoomChanged(zoom: TimelineZoom) {
        _state.update { state ->
            val center = (state.visibleStartYear + state.visibleEndYear) / 2
            val halfRange = zoom.yearsPerScreen / 2
            state.copy(
                zoomLevel = zoom,
                visibleStartYear = center - halfRange,
                visibleEndYear = center + halfRange
            )
        }
        loadVisibleEvents()
    }

    override fun onScrollToYear(year: Int) {
        val halfRange = _state.value.zoomLevel.yearsPerScreen / 2
        _state.update {
            it.copy(
                visibleStartYear = year - halfRange,
                visibleEndYear = year + halfRange
            )
        }
        loadVisibleEvents()
    }

    override fun onCategoryFilter(category: TimelineCategory?) {
        _state.update { it.copy(activeCategory = category) }
        if (category != null) {
            scope.launch {
                _state.update { it.copy(isLoading = true, error = null) }
                repository.getEventsByCategory(category.name)
                    .onSuccess { events ->
                        _state.update { it.copy(events = events, isLoading = false) }
                    }
                    .onFailure { e ->
                        Napier.e("Failed to filter events by category", e)
                        _state.update { it.copy(error = e.message, isLoading = false) }
                    }
            }
        } else {
            loadVisibleEvents()
        }
    }

    override fun onClearSelection() {
        _state.update { it.copy(selectedEvent = null) }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    loadEventsForVerse(event.globalVerseId.toLong())
                }
        }
    }

    private fun loadAllEvents() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val s = _state.value
            repository.getEvents(s.visibleStartYear, s.visibleEndYear)
                .onSuccess { events ->
                    _state.update { it.copy(events = events, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load timeline events", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadVisibleEvents() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val s = _state.value
            repository.getEvents(s.visibleStartYear, s.visibleEndYear)
                .onSuccess { events ->
                    _state.update { it.copy(events = events, isLoading = false) }
                }
                .onFailure { e ->
                    Napier.e("Failed to load visible events", e)
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadEventsForVerse(globalVerseId: Long) {
        scope.launch {
            repository.getEventsForVerse(globalVerseId)
                .onSuccess { events ->
                    if (events.isNotEmpty()) {
                        val first = events.first()
                        _state.update {
                            it.copy(events = events, selectedEvent = first)
                        }
                        onScrollToYear(first.startYear)
                    }
                }
                .onFailure { e ->
                    Napier.e("Failed to load events for verse", e)
                }
        }
    }
}
