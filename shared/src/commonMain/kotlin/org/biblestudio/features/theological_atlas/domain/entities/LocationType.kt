package org.biblestudio.features.theological_atlas.domain.entities

/**
 * Categories of geographic locations in the biblical atlas.
 */
enum class LocationType(val displayName: String) {
    City("City"),
    Town("Town"),
    Village("Village"),
    Mountain("Mountain"),
    River("River"),
    Lake("Lake"),
    Sea("Sea"),
    Region("Region"),
    Desert("Desert"),
    Valley("Valley"),
    Island("Island");

    companion object {
        fun fromString(value: String): LocationType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: City
    }
}
