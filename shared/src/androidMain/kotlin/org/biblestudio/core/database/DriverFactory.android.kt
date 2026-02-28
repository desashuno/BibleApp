package org.biblestudio.core.database

import android.content.Context
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

internal lateinit var appContext: Context
    private set

fun initAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createSqlDriver(schema: SqlSchema<QueryResult.Value<Unit>>): SqlDriver {
    val dbFile = appContext.getDatabasePath("biblestudio.db")
    val seeded = copySeedDatabaseIfNeeded(dbFile.absolutePath)
    return if (seeded || dbFile.exists()) {
        // Seed DB already contains the schema — open without creating tables
        AndroidSqliteDriver(
            schema = schema,
            context = appContext,
            name = "biblestudio.db",
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Do nothing — seed DB already has the schema
                }
            }
        )
    } else {
        AndroidSqliteDriver(
            schema = schema,
            context = appContext,
            name = "biblestudio.db"
        )
    }
}

actual fun appDataPath(): String = appContext.filesDir.absolutePath
