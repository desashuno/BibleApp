package org.biblestudio.di

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Initializes the Koin dependency injection graph.
 * Must be called once at application startup from each platform entry point.
 *
 * @param platformConfig optional lambda to add platform-specific Koin configuration
 *        (e.g., `androidContext(this)` on Android).
 */
fun initKoin(platformConfig: KoinApplication.() -> Unit = {}) {
    Napier.base(DebugAntilog())
    startKoin {
        platformConfig()
        modules(
            coreModule,
            repositoryModule,
            componentModule
        )
    }
}
