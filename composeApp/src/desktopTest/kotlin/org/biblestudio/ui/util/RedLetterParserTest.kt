package org.biblestudio.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedLetterParserTest {

    @Test
    fun `null htmlText returns empty list`() {
        val ranges = extractRedLetterRanges(null, "Hello world")
        assertTrue(ranges.isEmpty())
    }

    @Test
    fun `htmlText without wj returns empty list`() {
        val ranges = extractRedLetterRanges("Hello world", "Hello world")
        assertTrue(ranges.isEmpty())
    }

    @Test
    fun `single wj block returns correct range`() {
        val plain = "Jesus said this"
        val html = "<wj>Jesus said</wj> this"

        val ranges = extractRedLetterRanges(html, plain)

        assertEquals(listOf(0..9), ranges)
    }

    @Test
    fun `multiple wj blocks return all ranges`() {
        val plain = "A B C D"
        val html = "<wj>A</wj> B <wj>C</wj> D"

        val ranges = extractRedLetterRanges(html, plain)

        assertEquals(listOf(0..0, 4..4), ranges)
    }

    @Test
    fun `wj spanning entire verse returns full range`() {
        val plain = "Blessed are the poor"
        val html = "<wj>Blessed are the poor</wj>"

        val ranges = extractRedLetterRanges(html, plain)

        assertEquals(listOf(0..(plain.length - 1)), ranges)
    }

    @Test
    fun `malformed tags handled gracefully`() {
        val plain = "Alpha Beta"
        val html = "<wj>Alpha Beta"

        val ranges = extractRedLetterRanges(html, plain)

        assertEquals(listOf(0..(plain.length - 1)), ranges)
    }

    @Test
    fun `range maps when html contains other tags`() {
        val plain = "Jesus wept"
        val html = "<i><wj>Jesus</wj></i> wept"

        val ranges = extractRedLetterRanges(html, plain)

        assertEquals(listOf(0..4), ranges)
    }
}
