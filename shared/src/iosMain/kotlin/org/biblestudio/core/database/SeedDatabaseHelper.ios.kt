package org.biblestudio.core.database

import io.github.aakira.napier.Napier
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.dataWithContentsOfFile

actual fun copySeedDatabaseIfNeeded(targetPath: String): Boolean {
    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(targetPath)) return false

    val seedPath = NSBundle.mainBundle.pathForResource("biblestudio-seed", ofType = "db")
    if (seedPath == null) {
        Napier.w("Seed database not found in app bundle — starting with empty DB")
        return false
    }

    val success = fileManager.copyItemAtPath(seedPath, toPath = targetPath, error = null)
    if (success) {
        Napier.i("Copied seed database to $targetPath")
    } else {
        Napier.e("Failed to copy seed database to $targetPath")
    }
    return success
}
