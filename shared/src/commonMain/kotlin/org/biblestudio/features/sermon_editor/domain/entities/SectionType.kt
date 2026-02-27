package org.biblestudio.features.sermon_editor.domain.entities

/**
 * Section types for a [Sermon].
 */
enum class SectionType(val raw: String) {
    Introduction("introduction"),
    Point("point"),
    Illustration("illustration"),
    Application("application"),
    Conclusion("conclusion");

    companion object {
        fun fromString(value: String): SectionType = entries.firstOrNull { it.raw == value } ?: Point
    }
}
