package org.biblestudio.core.database

/**
 * Returns the platform-specific application data directory path.
 *
 * Each platform provides its own `actual` implementation:
 * - Android → context.filesDir
 * - iOS → NSDocumentDirectory
 * - Desktop → OS-specific app data path
 */
expect fun appDataPath(): String
