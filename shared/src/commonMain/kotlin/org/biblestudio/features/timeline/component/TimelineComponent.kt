package org.biblestudio.features.timeline.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.timeline.domain.entities.TimelineCategory
import org.biblestudio.features.timeline.domain.entities.TimelineEvent

/**
 * Observable state for the Timeline pane.
 */
data class TimelineState(
    val events: List<TimelineEvent> = emptyList(),
    val visibleStartYear: Int = -4000,
    val visibleEndYear: Int = 100,
    val selectedEvent: TimelineEvent? = null,
    val zoomLevel: TimelineZoom = TimelineZoom.Century,
    val activeCategory: TimelineCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Zoom level for the timeline time axis.
 */
enum class TimelineZoom(val yearsPerScreen: Int) {
    Millennium(2000),
    Century(500),
    Decade(100),
    Year(20)
}

/**
 * Business-logic boundary for the Timeline pane.
 *
 * Subscribes to [LinkEvent.VerseSelected] to load events for the active
 * verse and provides timeline exploration operations.
 */
interface TimelineComponent {

    /** The current timeline state observable. */
    val state: StateFlow<TimelineState>

    /** Select an event and show its detail card. */
    fun onEventSelected(event: TimelineEvent)

    /** Change the zoom level. */
    fun onZoomChanged(zoom: TimelineZoom)

    /** Scroll the visible range to center on a year. */
    fun onScrollToYear(year: Int)

    /** Filter events by category, or null to show all. */
    fun onCategoryFilter(category: TimelineCategory?)

    /** Clear the selected event. */
    fun onClearSelection()
}
