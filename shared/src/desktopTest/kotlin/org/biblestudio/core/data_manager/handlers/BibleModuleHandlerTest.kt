package org.biblestudio.core.data_manager.handlers

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.core.data_manager.model.DataModuleDescriptor
import org.biblestudio.core.data_manager.model.DataModuleType
import org.biblestudio.test.TestDatabase

class BibleModuleHandlerTest {

    private lateinit var testDb: TestDatabase
    private lateinit var handler: BibleModuleHandler

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        handler = BibleModuleHandler(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun `install imports inline OSIS data into bible tables`() = runTest {
        val descriptor = DataModuleDescriptor(
            moduleId = "bible-kjv",
            name = "King James Version",
            type = DataModuleType.Bible,
            metadata = """
                {
                  "format":"osis",
                  "abbreviation":"KJV",
                  "content":"<verse osisID=\"Gen.1.1\"><q who=\"Jesus\">In the beginning</q> God created.</verse>"
                }
            """.trimIndent()
        )

        val result = handler.install(descriptor)
        assertTrue(result.isSuccess)

        val bibles = testDb.database.bibleQueries.allBibles().executeAsList()
        assertEquals(1, bibles.size)
        assertEquals("KJV", bibles.first().abbreviation)

        val verse = testDb.database.bibleQueries.verseByGlobalId(1_001_001).executeAsOneOrNull()
        assertNotNull(verse)
        assertTrue(verse.text.contains("In the beginning"))
        assertTrue(verse.html_text?.contains("<wj>") == true)
    }

    @Test
    fun `install fails when descriptor has no inline source data`() = runTest {
        val descriptor = DataModuleDescriptor(
            moduleId = "bible-rvr1960",
            name = "RVR1960",
            type = DataModuleType.Bible,
            metadata = "{}",
            sourceUrl = ""
        )

        val result = handler.install(descriptor)
        assertTrue(result.isFailure)
    }
}
