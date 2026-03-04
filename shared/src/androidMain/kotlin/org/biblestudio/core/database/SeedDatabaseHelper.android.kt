package org.biblestudio.core.database

import io.github.aakira.napier.Napier
import java.io.File

actual fun extractSeedToTempFile(): String? = null // ATTACH-based import is desktop-only

actual fun copySeedDatabaseIfNeeded(targetPath: String): Boolean {
    val targetFile = File(targetPath)
    if (targetFile.exists()) return false

    try {
        // appContext is declared internal in DriverFactory.android.kt (same package)
        val assetStream = appContext.assets.open("biblestudio-seed.db")
        targetFile.parentFile?.mkdirs()
        assetStream.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Napier.i("Copied seed database from assets to ${targetFile.absolutePath}")
        return true
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        Napier.w("Seed database not found in assets — starting with empty DB", e)
        return false
    }
}
