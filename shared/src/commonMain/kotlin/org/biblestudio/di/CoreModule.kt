package org.biblestudio.di

import app.cash.sqldelight.db.SqlDriver
import org.biblestudio.core.database.createSqlDriver
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.database.BibleStudioDatabase
import org.koin.dsl.module

/**
 * Core DI module — platform driver, database instance, and shared services.
 */
val coreModule = module {
    single<SqlDriver> {
        createSqlDriver(BibleStudioDatabase.Schema)
    }

    single<BibleStudioDatabase> {
        BibleStudioDatabase(get())
    }

    single { VerseBus() }
}
