package org.biblestudio.features.module_system.data.parsers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [UsfmParser] — USFM content parsing and validation.
 */
class UsfmParserTest {

    @Test
    fun `parse extracts book and verses from USFM content`() {
        val content = """
            \id GEN - Test Bible
            \h Genesis
            \c 1
            \p
            \v 1 In the beginning God created the heaven and the earth.
            \v 2 And the earth was without form, and void.
        """.trimIndent()

        val books = mapOf("GEN" to content)
        val result = UsfmParser.parse(books, "USFM Bible", "UFM")

        assertEquals("USFM Bible", result.moduleName)
        assertEquals("UFM", result.abbreviation)
        assertTrue(result.books.isNotEmpty())

        val genesis = result.books.first()
        assertEquals(1, genesis.number)
        assertEquals("Genesis", genesis.name)
        assertTrue(genesis.chapters.containsKey(1))

        val verses = genesis.chapters[1]!!
        assertEquals(2, verses.size)
        assertEquals(1, verses[0].verseNumber)
        assertTrue(verses[0].text.contains("beginning"))
    }

    @Test
    fun `parse extracts Strong numbers from w markers`() {
        val content = """
            \id GEN
            \h Genesis
            \c 1
            \p
            \v 1 In the \w beginning|strong="H7225"\w* God created.
        """.trimIndent()

        val result = UsfmParser.parse(mapOf("GEN" to content), "Strong Test", "ST")
        val verse = result.books.first().chapters[1]!!.first()
        assertTrue(verse.lemmaRefs.contains("H7225"), "Expected Strong's number H7225")
    }

    @Test
    fun `parse strips footnotes and cross-references`() {
        val content = """
            \id GEN
            \h Genesis
            \c 1
            \p
            \v 1 In the beginning\f + \ft a footnote\f* God\x + \xo 1:1 \xt Joh 1:1\x* created.
        """.trimIndent()

        val result = UsfmParser.parse(mapOf("GEN" to content), "Clean Test", "CT")
        val text = result.books.first().chapters[1]!!.first().text
        assertTrue("footnote" !in text, "Footnotes should be stripped")
        assertTrue("Joh 1:1" !in text, "Cross-references should be stripped")
        assertTrue("beginning" in text)
        assertTrue("created" in text)
    }

    @Test
    fun `parse handles multiple chapters`() {
        val content = """
            \id GEN
            \h Genesis
            \c 1
            \p
            \v 1 First chapter verse.
            \c 2
            \p
            \v 1 Second chapter verse.
        """.trimIndent()

        val result = UsfmParser.parse(mapOf("GEN" to content), "Multi Chapter", "MC")
        val genesis = result.books.first()
        assertTrue(genesis.chapters.containsKey(1))
        assertTrue(genesis.chapters.containsKey(2))
        assertEquals(1, genesis.chapters[1]!!.size)
        assertEquals(1, genesis.chapters[2]!!.size)
    }

    @Test
    fun `validate rejects empty result`() {
        val result = ParseResult(
            moduleName = "Empty",
            abbreviation = "EMP",
            language = "en",
            books = emptyList(),
            errors = emptyList()
        )

        val validation = UsfmParser.validate(result)
        assertTrue(!validation.isValid)
    }
}
