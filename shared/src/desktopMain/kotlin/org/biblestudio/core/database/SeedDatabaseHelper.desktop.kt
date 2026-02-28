package org.biblestudio.core.database

import io.github.aakira.napier.Napier
import java.io.File

actual fun copySeedDatabaseIfNeeded(targetPath: String): Boolean {
    val targetFile = File(targetPath)
    if (targetFile.exists()) return false

    val seedStream = Thread.currentThread().contextClassLoader
        ?.getResourceAsStream("biblestudio-seed.db")
        ?: SeedDatabaseHelper::class.java.getResourceAsStream("/biblestudio-seed.db")

    if (seedStream == null) {
        Napier.w("Seed database not found in classpath resources — starting with empty DB")
        return false
    }

    targetFile.parentFile?.mkdirs()
    seedStream.use { input ->
        targetFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    Napier.i("Copied seed database to ${targetFile.absolutePath}")
    return true
}

/** Anchor class for classloader resource lookup. */
private object SeedDatabaseHelper
