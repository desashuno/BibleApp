package org.biblestudio.core.verse_bus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Central event bus for cross-pane verse navigation.
 *
 * The VerseBus uses a [SharedFlow] with `replay = 1` so that newly attached
 * collectors immediately receive the most recent event (i.e., the current
 * verse context). Components publish [LinkEvent] instances via [publish] and
 * observe them by collecting [events].
 *
 * Registered as a Koin singleton in `coreModule`.
 */
class VerseBus {

    private val _events = MutableSharedFlow<LinkEvent>(replay = 1)

    /** Observable stream of [LinkEvent]s broadcast across all panes. */
    val events: SharedFlow<LinkEvent> = _events.asSharedFlow()

    /** The most recently published event, or `null` if none yet. */
    val current: LinkEvent?
        get() = _events.replayCache.firstOrNull()

    /**
     * Publishes a [LinkEvent] to all current and future collectors.
     *
     * This is a non-suspending call that uses [MutableSharedFlow.tryEmit],
     * which always succeeds when `replay >= 1` and there is no buffer overflow.
     */
    fun publish(event: LinkEvent) {
        _events.tryEmit(event)
    }

    /**
     * Alias for [publish] — navigates all panes to the given event context.
     */
    fun navigate(event: LinkEvent) {
        publish(event)
    }
}
