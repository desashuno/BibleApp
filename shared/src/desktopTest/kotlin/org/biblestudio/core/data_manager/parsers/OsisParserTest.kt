package org.biblestudio.core.data_manager.parsers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OsisParserTest {

    @Test
    fun `parse extracts verses from OSIS XML`() {
        val xml = """
            <verse osisID="Gen.1.1" sID="Gen.1.1">In the beginning God created the heaven and the earth.</verse>
            <verse osisID="Gen.1.2" sID="Gen.1.2">And the earth was without form, and void.</verse>
        """.trimIndent()

        val result = OsisParser.parse(xml, "Test", "TST")
        assertEquals(1, result.books.size)
        assertEquals("Genesis", result.books[0].name)
        assertEquals(1, result.books[0].chapters.size)
        assertEquals(2, result.books[0].chapters[1]!!.size)
        assertEquals("In the beginning God created the heaven and the earth.", result.books[0].chapters[1]!![0].text)
    }

    @Test
    fun `parse extracts lemma references`() {
        val xml = """
            <verse osisID="Gen.1.1" sID="Gen.1.1"><w lemma="H7225">beginning</w> <w lemma="H430">God</w></verse>
        """.trimIndent()

        val result = OsisParser.parse(xml, "Test", "TST")
        val verse = result.books[0].chapters[1]!![0]
        assertEquals(listOf("H7225", "H430"), verse.lemmaRefs)
    }

    @Test
    fun `parse strips inline notes from text`() {
        val xml = """
            <verse osisID="Gen.1.1" sID="Gen.1.1">Text <note type="study">footnote</note> more text.</verse>
        """.trimIndent()

        val result = OsisParser.parse(xml, "Test", "TST")
        val text = result.books[0].chapters[1]!![0].text
        assertFalse(text.contains("footnote"))
        assertTrue(text.contains("Text"))
        assertTrue(text.contains("more text."))
    }

    @Test
    fun `validate returns errors for empty result`() {
        val result = ParseResult("Test", "TST", "en", emptyList())
        val validation = OsisParser.validate(result)
        assertFalse(validation.isValid)
        assertTrue(validation.errors.isNotEmpty())
    }

    @Test
    fun `validate returns valid for normal result`() {
        val book = ParsedBook(
            number = 1,
            name = "Genesis",
            testament = "OT",
            chapters = mapOf(
                1 to listOf(ParsedVerse(1, 1, 1, "In the beginning"))
            )
        )
        val result = ParseResult("Test", "TST", "en", listOf(book))
        val validation = OsisParser.validate(result)
        assertTrue(validation.isValid)
    }
}
