package org.biblestudio.ui.map

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun loadTileBitmap(zoom: Int, x: Int, y: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        try {
            // Check disk cache first
            val cached = DiskTileCache.read(zoom, x, y)
            if (cached != null) {
                return@withContext BitmapFactory.decodeByteArray(cached, 0, cached.size)?.asImageBitmap()
            }

            // Fetch from network
            val url = URL("https://tile.openstreetmap.org/$zoom/$x/$y.png")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "BibleStudio/0.1 (Compose Multiplatform)")
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            val bytes = connection.inputStream.use { it.readBytes() }

            // Save to disk cache
            DiskTileCache.write(zoom, x, y, bytes)

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
