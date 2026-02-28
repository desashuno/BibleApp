package org.biblestudio.features.theological_atlas.domain.entities

/**
 * Represents a geographic location on the biblical atlas.
 *
 * @param id Database primary key.
 * @param name Ancient/biblical name.
 * @param modernName Modern equivalent name, if known.
 * @param latitude WGS-84 latitude.
 * @param longitude WGS-84 longitude.
 * @param type Category of the location.
 * @param description Human-readable description.
 * @param era Historical era when the location was prominent.
 * @param verseIds BBCCCVVV-encoded verse references linked to this location.
 */
data class AtlasLocation(
    val id: Long,
    val name: String,
    val modernName: String?,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType,
    val description: String,
    val era: String = "AllEras",
    val verseIds: List<Long> = emptyList()
)
