package org.biblestudio.ui.map

/**
 * Platform-specific disk tile cache.
 *
 * Stores downloaded OSM tiles as PNG files in `{cacheDir}/tiles/{z}/{x}/{y}.png`.
 * Each platform provides its own `actual` implementation.
 */
expect object DiskTileCache {
    /**
     * Read a cached tile from disk.
     * @return raw PNG bytes, or `null` if not cached.
     */
    fun read(zoom: Int, x: Int, y: Int): ByteArray?

    /**
     * Write a tile to disk cache.
     * @param data raw PNG bytes.
     */
    fun write(zoom: Int, x: Int, y: Int, data: ByteArray)
}
