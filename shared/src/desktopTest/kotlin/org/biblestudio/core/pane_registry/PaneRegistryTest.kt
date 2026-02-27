package org.biblestudio.core.pane_registry

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaneRegistryTest {

    @BeforeTest
    fun setUp() {
        PaneRegistry.clear()
    }

    @Test
    fun `all 22 pane types are registered after init`() {
        PaneRegistry.init()

        assertEquals(22, PaneRegistry.availableTypes.size)

        // Spot-check a few well-known types
        assertTrue("bible-reader" in PaneRegistry.availableTypes)
        assertTrue("word-study" in PaneRegistry.availableTypes)
        assertTrue("sermon-editor" in PaneRegistry.availableTypes)
        assertTrue("search" in PaneRegistry.availableTypes)
        assertTrue("audio-sync" in PaneRegistry.availableTypes)
        assertTrue("dashboard" in PaneRegistry.availableTypes)
    }

    @Test
    fun `build throws IllegalArgumentException for unknown type`() {
        PaneRegistry.init()

        assertFailsWith<IllegalArgumentException> {
            PaneRegistry.build("non-existent-pane")
        }
    }

    @Test
    fun `metadata returns correct data for registered type`() {
        PaneRegistry.init()

        val meta = PaneRegistry.metadata("bible-reader")
        assertEquals("Bible Reader", meta.displayName)
        assertEquals(PaneCategory.Text, meta.category)
    }
}
