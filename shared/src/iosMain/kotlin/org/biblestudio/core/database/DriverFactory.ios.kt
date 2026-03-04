package org.biblestudio.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver {
    val dbDir = appDataPath()
    val dbPath = "$dbDir/biblestudio.db"

    @Suppress("UnusedPrivateProperty")
    val seeded = copySeedDatabaseIfNeeded(dbPath)
    // NativeSqliteDriver always calls schema.create on first open if the DB is new.
    // When the seed DB was copied, the file already exists so NativeSqliteDriver
    // detects it and skips schema creation automatically.
    return NativeSqliteDriver(
        schema = schema,
        name = "biblestudio.db"
    )
}

actual fun appDataPath(): String {
    val paths = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    )
    return paths.first() as String
}
