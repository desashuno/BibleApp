package org.biblestudio.features.morphology_interlinear.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParsingDecoderTest {

    private val decoder = ParsingDecoder()

    @Test
    fun decodesHebrewVerbParsing() {
        val result = decoder.decode("V-QAL-3MS")
        assertTrue(result.contains("Verb"))
        assertTrue(result.contains("Qal"))
        assertTrue(result.contains("3rd Person"))
        assertTrue(result.contains("Masculine"))
        assertTrue(result.contains("Singular"))
    }

    @Test
    fun decodesHebrewNounParsing() {
        val result = decoder.decode("N-MPC")
        assertTrue(result.contains("Noun"))
        assertTrue(result.contains("Masculine"))
        assertTrue(result.contains("Plural"))
    }

    @Test
    fun decodesGreekAoristParsing() {
        val result = decoder.decode("V-AAI-3S")
        assertTrue(result.contains("Verb"))
        assertTrue(result.contains("Aorist Active Indicative"))
        assertTrue(result.contains("3rd Person"))
        assertTrue(result.contains("Singular"))
    }

    @Test
    fun unknownCodeReturnsAsIs() {
        val result = decoder.decode("XYZ")
        assertEquals("XYZ", result)
    }

    @Test
    fun emptyCodeReturnsEmpty() {
        val result = decoder.decode("")
        assertEquals("", result)
    }
}
