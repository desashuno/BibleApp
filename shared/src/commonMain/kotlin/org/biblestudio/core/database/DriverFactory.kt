package org.biblestudio.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/**
 * Creates a platform-specific [SqlDriver] for the BibleStudio SQLite database.
 *
 * Each platform provides its own `actual` implementation:
 * - Android → AndroidSqliteDriver
 * - iOS → NativeSqliteDriver
 * - Desktop (JVM) → JdbcSqliteDriver
 */
expect fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver
