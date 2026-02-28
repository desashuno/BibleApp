package org.biblestudio.di

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.navigation.DefaultRootComponent
import org.biblestudio.core.navigation.RootComponent
import org.biblestudio.core.navigation.RootConfig
import org.biblestudio.features.bible_reader.component.BibleReaderComponent
import org.biblestudio.features.bible_reader.component.DefaultBibleReaderComponent
import org.biblestudio.features.bible_reader.component.DefaultTextComparisonComponent
import org.biblestudio.features.bible_reader.component.TextComparisonComponent
import org.biblestudio.features.bookmarks_history.component.BookmarksComponent
import org.biblestudio.features.bookmarks_history.component.DefaultBookmarksComponent
import org.biblestudio.features.cross_references.component.CrossReferenceComponent
import org.biblestudio.features.cross_references.component.DefaultCrossReferenceComponent
import org.biblestudio.features.dashboard.component.DashboardComponent
import org.biblestudio.features.dashboard.component.DefaultDashboardComponent
import org.biblestudio.features.exegetical_guide.component.DefaultExegeticalGuideComponent
import org.biblestudio.features.exegetical_guide.component.ExegeticalGuideComponent
import org.biblestudio.features.highlights.component.DefaultHighlightComponent
import org.biblestudio.features.highlights.component.HighlightComponent
import org.biblestudio.features.knowledge_graph.component.DefaultKnowledgeGraphComponent
import org.biblestudio.features.knowledge_graph.component.KnowledgeGraphComponent
import org.biblestudio.features.audio_sync.component.AudioSyncComponent
import org.biblestudio.features.audio_sync.component.DefaultAudioSyncComponent
import org.biblestudio.features.theological_atlas.component.AtlasComponent
import org.biblestudio.features.theological_atlas.component.DefaultAtlasComponent
import org.biblestudio.features.timeline.component.DefaultTimelineComponent
import org.biblestudio.features.timeline.component.TimelineComponent
import org.biblestudio.features.import_export.component.DefaultImportExportComponent
import org.biblestudio.features.import_export.component.ImportExportComponent
import org.biblestudio.features.module_system.component.DefaultModuleManagerComponent
import org.biblestudio.features.module_system.component.ModuleManagerComponent
import org.biblestudio.features.morphology_interlinear.component.DefaultInterlinearComponent
import org.biblestudio.features.morphology_interlinear.component.DefaultReverseInterlinearComponent
import org.biblestudio.features.morphology_interlinear.component.InterlinearComponent
import org.biblestudio.features.morphology_interlinear.component.ReverseInterlinearComponent
import org.biblestudio.features.morphology_interlinear.domain.ParsingDecoder
import org.biblestudio.features.note_editor.component.DefaultNoteEditorComponent
import org.biblestudio.features.note_editor.component.NoteEditorComponent
import org.biblestudio.features.passage_guide.component.DefaultPassageGuideComponent
import org.biblestudio.features.passage_guide.component.PassageGuideComponent
import org.biblestudio.features.reading_plans.component.DefaultReadingPlanComponent
import org.biblestudio.features.reading_plans.component.ReadingPlanComponent
import org.biblestudio.features.resource_library.component.DefaultResourceLibraryComponent
import org.biblestudio.features.resource_library.component.ResourceLibraryComponent
import org.biblestudio.features.search.component.DefaultSearchComponent
import org.biblestudio.features.search.component.SearchComponent
import org.biblestudio.features.sermon_editor.component.DefaultSermonEditorComponent
import org.biblestudio.features.sermon_editor.component.SermonEditorComponent
import org.biblestudio.features.settings.component.DefaultSettingsComponent
import org.biblestudio.features.settings.component.SettingsComponent
import org.biblestudio.features.word_study.component.DefaultWordStudyComponent
import org.biblestudio.features.word_study.component.WordStudyComponent
import org.biblestudio.features.workspace.component.DefaultWorkspaceComponent
import org.biblestudio.features.workspace.component.WorkspaceComponent
import org.koin.dsl.module

/**
 * Component DI module — Decompose component factories.
 *
 * Components that need a [ComponentContext] are registered as factories
 * because each navigation event creates a fresh context.
 */
val componentModule = module {
    factory<RootComponent> { (componentContext: ComponentContext, initialConfig: RootConfig) ->
        DefaultRootComponent(
            componentContext = componentContext,
            verseBus = get(),
            initialConfig = initialConfig
        )
    }

    factory<WorkspaceComponent> { (componentContext: ComponentContext) ->
        DefaultWorkspaceComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<BibleReaderComponent> { (componentContext: ComponentContext) ->
        DefaultBibleReaderComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get(),
            highlightRepository = get(),
            crossRefRepository = get(),
            morphologyRepository = get(),
            settingsRepository = get()
        )
    }

    factory<TextComparisonComponent> { (componentContext: ComponentContext) ->
        DefaultTextComparisonComponent(
            componentContext = componentContext,
            repository = get(),
            bibleRepository = get(),
            verseBus = get()
        )
    }

    factory<SearchComponent> { (componentContext: ComponentContext) ->
        DefaultSearchComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<CrossReferenceComponent> { (componentContext: ComponentContext) ->
        DefaultCrossReferenceComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<SettingsComponent> { (componentContext: ComponentContext) ->
        DefaultSettingsComponent(
            componentContext = componentContext,
            repository = get()
        )
    }

    factory<ModuleManagerComponent> { (componentContext: ComponentContext) ->
        DefaultModuleManagerComponent(
            componentContext = componentContext,
            repository = get()
        )
    }

    factory<ImportExportComponent> { (componentContext: ComponentContext) ->
        DefaultImportExportComponent(
            componentContext = componentContext,
            repository = get()
        )
    }

    factory<WordStudyComponent> { (componentContext: ComponentContext) ->
        DefaultWordStudyComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    single { ParsingDecoder() }

    factory<InterlinearComponent> { (componentContext: ComponentContext) ->
        DefaultInterlinearComponent(
            componentContext = componentContext,
            repository = get(),
            parsingDecoder = get(),
            verseBus = get()
        )
    }

    factory<ReverseInterlinearComponent> { (componentContext: ComponentContext) ->
        DefaultReverseInterlinearComponent(
            componentContext = componentContext,
            repository = get(),
            parsingDecoder = get(),
            verseBus = get()
        )
    }

    factory<PassageGuideComponent> { (componentContext: ComponentContext) ->
        DefaultPassageGuideComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<ResourceLibraryComponent> { (componentContext: ComponentContext) ->
        DefaultResourceLibraryComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<NoteEditorComponent> { (componentContext: ComponentContext) ->
        DefaultNoteEditorComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<HighlightComponent> { (componentContext: ComponentContext) ->
        DefaultHighlightComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<BookmarksComponent> { (componentContext: ComponentContext) ->
        DefaultBookmarksComponent(
            componentContext = componentContext,
            bookmarkRepository = get(),
            historyRepository = get(),
            verseBus = get()
        )
    }

    factory<SermonEditorComponent> { (componentContext: ComponentContext) ->
        DefaultSermonEditorComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<ReadingPlanComponent> { (componentContext: ComponentContext) ->
        DefaultReadingPlanComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<DashboardComponent> { (componentContext: ComponentContext) ->
        DefaultDashboardComponent(
            componentContext = componentContext,
            noteRepository = get(),
            highlightRepository = get(),
            bookmarkRepository = get(),
            sermonRepository = get(),
            readingPlanRepository = get(),
            historyRepository = get(),
            verseBus = get(),
            bibleRepository = get()
        )
    }

    factory<ExegeticalGuideComponent> { (componentContext: ComponentContext) ->
        DefaultExegeticalGuideComponent(
            componentContext = componentContext,
            commentaryRepository = get(),
            crossRefRepository = get(),
            wordStudyRepository = get(),
            verseBus = get()
        )
    }

    factory<KnowledgeGraphComponent> { (componentContext: ComponentContext) ->
        DefaultKnowledgeGraphComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<TimelineComponent> { (componentContext: ComponentContext) ->
        DefaultTimelineComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<AtlasComponent> { (componentContext: ComponentContext) ->
        DefaultAtlasComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get()
        )
    }

    factory<AudioSyncComponent> { (componentContext: ComponentContext) ->
        DefaultAudioSyncComponent(
            componentContext = componentContext,
            repository = get(),
            verseBus = get(),
            settingsRepository = get()
        )
    }
}
