package org.biblestudio.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.biblestudio.AppInfo
import org.biblestudio.database.BibleStudioDatabase

/**
 * Verifies the SQLDelight schema creates all expected tables and matches the
 * project-wide database version constant.
 */
class DriverFactoryTest {

    /**
     * Creates an in-memory JdbcSqliteDriver, runs [BibleStudioDatabase.Schema.create],
     * and asserts that all 27 regular tables exist in `sqlite_master`.
     */
    @Test
    fun `in-memory driver creates all 29 tables`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BibleStudioDatabase.Schema.create(driver)

        val tables = mutableListOf<String>()
        driver.execute(null, "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'", 0)
        val cursor = driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
            mapper = { cursor ->
                val result = mutableListOf<String>()
                while (cursor.next().value) {
                    result.add(cursor.getString(0)!!)
                }
                app.cash.sqldelight.db.QueryResult.Value(result)
            },
            parameters = 0
        )

        val tableNames = cursor.value

        val expectedTables = listOf(
            // Bible text (4)
            "bibles", "books", "chapters", "verses",
            // Annotations (3)
            "notes", "highlights", "bookmarks",
            // Study (3)
            "lexicon_entries", "morphology", "word_occurrences",
            // Resources (2)
            "resources", "resource_entries",
            // Writing (2)
            "sermons", "sermon_sections",
            // References (2)
            "cross_references", "parallel_passages",
            // Settings (3)
            "settings", "workspaces", "workspace_layouts",
            // Search (1)
            "search_history",
            // Sync (2)
            "sync_log", "delete_log",
            // Timeline & Geography (2)
            "timeline_events", "geographic_locations",
            // Audio (1)
            "audio_timestamps",
            // Reading Plans (2)
            "reading_plans", "reading_plan_progress",
            // Module System (2)
            "installed_modules", "backup_history"
        )

        expectedTables.forEach { table ->
            assertTrue(
                actual = table in tableNames,
                message = "Expected table '$table' not found. Found tables: $tableNames"
            )
        }
        assertEquals(
            expected = 29,
            actual = expectedTables.size,
            message = "Expected table list should have exactly 29 entries"
        )

        driver.close()
    }

    /**
     * Verifies the schema version reported by SQLDelight matches [AppInfo.DATABASE_VERSION].
     */
    @Test
    fun `schema version matches AppInfo DATABASE_VERSION`() {
        val schemaVersion = BibleStudioDatabase.Schema.version
        assertEquals(
            expected = AppInfo.DATABASE_VERSION.toLong(),
            actual = schemaVersion,
            message = "SQLDelight schema version ($schemaVersion) " +
                "must match AppInfo.DATABASE_VERSION (${AppInfo.DATABASE_VERSION})"
        )
    }
}
