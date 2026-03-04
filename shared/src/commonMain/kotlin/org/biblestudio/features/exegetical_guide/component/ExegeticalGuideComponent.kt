package org.biblestudio.features.exegetical_guide.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.study.CrossReference
import org.biblestudio.features.exegetical_guide.domain.entities.CommentaryEntry
import org.biblestudio.features.exegetical_guide.domain.entities.GrammaticalNote
import org.biblestudio.features.exegetical_guide.domain.entities.StructuralOutline
import org.biblestudio.features.exegetical_guide.domain.entities.TextVariant
import org.biblestudio.core.study.LexiconEntry

/**
 * Aggregated exegetical-guide state for a single verse.
 */
data class ExegeticalGuideState(
    val globalVerseId: Long? = null,
    val commentaries: List<CommentaryEntry> = emptyList(),
    val crossReferences: List<CrossReference> = emptyList(),
    val keyWords: List<LexiconEntry> = emptyList(),
    val textVariants: List<TextVariant> = emptyList(),
    val grammaticalNotes: List<GrammaticalNote> = emptyList(),
    val structuralOutline: StructuralOutline? = null,
    val expandedSections: Set<GuideSection> = GuideSection.entries.toSet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Collapsible sections within the Exegetical Guide.
 */
enum class GuideSection {
    TextCritical,
    Grammatical,
    Lexical,
    Structural,
    Commentaries,
    CrossReferences
}

/**
 * Component for the Exegetical Guide feature.
 */
interface ExegeticalGuideComponent {
    val state: StateFlow<ExegeticalGuideState>
    fun onVerseSelected(globalVerseId: Long)
    fun onToggleSection(section: GuideSection)
}
