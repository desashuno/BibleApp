package org.biblestudio.core.database

/**
 * Copies the bundled seed database (`biblestudio-seed.db`) to the app's data directory
 * if no database file exists yet.
 *
 * Each platform provides its own `actual` implementation:
 * - Android → copies from `assets/biblestudio-seed.db`
 * - iOS     → copies from `NSBundle.mainBundle`
 * - Desktop → copies from classpath resources
 *
 * @param targetPath Full path to the target database file (e.g. `appDataPath()/biblestudio.db`)
 * @return `true` if the seed was copied (first launch), `false` if the DB already existed.
 */
expect fun copySeedDatabaseIfNeeded(targetPath: String): Boolean

/**
 * Extracts the bundled seed database to a temporary file for ATTACH-based import.
 *
 * @return Absolute path to the temporary copy, or `null` if no seed is bundled.
 */
expect fun extractSeedToTempFile(): String?
