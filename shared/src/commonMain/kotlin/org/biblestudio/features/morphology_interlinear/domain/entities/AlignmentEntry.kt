package org.biblestudio.features.morphology_interlinear.domain.entities

/**
 * Pre-computed alignment between an English translation token and a
 * Strong's number for Reverse Interlinear display.
 *
 * @param id Auto-generated database ID.
 * @param globalVerseId The BBCCCVVV-encoded verse identifier.
 * @param englishPosition 1-based position of the English token in the verse.
 * @param englishToken The English word/token.
 * @param originalPosition Position of the corresponding original-language word.
 * @param strongsNumber Strong's reference linking to the original word.
 */
data class AlignmentEntry(
    val id: Long,
    val globalVerseId: Long,
    val englishPosition: Long,
    val englishToken: String,
    val originalPosition: Long,
    val strongsNumber: String
)
