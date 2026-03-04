package org.biblestudio.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.aakira.napier.Napier
import java.io.File

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver {
    val dbPath = File(appDataPath(), "biblestudio.db")
    val dbExists = dbPath.exists()
    val seeded = copySeedDatabaseIfNeeded(dbPath.absolutePath)

    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath.absolutePath}")

    if (!dbExists && !seeded) {
        // Brand-new install, no seed available — create empty schema then try ATTACH import
        Napier.i("Creating empty schema (no seed file copied)")
        schema.create(driver)
        driver.execute(null, "PRAGMA user_version = ${schema.version}", 0)
        importFromSeedIfAvailable(driver)
    } else if (seeded) {
        // Seed was just copied as the whole DB file — run migrations to bring it up to date
        Napier.i("Seed DB copied; running migrations to bring schema up to date")
        migrateIfNeeded(driver, schema)
    } else {
        // Existing DB — detect legacy databases that never had user_version stamped
        stampLegacyDatabaseVersion(driver, schema)
        migrateIfNeeded(driver, schema)
    }

    return driver
}

/**
 * Detects legacy databases created by the old code path that called `schema.create()`
 * but never stamped `PRAGMA user_version`. Without this, `migrateIfNeeded` sees
 * `user_version = 0` and tries to run all migrations from scratch — which crashes
 * because the tables already exist.
 */
private fun stampLegacyDatabaseVersion(driver: SqlDriver, schema: SqlSchema<QueryResult.Value<Unit>>) {
    val userVersion = driver.executeQuery(null, "PRAGMA user_version", { cursor ->
        cursor.next()
        QueryResult.Value(cursor.getLong(0) ?: 0L)
    }, 0).value

    if (userVersion > 0L) return // Already version-tracked, nothing to do

    // user_version = 0 — check sqlite_master to determine the actual schema state
    val hasDataModules = tableExists(driver, "data_modules")
    val hasBibles = tableExists(driver, "bibles")

    // Check if is_active column already exists on data_modules
    val hasIsActive = columnExists(driver, "data_modules", "is_active")

    when {
        hasDataModules && hasIsActive -> {
            // All migrations through 32 already applied
            Napier.i("Legacy DB detected with data_modules + is_active; stamping user_version = ${schema.version}")
            driver.execute(null, "PRAGMA user_version = ${schema.version}", 0)
        }
        hasDataModules -> {
            // Migration 31 ran but 32 (is_active) hasn't — stamp at 32 so migration 32 runs
            Napier.i("Legacy DB detected with data_modules but no is_active; stamping user_version = 32")
            driver.execute(null, "PRAGMA user_version = 32", 0)
        }
        hasBibles -> {
            // Old code ran schema.create() which included 30 migration files (versions 1–30)
            // but never stamped user_version. Mark it at 31 so only migration 31 runs.
            Napier.i("Legacy DB detected with bibles table but no data_modules; stamping user_version = 31")
            driver.execute(null, "PRAGMA user_version = 31", 0)
        }
        // else: truly empty DB — let migrateIfNeeded handle it from version 0
    }
}

private fun columnExists(driver: SqlDriver, tableName: String, columnName: String): Boolean {
    if (!tableExists(driver, tableName)) return false
    return try {
        driver.executeQuery(
            null,
            "SELECT $columnName FROM $tableName LIMIT 0",
            { QueryResult.Value(true) },
            0
        ).value
    } catch (_: Exception) {
        false
    }
}

private fun tableExists(driver: SqlDriver, tableName: String): Boolean {
    return driver.executeQuery(
        null,
        "SELECT count(*) FROM sqlite_master WHERE type = 'table' AND name = '$tableName'",
        { cursor ->
            cursor.next()
            QueryResult.Value((cursor.getLong(0) ?: 0L) > 0L)
        },
        0
    ).value
}

/**
 * Reads `PRAGMA user_version` and runs `schema.migrate(old, new)` if the DB
 * is behind the app's schema version. Updates user_version after migration.
 */
private fun migrateIfNeeded(driver: SqlDriver, schema: SqlSchema<QueryResult.Value<Unit>>) {
    val appVersion = schema.version
    val cursor = driver.executeQuery(null, "PRAGMA user_version", { cursor ->
        cursor.next()
        QueryResult.Value(cursor.getLong(0) ?: 0L)
    }, 0)
    val dbVersion = cursor.value

    when {
        dbVersion == 0L && appVersion > 0 -> {
            // Seed DB has user_version=0 — assume it matches migration 0, migrate all the way
            Napier.i("DB user_version=0, migrating 0 → $appVersion")
            schema.migrate(driver, 0, appVersion)
            driver.execute(null, "PRAGMA user_version = $appVersion", 0)
        }
        dbVersion < appVersion -> {
            Napier.i("Migrating DB from version $dbVersion → $appVersion")
            schema.migrate(driver, dbVersion, appVersion)
            driver.execute(null, "PRAGMA user_version = $appVersion", 0)
        }
        else -> {
            Napier.d("DB is up to date (version $dbVersion)")
        }
    }
}

/**
 * Extracts the seed DB from classpath resources to a temp file and uses
 * ATTACH-based import to populate the empty app DB with seed data.
 */
private fun importFromSeedIfAvailable(driver: SqlDriver) {
    val seedTempPath = extractSeedToTempFile() ?: return
    Napier.i("Importing seed data via ATTACH from $seedTempPath")
    SeedDataImporter.importSeedData(driver, seedTempPath)
}

actual fun appDataPath(): String {
    val os = System.getProperty("os.name").lowercase()
    val path = when {
        "win" in os -> System.getenv("APPDATA") + "\\BibleStudio"
        "mac" in os -> System.getProperty("user.home") + "/Library/Application Support/BibleStudio"
        else -> System.getProperty("user.home") + "/.local/share/biblestudio"
    }
    File(path).mkdirs()
    return path
}
