@file:Suppress("MatchingDeclarationName")

package org.biblestudio.ui.map

import java.io.File

actual object DiskTileCache {
    private val cacheDir: File by lazy {
        val context = org.biblestudio.core.database.appContext
        File(context.cacheDir, "tiles")
    }

    actual fun read(zoom: Int, x: Int, y: Int): ByteArray? {
        val file = File(cacheDir, "$zoom/$x/$y.png")
        return if (file.exists()) file.readBytes() else null
    }

    actual fun write(zoom: Int, x: Int, y: Int, data: ByteArray) {
        val file = File(cacheDir, "$zoom/$x/$y.png")
        file.parentFile?.mkdirs()
        file.writeBytes(data)
    }
}
