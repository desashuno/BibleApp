package org.biblestudio.features.exegetical_guide.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.cross_references.domain.entities.CrossReference
import org.biblestudio.features.exegetical_guide.domain.entities.CommentaryEntry
import org.biblestudio.features.word_study.domain.entities.LexiconEntry

/**
 * Aggregated exegetical-guide state for a single verse.
 */
data class ExegeticalGuideState(
    val globalVerseId: Long? = null,
    val commentaries: List<CommentaryEntry> = emptyList(),
    val crossReferences: List<CrossReference> = emptyList(),
    val keyWords: List<LexiconEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Component for the Exegetical Guide feature.
 */
interface ExegeticalGuideComponent {
    val state: StateFlow<ExegeticalGuideState>
    fun onVerseSelected(globalVerseId: Long)
}
