package org.biblestudio.di

import app.cash.sqldelight.db.SqlDriver
import org.biblestudio.core.data_manager.DataManager
import org.biblestudio.core.data_manager.DataModuleRepository
import org.biblestudio.core.data_manager.DataModuleRepositoryImpl
import org.biblestudio.core.data_manager.DefaultDataManager
import org.biblestudio.core.data_manager.handlers.BibleModuleHandler
import org.biblestudio.core.data_manager.handlers.CommentaryModuleHandler
import org.biblestudio.core.data_manager.handlers.CrossReferencesModuleHandler
import org.biblestudio.core.data_manager.handlers.DictionaryModuleHandler
import org.biblestudio.core.data_manager.handlers.EntitiesModuleHandler
import org.biblestudio.core.data_manager.handlers.GeographyModuleHandler
import org.biblestudio.core.data_manager.handlers.MorphologyModuleHandler
import org.biblestudio.core.data_manager.handlers.TimelineModuleHandler
import org.biblestudio.core.database.createSqlDriver
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.worship.WorshipPlayer
import org.biblestudio.features.worship.audio.AudioEngine
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

    single { AudioEngine() }
    single { WorshipPlayer(get(), get()) }

    single<DataModuleRepository> { DataModuleRepositoryImpl(get()) }

    single<DataManager> {
        DefaultDataManager(get<DataModuleRepository>()).also { dm ->
            dm.registerHandler(BibleModuleHandler(get()))
            dm.registerHandler(CommentaryModuleHandler())
            dm.registerHandler(DictionaryModuleHandler())
            dm.registerHandler(MorphologyModuleHandler())
            dm.registerHandler(CrossReferencesModuleHandler())
            dm.registerHandler(GeographyModuleHandler())
            dm.registerHandler(EntitiesModuleHandler())
            dm.registerHandler(TimelineModuleHandler())
        }
    }
}
