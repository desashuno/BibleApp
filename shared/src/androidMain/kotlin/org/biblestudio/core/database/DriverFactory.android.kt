package org.biblestudio.core.database

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

private lateinit var appContext: Context

fun initAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver = AndroidSqliteDriver(
    schema = schema,
    context = appContext,
    name = "biblestudio.db"
)

actual fun appDataPath(): String = appContext.filesDir.absolutePath
