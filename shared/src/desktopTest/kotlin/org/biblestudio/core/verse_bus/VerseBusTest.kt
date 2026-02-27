package org.biblestudio.core.verse_bus

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class VerseBusTest {

    @Test
    fun `published event is received by collector`() = runTest {
        val bus = VerseBus()
        val event = LinkEvent.VerseSelected(1001001)

        val received = mutableListOf<LinkEvent>()
        val job = launch {
            bus.events.first().also { received.add(it) }
        }

        bus.publish(event)
        job.join()

        assertEquals(1, received.size)
        assertEquals(event, received.first())
    }

    @Test
    fun `new subscriber receives last replayed event`() = runTest {
        val bus = VerseBus()
        val event = LinkEvent.StrongsSelected("H1234")

        // Publish before any subscriber
        bus.publish(event)

        // New subscriber should immediately receive the replayed event
        val received = mutableListOf<LinkEvent>()
        val job = launch {
            bus.events.first().also { received.add(it) }
        }
        job.join()

        assertEquals(event, received.first())
        assertEquals(event, bus.current)
    }

    @Test
    fun `multiple subscribers all receive events`() = runTest {
        val bus = VerseBus()
        val event = LinkEvent.PassageSelected(startVerseId = 1001001, endVerseId = 1001010)

        val received1 = mutableListOf<LinkEvent>()
        val received2 = mutableListOf<LinkEvent>()

        val job1 = launch {
            bus.events.first().also { received1.add(it) }
        }
        val job2 = launch {
            bus.events.first().also { received2.add(it) }
        }

        bus.publish(event)

        job1.join()
        job2.join()

        assertEquals(event, received1.first())
        assertEquals(event, received2.first())
    }
}
