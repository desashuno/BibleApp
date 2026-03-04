@file:Suppress("MatchingDeclarationName")

package org.biblestudio.ui.map

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile

actual object DiskTileCache {
    private val cacheDir: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        "${paths.first()}/BibleStudio/tiles"
    }

    actual fun read(zoom: Int, x: Int, y: Int): ByteArray? {
        val path = "$cacheDir/$zoom/$x/$y.png"
        val data = NSData.dataWithContentsOfFile(path) ?: return null
        return data.toByteArray()
    }

    actual fun write(zoom: Int, x: Int, y: Int, data: ByteArray) {
        val path = "$cacheDir/$zoom/$x/$y.png"
        val dir = "$cacheDir/$zoom/$x"
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(dir)) {
            fm.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        }
        data.toNSData().writeToFile(path, atomically = true)
    }
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        kotlinx.cinterop.memcpy(bytes.refTo(0), this.bytes, this.length)
    }
    return bytes
}

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return kotlinx.cinterop.memScoped {
        platform.Foundation.NSData.create(bytes = this@toNSData.refTo(0), length = this@toNSData.size.toULong())
    }
}
