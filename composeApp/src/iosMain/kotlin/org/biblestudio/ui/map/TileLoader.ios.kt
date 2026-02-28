package org.biblestudio.ui.map

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLConnection
import platform.Foundation.NSURLResponse
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue

actual suspend fun loadTileBitmap(zoom: Int, x: Int, y: Int): ImageBitmap? =
    withContext(Dispatchers.Default) {
        try {
            // Check disk cache first
            val cached = DiskTileCache.read(zoom, x, y)
            if (cached != null) {
                return@withContext Image.makeFromEncoded(cached).toComposeImageBitmap()
            }

            // Fetch from network
            val urlString = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
            val url = NSURL.URLWithString(urlString) ?: return@withContext null
            val data = NSData.dataWithContentsOfURL(url) ?: return@withContext null
            val bytes = ByteArray(data.length.toInt()).also { arr ->
                data.getBytes(arr.refTo(0), data.length)
            }

            // Save to disk cache
            DiskTileCache.write(zoom, x, y, bytes)

            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
