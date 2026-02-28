package org.biblestudio.features.exegetical_guide.component

import com.arkivanov.decompose.ComponentContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository
import org.biblestudio.features.exegetical_guide.domain.repositories.CommentaryRepository
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository

/**
 * Default [ExegeticalGuideComponent] aggregating commentaries, cross-references,
 * and key words for the currently selected verse.
 */
class DefaultExegeticalGuideComponent(
    componentContext: ComponentContext,
    private val commentaryRepository: CommentaryRepository,
    private val crossRefRepository: CrossRefRepository,
    private val wordStudyRepository: WordStudyRepository,
    private val verseBus: VerseBus
) : ExegeticalGuideComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(ExegeticalGuideState())
    override val state: StateFlow<ExegeticalGuideState> = _state.asStateFlow()

    init {
        observeVerseBus()
    }

    override fun onVerseSelected(globalVerseId: Long) {
        loadGuide(globalVerseId)
    }

    override fun onToggleSection(section: GuideSection) {
        _state.update {
            val sections = it.expandedSections.toMutableSet()
            if (sections.contains(section)) sections.remove(section) else sections.add(section)
            it.copy(expandedSections = sections)
        }
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { event ->
                    loadGuide(event.globalVerseId.toLong())
                }
        }
    }

    private fun loadGuide(globalVerseId: Long) {
        scope.launch {
            _state.update { it.copy(globalVerseId = globalVerseId, isLoading = true, error = null) }

            val commentaries = commentaryRepository.getEntriesForVerse("", globalVerseId)
                .getOrDefault(emptyList())

            val crossRefs = crossRefRepository.getAllForVerse(globalVerseId)
                .getOrDefault(emptyList())

            val keyWords = wordStudyRepository.searchLexicon(globalVerseId.toString(), KEY_WORD_LIMIT)
                .getOrDefault(emptyList())

            _state.update {
                it.copy(
                    commentaries = commentaries,
                    crossReferences = crossRefs,
                    keyWords = keyWords,
                    isLoading = false
                )
            }
            Napier.d("Exegetical guide loaded for verse $globalVerseId")
        }
    }

    companion object {
        private const val KEY_WORD_LIMIT = 20L
    }
}
