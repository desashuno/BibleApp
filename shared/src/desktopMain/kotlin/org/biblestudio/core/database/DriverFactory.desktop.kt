package org.biblestudio.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver {
    val dbPath = File(appDataPath(), "biblestudio.db")
    val seeded = copySeedDatabaseIfNeeded(dbPath.absolutePath)
    val dbExists = dbPath.exists()
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbPath.absolutePath}")
    if (!dbExists && !seeded) {
        // No seed DB available — create empty schema
        schema.create(driver)
    }
    return driver
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
