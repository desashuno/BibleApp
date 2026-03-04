package org.biblestudio.core.database

import app.cash.sqldelight.db.SqlDriver
import io.github.aakira.napier.Napier
import java.io.File

/**
 * Imports data from the bundled seed database into the app's properly-schemaed
 * database using `ATTACH DATABASE`. Tables with compatible schemas are copied
 * directly; tables with divergent schemas are column-mapped.
 *
 * The seed DB uses the normalize.py schema (flat tables), while the app uses
 * SQLDelight migrations that renamed/restructured several tables:
 * - `geographic_locations` → `atlas_locations`
 * - `location_verse_index` → `atlas_location_verses`
 * - `entities` → `graph_nodes`
 * - `relationships` → `graph_edges`
 * - `entity_verse_index` → `graph_node_verses`
 * - `timeline_events` columns renamed
 */
@Suppress("TooGenericExceptionCaught")
internal object SeedDataImporter {

    /**
     * Direct-copy tables: seed and app schemas are compatible.
     */
    private val DIRECT_COPY_TABLES = listOf(
        "bibles",
        "books",
        "chapters",
        "verses",
        "lexicon_entries",
        "morphology",
        "word_occurrences",
        "cross_references",
        "parallel_passages",
        "settings",
        "search_history",
        "reading_plans",
        "reading_plan_progress",
        "reading_plan_entries",
        "dictionary_entries",
        "dictionary_entry_verses",
        "resources",
        "resource_entries",
        "sermons",
        "sermon_sections",
        "data_modules"
    )

    /**
     * Imports seed data from [seedPath] into the app database via the given [driver].
     * Attaches the seed DB, copies all data, rebuilds FTS indexes, then detaches.
     *
     * @return `true` if import succeeded, `false` otherwise.
     */
    @Suppress("LongMethod")
    fun importSeedData(driver: SqlDriver, seedPath: String): Boolean {
        val seedFile = File(seedPath)
        if (!seedFile.exists()) {
            Napier.w("Seed DB file not found at $seedPath")
            return false
        }

        return try {
            Napier.i("Starting seed data import from $seedPath")

            // Attach seed database
            driver.execute(null, "ATTACH DATABASE '$seedPath' AS seed", 0)

            // Direct-copy tables
            for (table in DIRECT_COPY_TABLES) {
                copyTableSafe(driver, table)
            }

            // Column-mapped tables
            importAtlasLocations(driver)
            importAtlasLocationVerses(driver)
            importAtlasRegions(driver)
            importGraphNodes(driver)
            importGraphEdges(driver)
            importGraphNodeVerses(driver)
            importTimelineEvents(driver)
            importTimelineEventVerses(driver)

            // Rebuild FTS indexes
            rebuildFtsIndexes(driver)

            // Detach and clean up
            driver.execute(null, "DETACH DATABASE seed", 0)
            seedFile.delete()
            Napier.i("Seed data import completed successfully")
            true
        } catch (e: Exception) {
            Napier.e("Seed data import failed", e)
            try {
                driver.execute(null, "DETACH DATABASE seed", 0)
            } catch (_: Exception) { /* ignore */ }
            seedFile.delete()
            false
        }
    }

    /**
     * Copies all rows from `seed.[table]` to `main.[table]` using INSERT OR IGNORE.
     * Skipped silently if the seed table doesn't exist (e.g. seed was generated
     * without that normalizer).
     */
    private fun copyTableSafe(driver: SqlDriver, table: String) {
        try {
            driver.execute(null, "INSERT OR IGNORE INTO main.$table SELECT * FROM seed.$table", 0)
            Napier.d("Imported table: $table")
        } catch (e: Exception) {
            Napier.w("Skipped table $table: ${e.message}")
        }
    }

    /**
     * `geographic_locations` (seed) → `atlas_locations` (app)
     * Seed: id, name, modern_name, lat, lon, type, description, era, verse_references
     * App:  id, name, modern_name, latitude, longitude, type, description, era
     */
    private fun importAtlasLocations(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.atlas_locations
                    (id, name, modern_name, latitude, longitude, type, description, era)
                SELECT id, name, modern_name, lat, lon, type, description, era
                FROM seed.geographic_locations
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: atlas_locations (from geographic_locations)")
        } catch (e: Exception) {
            Napier.w("Skipped atlas_locations: ${e.message}")
        }
    }

    /**
     * `location_verse_index` (seed) → `atlas_location_verses` (app)
     * Seed: location_id, global_verse_id (composite PK)
     * App:  id (autoincrement), location_id, global_verse_id
     */
    private fun importAtlasLocationVerses(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.atlas_location_verses (location_id, global_verse_id)
                SELECT location_id, global_verse_id
                FROM seed.location_verse_index
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: atlas_location_verses (from location_verse_index)")
        } catch (e: Exception) {
            Napier.w("Skipped atlas_location_verses: ${e.message}")
        }
    }

    /**
     * `entities` (seed) → `graph_nodes` (app)
     * Seed: id, name, type, description, aliases, verse_references
     * App:  id, name, type, description, properties
     */
    private fun importGraphNodes(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.graph_nodes (id, name, type, description, properties)
                SELECT id, name, type, description, '{}'
                FROM seed.entities
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: graph_nodes (from entities)")
        } catch (e: Exception) {
            Napier.w("Skipped graph_nodes: ${e.message}")
        }
    }

    /**
     * `relationships` (seed) → `graph_edges` (app)
     * Seed: id, source_entity_id, target_entity_id, type, description
     * App:  id, source_id, target_id, relationship, weight
     */
    private fun importGraphEdges(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.graph_edges (id, source_id, target_id, relationship, weight)
                SELECT id, source_entity_id, target_entity_id, type, 1.0
                FROM seed.relationships
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: graph_edges (from relationships)")
        } catch (e: Exception) {
            Napier.w("Skipped graph_edges: ${e.message}")
        }
    }

    /**
     * `entity_verse_index` (seed) → `graph_node_verses` (app)
     * Seed: entity_id, global_verse_id (composite PK)
     * App:  id (autoincrement), node_id, global_verse_id
     */
    private fun importGraphNodeVerses(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.graph_node_verses (node_id, global_verse_id)
                SELECT entity_id, global_verse_id
                FROM seed.entity_verse_index
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: graph_node_verses (from entity_verse_index)")
        } catch (e: Exception) {
            Napier.w("Skipped graph_node_verses: ${e.message}")
        }
    }

    /**
     * `atlas_regions` (seed) → `atlas_regions` (app)
     * Direct copy — schemas are compatible.
     */
    private fun importAtlasRegions(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.atlas_regions
                    (id, name, description, bounds_north, bounds_south, bounds_east, bounds_west)
                SELECT id, name, description, bounds_north, bounds_south, bounds_east, bounds_west
                FROM seed.atlas_regions
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: atlas_regions")
        } catch (e: Exception) {
            Napier.w("Skipped atlas_regions: ${e.message}")
        }
    }

    /**
     * `timeline_events` (seed) → `timeline_events` (app)
     * Seed: id, title, description, year_start, year_end, era, importance
     * App:  id, title, description, start_year, end_year, category, importance
     */
    private fun importTimelineEvents(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.timeline_events
                    (id, title, description, start_year, end_year, category, importance)
                SELECT id, title, description, year_start, year_end, era, importance
                FROM seed.timeline_events
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: timeline_events (column-mapped)")
        } catch (e: Exception) {
            Napier.w("Skipped timeline_events: ${e.message}")
        }
    }

    /**
     * `timeline_event_verses` (seed) → `timeline_event_verses` (app)
     * Seed: id, event_id, global_verse_id
     * App:  id, event_id, global_verse_id
     */
    private fun importTimelineEventVerses(driver: SqlDriver) {
        try {
            driver.execute(
                null,
                """
                INSERT OR IGNORE INTO main.timeline_event_verses (event_id, global_verse_id)
                SELECT event_id, global_verse_id
                FROM seed.timeline_event_verses
                """.trimIndent(),
                0
            )
            Napier.d("Imported table: timeline_event_verses")
        } catch (e: Exception) {
            Napier.w("Skipped timeline_event_verses: ${e.message}")
        }
    }

    /**
     * Rebuilds all FTS5 indexes so they reflect the newly imported data.
     */
    private fun rebuildFtsIndexes(driver: SqlDriver) {
        val ftsIndexes = listOf(
            "fts_verses",
            "fts_lexicon",
            "fts_notes",
            "fts_resources",
            "fts_sermons",
            "fts_graph_nodes",
            "fts_atlas_locations",
            "fts_dictionary_entries"
        )
        for (fts in ftsIndexes) {
            try {
                driver.execute(null, "INSERT INTO $fts($fts) VALUES('rebuild')", 0)
                Napier.d("Rebuilt FTS index: $fts")
            } catch (e: Exception) {
                Napier.w("Could not rebuild FTS index $fts: ${e.message}")
            }
        }
    }
}
