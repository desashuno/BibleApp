package org.biblestudio.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.AfterTest
import kotlin.test.Test
import org.biblestudio.core.navigation.RootComponent
import org.biblestudio.core.navigation.RootConfig
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.database.BibleStudioDatabase
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

/**
 * Verifies the Koin dependency graph resolves without errors.
 * Uses an in-memory SQLite driver to satisfy the [SqlDriver] dependency.
 */
class KoinModulesTest : KoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `all modules resolve without errors`() {
        startKoin {
            modules(
                module {
                    single<SqlDriver> {
                        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
                        BibleStudioDatabase.Schema.create(driver)
                        driver
                    }
                    single { BibleStudioDatabase(get<SqlDriver>()) }
                    single { VerseBus() }
                },
                repositoryModule,
                componentModule
            )
        }

        // Verify all repositories resolve
        get<org.biblestudio.features.bible_reader.domain.repositories.BibleRepository>()

        // Verify RootComponent factory resolves with explicit parameters
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        get<RootComponent> { parametersOf(context, RootConfig.Workspace) }
    }
}
