package org.biblestudio.features.passage_guide.domain.entities

import org.biblestudio.core.study.CrossReference
import org.biblestudio.core.study.MorphWord
import org.biblestudio.features.note_editor.domain.entities.Note
import org.biblestudio.features.resource_library.domain.entities.ResourceEntry
import org.biblestudio.core.study.LexiconEntry

/**
 * Aggregated study report for a single verse, combining data from
 * six different repositories.
 *
 * @param verseId The BBCCCVVV-encoded verse reference.
 * @param verseText Full text of the verse.
 * @param crossReferences Related cross-reference entries.
 * @param outlines Passage outlines covering this verse.
 * @param keyWords Important words with lexicon definitions.
 * @param commentaryEntries Commentary/resource entries for the verse.
 * @param userNotes User's notes on this verse.
 * @param morphologyWords Word-level linguistic data.
 */
data class PassageReport(
    val verseId: Long,
    val verseText: String,
    val crossReferences: List<CrossReference>,
    val outlines: List<Outline>,
    val keyWords: List<LexiconEntry>,
    val commentaryEntries: List<ResourceEntry>,
    val userNotes: List<Note>,
    val morphologyWords: List<MorphWord>
)
