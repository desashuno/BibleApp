package org.biblestudio.core.navigation

import kotlinx.serialization.Serializable

/**
 * Configuration for each screen in the root navigation stack.
 *
 * Each variant is [Serializable] so Decompose can persist and restore
 * the back-stack across configuration changes and process death.
 */
@Serializable
sealed class RootConfig {

    /** Main workspace screen with multi-pane layout. */
    @Serializable
    data object Workspace : RootConfig()

    /** Application settings screen. */
    @Serializable
    data object Settings : RootConfig()

    /** Bible / resource import wizard. */
    @Serializable
    data object Import : RootConfig()

    /**
     * Deep link that navigates to a specific verse.
     * On resolution, publishes to VerseBus then transitions to [Workspace].
     *
     * @param globalVerseId The BBCCCVVV-encoded verse identifier.
     */
    @Serializable
    data class DeepLink(val globalVerseId: Long) : RootConfig()
}
