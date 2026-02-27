package org.biblestudio.core.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver = NativeSqliteDriver(
    schema = schema,
    name = "biblestudio.db"
)

actual fun appDataPath(): String {
    val paths = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    )
    return paths.first() as String
}
