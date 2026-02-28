package org.biblestudio.features.theological_atlas.domain.entities

/**
 * Represents a named geographic region with bounding box.
 */
data class AtlasRegion(
    val id: Long,
    val name: String,
    val description: String,
    val boundsNorth: Double,
    val boundsSouth: Double,
    val boundsEast: Double,
    val boundsWest: Double
)
