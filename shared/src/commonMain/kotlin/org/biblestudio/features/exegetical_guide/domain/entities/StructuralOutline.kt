package org.biblestudio.features.exegetical_guide.domain.entities

/**
 * A structural/rhetorical outline for a passage surrounding a verse.
 *
 * @param id Unique identifier.
 * @param passageRange Human-readable passage range (e.g. "Romans 8:1-11").
 * @param title Outline title.
 * @param elements Ordered list of structural elements.
 */
data class StructuralOutline(
    val id: Long,
    val passageRange: String,
    val title: String,
    val elements: List<OutlineElement>
)

/**
 * A single element within a structural outline.
 *
 * @param label Section label (e.g. "A", "B", "A'").
 * @param description Content of the section.
 * @param verseRange Verse range covered by this element.
 * @param depth Nesting depth (0 = top level).
 */
data class OutlineElement(
    val label: String,
    val description: String,
    val verseRange: String,
    val depth: Int = 0
)
