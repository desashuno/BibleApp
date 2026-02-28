package org.biblestudio.ui.map

import java.io.File

actual object DiskTileCache {
    private val cacheDir: File by lazy {
        val os = System.getProperty("os.name").lowercase()
        val base = when {
            "win" in os -> System.getenv("APPDATA") + "\\BibleStudio"
            "mac" in os -> System.getProperty("user.home") + "/Library/Caches/BibleStudio"
            else -> System.getProperty("user.home") + "/.cache/biblestudio"
        }
        File(base, "tiles")
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
