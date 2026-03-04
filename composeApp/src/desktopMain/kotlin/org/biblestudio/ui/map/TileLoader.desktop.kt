package org.biblestudio.ui.map

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun loadTileBitmap(zoom: Int, x: Int, y: Int): ImageBitmap? = withContext(Dispatchers.IO) {
    try {
        // Check disk cache first
        val cached = DiskTileCache.read(zoom, x, y)
        if (cached != null) {
            return@withContext org.jetbrains.skia.Image.makeFromEncoded(cached).toComposeImageBitmap()
        }

        // Fetch from network
        val url = URL("${MapConstants.TILE_URL_TEMPLATE}$zoom/$x/$y.png")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", MapConstants.USER_AGENT)
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        val bytes = connection.inputStream.use { it.readBytes() }

        // Save to disk cache
        DiskTileCache.write(zoom, x, y, bytes)

        org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}
