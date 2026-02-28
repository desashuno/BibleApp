package org.biblestudio.ui.map

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Load an OSM tile as an [ImageBitmap] from the network.
 *
 * URL format: `https://tile.openstreetmap.org/{z}/{x}/{y}.png`
 *
 * Returns `null` if the tile could not be loaded.
 */
expect suspend fun loadTileBitmap(zoom: Int, x: Int, y: Int): ImageBitmap?
