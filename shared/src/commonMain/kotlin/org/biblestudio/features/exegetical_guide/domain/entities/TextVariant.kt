package org.biblestudio.features.exegetical_guide.domain.entities

/**
 * A textual variant (text-critical note) for a verse.
 *
 * @param id Unique identifier.
 * @param manuscriptSigla Sigla of the manuscript family (e.g. "P46", "Aleph", "B").
 * @param readingText The variant reading text.
 * @param supportingManuscripts Comma-separated manuscript names supporting this reading.
 * @param notes Editorial notes about the variant.
 */
data class TextVariant(
    val id: Long,
    val manuscriptSigla: String,
    val readingText: String,
    val supportingManuscripts: String,
    val notes: String = ""
)
