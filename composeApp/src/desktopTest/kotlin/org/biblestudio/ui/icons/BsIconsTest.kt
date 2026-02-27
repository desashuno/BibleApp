package org.biblestudio.ui.icons

import kotlin.test.Test
import kotlin.test.assertEquals

class BsIconsTest {

    @Test
    fun allCustomIconsAreConstructed() {
        val icons = listOf(
            BsIcons.Interlinear,
            BsIcons.Strongs,
            BsIcons.CrossRef,
            BsIcons.Parallel,
            BsIcons.Hebrew,
            BsIcons.Greek
        )

        assertEquals(6, icons.size, "Should have 6 custom Bible study icons")
    }

    @Test
    fun iconsHaveCorrectNames() {
        assertEquals("Interlinear", BsIcons.Interlinear.name)
        assertEquals("Strongs", BsIcons.Strongs.name)
        assertEquals("CrossRef", BsIcons.CrossRef.name)
        assertEquals("Parallel", BsIcons.Parallel.name)
        assertEquals("Hebrew", BsIcons.Hebrew.name)
        assertEquals("Greek", BsIcons.Greek.name)
    }
}
