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
    fun `all 23 pane types are registered after init`() {
        PaneRegistry.init()

        assertEquals(23, PaneRegistry.availableTypes.size)

        // Spot-check a few well-known types
        assertTrue(PaneType.BIBLE_READER in PaneRegistry.availableTypes)
        assertTrue(PaneType.WORD_STUDY in PaneRegistry.availableTypes)
        assertTrue(PaneType.SERMON_EDITOR in PaneRegistry.availableTypes)
        assertTrue(PaneType.SEARCH in PaneRegistry.availableTypes)
        assertTrue(PaneType.AUDIO_SYNC in PaneRegistry.availableTypes)
        assertTrue(PaneType.DASHBOARD in PaneRegistry.availableTypes)
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

        val meta = PaneRegistry.metadata(PaneType.BIBLE_READER)
        assertEquals("Bible Reader", meta.displayName)
        assertEquals(PaneCategory.Text, meta.category)
    }
}
