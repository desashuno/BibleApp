package org.biblestudio.ui

import io.github.aakira.napier.Napier
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.core.pane_registry.PaneType

/**
 * Replaces placeholder [PaneRegistry] builders with real implementations
 * for all Phase 3 pane types.
 *
 * Called once at application startup after [PaneRegistry.init] has registered
 * the 21 placeholder entries.  Each builder logs its activation and can
 * perform lightweight pane-specific initialisation (the heavy composable
 * rendering is driven by the workspace layout system, not by these builders).
 */
@Suppress("TooManyFunctions")
object PaneRegistration {

    /**
     * Registers concrete builders for every Phase-3 pane type.
     */
    fun registerAll() {
        registerBibleReader()
        registerTextComparison()
        registerSearch()
        registerKnowledgeGraph()
        registerTimeline()
        registerTheologicalAtlas()
        registerAudioSync()
        registerCrossReferences()
        registerModuleManager()
        registerSettings()
        registerImportExport()
        registerWordStudy()
        registerInterlinear()
        registerReverseInterlinear()
        registerPassageGuide()
        registerNoteEditor()
        registerHighlights()
        registerBookmarks()
        registerSermonEditor()
        registerReadingPlans()
        registerDashboard()
        registerExegeticalGuide()
        registerWorship()

        Napier.i("PaneRegistration: replaced ${PANE_COUNT} placeholder builders with real implementations")
    }

    private fun registerBibleReader() {
        val meta = PaneRegistry.metadata(PaneType.BIBLE_READER)
        PaneRegistry.register(PaneType.BIBLE_READER, meta) { config ->
            Napier.d("BibleReaderPane activated with config=$config")
        }
    }

    private fun registerTextComparison() {
        val meta = PaneRegistry.metadata(PaneType.TEXT_COMPARISON)
        PaneRegistry.register(PaneType.TEXT_COMPARISON, meta) { config ->
            Napier.d("TextComparisonPane activated with config=$config")
        }
    }

    private fun registerSearch() {
        val meta = PaneRegistry.metadata(PaneType.SEARCH)
        PaneRegistry.register(PaneType.SEARCH, meta) { config ->
            Napier.d("SearchPane activated with config=$config")
        }
    }

    private fun registerKnowledgeGraph() {
        val meta = PaneRegistry.metadata(PaneType.KNOWLEDGE_GRAPH)
        PaneRegistry.register(PaneType.KNOWLEDGE_GRAPH, meta) { config ->
            Napier.d("KnowledgeGraphPane activated with config=$config")
        }
    }

    private fun registerTimeline() {
        val meta = PaneRegistry.metadata(PaneType.TIMELINE)
        PaneRegistry.register(PaneType.TIMELINE, meta) { config ->
            Napier.d("TimelinePane activated with config=$config")
        }
    }

    private fun registerTheologicalAtlas() {
        val meta = PaneRegistry.metadata(PaneType.THEOLOGICAL_ATLAS)
        PaneRegistry.register(PaneType.THEOLOGICAL_ATLAS, meta) { config ->
            Napier.d("TheologicalAtlasPane activated with config=$config")
        }
    }

    private fun registerAudioSync() {
        val meta = PaneRegistry.metadata(PaneType.AUDIO_SYNC)
        PaneRegistry.register(PaneType.AUDIO_SYNC, meta) { config ->
            Napier.d("AudioSyncPane activated with config=$config")
        }
    }

    private fun registerCrossReferences() {
        val meta = PaneRegistry.metadata(PaneType.CROSS_REFERENCES)
        PaneRegistry.register(PaneType.CROSS_REFERENCES, meta) { config ->
            Napier.d("CrossReferencePane activated with config=$config")
        }
    }

    private fun registerModuleManager() {
        // Resource library + module manager unified in Phase 9.
        val meta = PaneRegistry.metadata(PaneType.RESOURCE_LIBRARY)
        PaneRegistry.register(PaneType.RESOURCE_LIBRARY, meta) { config ->
            Napier.d("ResourceLibraryPane activated with config=$config")
        }
    }

    private fun registerSettings() {
        // Settings is a Decompose config screen, not a workspace pane,
        // but register for completeness.
        Napier.d("Settings pane uses Decompose RootConfig.Settings — no PaneRegistry builder needed")
    }

    private fun registerImportExport() {
        Napier.d("ImportExport pane uses Decompose RootConfig.Import — no PaneRegistry builder needed")
    }

    private fun registerWordStudy() {
        val meta = PaneRegistry.metadata(PaneType.WORD_STUDY)
        PaneRegistry.register(PaneType.WORD_STUDY, meta) { config ->
            Napier.d("WordStudyPane activated with config=$config")
        }
    }

    private fun registerInterlinear() {
        val meta = PaneRegistry.metadata(PaneType.MORPHOLOGY)
        PaneRegistry.register(PaneType.MORPHOLOGY, meta) { config ->
            Napier.d("InterlinearPane activated with config=$config")
        }
    }

    private fun registerReverseInterlinear() {
        val meta = PaneRegistry.metadata(PaneType.INTERLINEAR)
        PaneRegistry.register(PaneType.INTERLINEAR, meta) { config ->
            Napier.d("ReverseInterlinearPane activated with config=$config")
        }
    }

    private fun registerPassageGuide() {
        val meta = PaneRegistry.metadata(PaneType.PASSAGE_GUIDE)
        PaneRegistry.register(PaneType.PASSAGE_GUIDE, meta) { config ->
            Napier.d("PassageGuidePane activated with config=$config")
        }
    }

    private fun registerNoteEditor() {
        val meta = PaneRegistry.metadata(PaneType.NOTE_EDITOR)
        PaneRegistry.register(PaneType.NOTE_EDITOR, meta) { config ->
            Napier.d("NoteEditorPane activated with config=$config")
        }
    }

    private fun registerHighlights() {
        val meta = PaneRegistry.metadata(PaneType.HIGHLIGHTS)
        PaneRegistry.register(PaneType.HIGHLIGHTS, meta) { config ->
            Napier.d("HighlightsPane activated with config=$config")
        }
    }

    private fun registerBookmarks() {
        val meta = PaneRegistry.metadata(PaneType.BOOKMARKS)
        PaneRegistry.register(PaneType.BOOKMARKS, meta) { config ->
            Napier.d("BookmarksPane activated with config=$config")
        }
    }

    private fun registerSermonEditor() {
        val meta = PaneRegistry.metadata(PaneType.SERMON_EDITOR)
        PaneRegistry.register(PaneType.SERMON_EDITOR, meta) { config ->
            Napier.d("SermonEditorPane activated with config=$config")
        }
    }

    private fun registerReadingPlans() {
        val meta = PaneRegistry.metadata(PaneType.READING_PLANS)
        PaneRegistry.register(PaneType.READING_PLANS, meta) { config ->
            Napier.d("ReadingPlanPane activated with config=$config")
        }
    }

    private fun registerDashboard() {
        val meta = PaneRegistry.metadata(PaneType.DASHBOARD)
        PaneRegistry.register(PaneType.DASHBOARD, meta) { config ->
            Napier.d("DashboardPane activated with config=$config")
        }
    }

    private fun registerExegeticalGuide() {
        val meta = PaneRegistry.metadata(PaneType.EXEGETICAL_GUIDE)
        PaneRegistry.register(PaneType.EXEGETICAL_GUIDE, meta) { config ->
            Napier.d("ExegeticalGuidePane activated with config=$config")
        }
    }

    private fun registerWorship() {
        val meta = PaneRegistry.metadata(PaneType.WORSHIP)
        PaneRegistry.register(PaneType.WORSHIP, meta) { config ->
            Napier.d("WorshipPane activated with config=$config")
        }
    }

    private const val PANE_COUNT = 22
}
