package org.biblestudio.ui.map

import org.biblestudio.AppInfo

/**
 * Centralised constants for the OSM tile map subsystem.
 */
object MapConstants {
    /** OSM tile URL template — placeholders: `$zoom`, `$x`, `$y`. */
    const val TILE_URL_TEMPLATE = "https://tile.openstreetmap.org/"

    /** User-Agent sent with tile requests (OSM usage policy requires identification). */
    val USER_AGENT = "${AppInfo.NAME}/${AppInfo.VERSION} (Compose Multiplatform)"

    /** Default centre — Jerusalem. */
    const val DEFAULT_LAT = 31.7683
    const val DEFAULT_LNG = 35.2137
    const val DEFAULT_ZOOM = 7
}
