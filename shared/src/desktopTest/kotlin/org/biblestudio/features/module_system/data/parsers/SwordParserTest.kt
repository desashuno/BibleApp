package org.biblestudio.features.module_system.data.parsers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for [SwordParser] — .conf parsing and metadata extraction.
 */
class SwordParserTest {

    @Test
    fun `parseConf extracts key-value pairs`() {
        val conf = """
            [KJV]
            Description=King James Version
            DataPath=./modules/texts/rawtext/kjv/
            ModDrv=rawtext
            Lang=en
            Version=2.3
            Encoding=UTF-8
            SourceType=OSIS
        """.trimIndent()

        val map = SwordParser.parseConf(conf)
        assertEquals("King James Version", map["Description"])
        assertEquals("rawtext", map["ModDrv"])
        assertEquals("en", map["Lang"])
        assertEquals("2.3", map["Version"])
        assertEquals("UTF-8", map["Encoding"])
        assertEquals("OSIS", map["SourceType"])
    }

    @Test
    fun `extractMetadata builds SwordMetadata from conf`() {
        val conf = mapOf(
            "Description" to "King James Version",
            "ModDrv" to "rawtext",
            "Lang" to "en",
            "Version" to "2.3",
            "Encoding" to "UTF-8",
            "SourceType" to "OSIS"
        )

        val metadata = SwordParser.extractMetadata(conf)
        assertNotNull(metadata)
        assertEquals("King James Version", metadata.name)
        assertEquals("en", metadata.language)
        assertEquals("2.3", metadata.version)
        assertEquals("OSIS", metadata.sourceType)
        assertEquals("bible", metadata.moduleType)
    }

    @Test
    fun `extractMetadata resolves module type from ModDrv`() {
        val bibleConf = mapOf("ModDrv" to "rawtext")
        assertEquals("bible", SwordParser.extractMetadata(bibleConf).moduleType)

        val commentaryConf = mapOf("ModDrv" to "rawcom")
        assertEquals("commentary", SwordParser.extractMetadata(commentaryConf).moduleType)

        val dictionaryConf = mapOf("ModDrv" to "rawld")
        assertEquals("dictionary", SwordParser.extractMetadata(dictionaryConf).moduleType)
    }

    @Test
    fun `validate rejects conf without Description and ModDrv`() {
        val conf = mapOf("Lang" to "en")
        val validation = SwordParser.validate(conf)
        assertTrue(!validation.isValid)
        assertTrue(validation.errors.any { "description" in it.lowercase() || "ModDrv" in it })
    }

    @Test
    fun `validate accepts valid conf`() {
        val conf = mapOf(
            "ModDrv" to "rawtext",
            "DataPath" to "./modules/",
            "SourceType" to "OSIS"
        )
        val validation = SwordParser.validate(conf)
        assertTrue(validation.isValid, "Valid conf should pass validation")
    }
}
