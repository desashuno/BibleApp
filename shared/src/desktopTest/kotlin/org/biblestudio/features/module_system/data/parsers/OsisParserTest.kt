package org.biblestudio.features.module_system.data.parsers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [OsisParser] — OSIS XML parsing and validation.
 */
class OsisParserTest {

    @Test
    fun `parse extracts verses from OSIS XML`() {
        val xml = """
            <osis>
              <osisText>
                <div type="book" osisID="Gen">
                  <chapter osisID="Gen.1">
                    <verse osisID="Gen.1.1">In the beginning God created the heaven and the earth.</verse>
                    <verse osisID="Gen.1.2">And the earth was without form, and void.</verse>
                  </chapter>
                </div>
              </osisText>
            </osis>
        """.trimIndent()

        val result = OsisParser.parse(xml, "Test Bible", "TST")
        assertEquals("Test Bible", result.moduleName)
        assertEquals("TST", result.abbreviation)
        assertTrue(result.books.isNotEmpty(), "Expected at least one book")

        val genesis = result.books.first()
        assertEquals(1, genesis.number)
        assertEquals("Genesis", genesis.name)
        assertEquals("OT", genesis.testament)
        assertTrue(genesis.chapters.containsKey(1))

        val ch1Verses = genesis.chapters[1]!!
        assertEquals(2, ch1Verses.size)
        assertEquals(1, ch1Verses[0].verseNumber)
        assertTrue(ch1Verses[0].text.contains("beginning"))
        assertEquals(2, ch1Verses[1].verseNumber)
    }

    @Test
    fun `parse extracts lemma references`() {
        val xml = """
            <osis>
              <osisText>
                <div type="book" osisID="Gen">
                  <chapter osisID="Gen.1">
                    <verse osisID="Gen.1.1">In the <w lemma="strong:H7225">beginning</w> God created.</verse>
                  </chapter>
                </div>
              </osisText>
            </osis>
        """.trimIndent()

        val result = OsisParser.parse(xml, "Lemma Test", "LT")
        val verse = result.books.first().chapters[1]!!.first()
        assertTrue(verse.lemmaRefs.contains("strong:H7225"), "Expected Strong's reference strong:H7225")
    }

    @Test
    fun `parse strips inline notes from text`() {
        val xml = """
            <osis>
              <osisText>
                <div type="book" osisID="Gen">
                  <chapter osisID="Gen.1">
                    <verse osisID="Gen.1.1">In the beginning<note type="x">footnote</note> God created.</verse>
                  </chapter>
                </div>
              </osisText>
            </osis>
        """.trimIndent()

        val result = OsisParser.parse(xml, "Note Test", "NT")
        val text = result.books.first().chapters[1]!!.first().text
        assertTrue("footnote" !in text, "Notes should be stripped from plain text")
        assertTrue("beginning" in text)
    }

    @Test
    fun `validate returns errors for empty result`() {
        val result = ParseResult(
            moduleName = "Empty",
            abbreviation = "EMP",
            language = "en",
            books = emptyList(),
            errors = emptyList()
        )

        val validation = OsisParser.validate(result)
        assertTrue(!validation.isValid, "Empty result should not be valid")
        assertTrue(validation.errors.isNotEmpty())
    }

    @Test
    fun `validate returns valid for normal result`() {
        val xml = """
            <osis>
              <osisText>
                <div type="book" osisID="Gen">
                  <chapter osisID="Gen.1">
                    <verse osisID="Gen.1.1">In the beginning.</verse>
                  </chapter>
                </div>
              </osisText>
            </osis>
        """.trimIndent()

        val result = OsisParser.parse(xml, "Valid Test", "VT")
        val validation = OsisParser.validate(result)
        assertTrue(validation.isValid, "Result with books should be valid")
    }
}
