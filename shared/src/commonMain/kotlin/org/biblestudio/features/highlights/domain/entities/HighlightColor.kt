package org.biblestudio.features.highlights.domain.entities

/**
 * Pre-defined highlight colour palette (8 colours).
 * The [index] matches the position in [AppColors.highlights].
 */
@Suppress("MagicNumber")
enum class HighlightColor(val index: Long) {
    Yellow(0),
    Green(1),
    Blue(2),
    Pink(3),
    Orange(4),
    Purple(5),
    Red(6),
    Teal(7);

    companion object {
        fun fromIndex(value: Long): HighlightColor = entries.firstOrNull { it.index == value } ?: Yellow
    }
}
