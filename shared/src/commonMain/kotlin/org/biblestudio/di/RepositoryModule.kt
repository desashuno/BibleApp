package org.biblestudio.di

import org.biblestudio.features.bible_reader.data.repositories.BibleRepositoryImpl
import org.biblestudio.features.bible_reader.data.repositories.TextComparisonRepositoryImpl
import org.biblestudio.features.bible_reader.domain.repositories.BibleRepository
import org.biblestudio.features.bible_reader.domain.repositories.TextComparisonRepository
import org.biblestudio.features.bookmarks_history.data.repositories.BookmarkRepositoryImpl
import org.biblestudio.features.bookmarks_history.data.repositories.HistoryRepositoryImpl
import org.biblestudio.features.bookmarks_history.domain.repositories.BookmarkRepository
import org.biblestudio.features.bookmarks_history.domain.repositories.HistoryRepository
import org.biblestudio.features.cross_references.data.repositories.CrossRefRepositoryImpl
import org.biblestudio.features.cross_references.data.repositories.ParallelRepositoryImpl
import org.biblestudio.features.cross_references.domain.repositories.CrossRefRepository
import org.biblestudio.features.cross_references.domain.repositories.ParallelRepository
import org.biblestudio.features.exegetical_guide.data.repositories.CommentaryRepositoryImpl
import org.biblestudio.features.exegetical_guide.domain.repositories.CommentaryRepository
import org.biblestudio.features.highlights.data.repositories.HighlightRepositoryImpl
import org.biblestudio.features.highlights.domain.repositories.HighlightRepository
import org.biblestudio.features.import_export.data.repositories.ImportExportRepositoryImpl
import org.biblestudio.features.import_export.domain.repositories.ImportExportRepository
import org.biblestudio.features.module_system.data.repositories.ModuleRepositoryImpl
import org.biblestudio.features.module_system.domain.repositories.ModuleRepository
import org.biblestudio.features.morphology_interlinear.data.repositories.MorphologyRepositoryImpl
import org.biblestudio.features.morphology_interlinear.domain.repositories.MorphologyRepository
import org.biblestudio.features.note_editor.data.repositories.NoteRepositoryImpl
import org.biblestudio.features.note_editor.domain.repositories.NoteRepository
import org.biblestudio.features.passage_guide.data.repositories.PassageGuideRepositoryImpl
import org.biblestudio.features.passage_guide.domain.repositories.PassageGuideRepository
import org.biblestudio.features.reading_plans.data.repositories.ReadingPlanRepositoryImpl
import org.biblestudio.features.reading_plans.domain.repositories.ReadingPlanRepository
import org.biblestudio.features.resource_library.data.repositories.ResourceRepositoryImpl
import org.biblestudio.features.resource_library.domain.repositories.ResourceRepository
import org.biblestudio.features.search.data.repositories.SearchRepositoryImpl
import org.biblestudio.features.search.domain.repositories.SearchRepository
import org.biblestudio.features.sermon_editor.data.repositories.SermonRepositoryImpl
import org.biblestudio.features.sermon_editor.domain.repositories.SermonRepository
import org.biblestudio.features.settings.data.repositories.SettingsRepositoryImpl
import org.biblestudio.features.settings.domain.repositories.SettingsRepository
import org.biblestudio.features.word_study.data.repositories.DictionaryRepositoryImpl
import org.biblestudio.features.word_study.data.repositories.WordStudyRepositoryImpl
import org.biblestudio.features.word_study.domain.repositories.DictionaryRepository
import org.biblestudio.features.word_study.domain.repositories.WordStudyRepository
import org.biblestudio.features.workspace.data.repositories.WorkspaceRepositoryImpl
import org.biblestudio.features.workspace.domain.repositories.WorkspaceRepository
import org.koin.dsl.module

/**
 * Repository DI module — binds each repository interface to its SQLDelight-backed implementation.
 * All implementations receive [BibleStudioDatabase] from [coreModule].
 */
val repositoryModule = module {
    single<BibleRepository> { BibleRepositoryImpl(get()) }
    single<TextComparisonRepository> { TextComparisonRepositoryImpl(get()) }
    single<NoteRepository> { NoteRepositoryImpl(get()) }
    single<HighlightRepository> { HighlightRepositoryImpl(get()) }
    single<BookmarkRepository> { BookmarkRepositoryImpl(get()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get()) }
    single<WordStudyRepository> { WordStudyRepositoryImpl(get()) }
    single<MorphologyRepository> { MorphologyRepositoryImpl(get()) }
    single<ResourceRepository> { ResourceRepositoryImpl(get()) }
    single<CommentaryRepository> { CommentaryRepositoryImpl(get()) }
    single<DictionaryRepository> { DictionaryRepositoryImpl(get()) }
    single<SermonRepository> { SermonRepositoryImpl(get()) }
    single<CrossRefRepository> { CrossRefRepositoryImpl(get()) }
    single<ParallelRepository> { ParallelRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<WorkspaceRepository> { WorkspaceRepositoryImpl(get()) }
    single<SearchRepository> { SearchRepositoryImpl(get()) }
    single<ReadingPlanRepository> { ReadingPlanRepositoryImpl(get()) }
    single<ModuleRepository> { ModuleRepositoryImpl(get()) }
    single<ImportExportRepository> { ImportExportRepositoryImpl(get()) }
    single<PassageGuideRepository> {
        PassageGuideRepositoryImpl(
            database = get(),
            bibleRepository = get(),
            crossRefRepository = get(),
            morphologyRepository = get(),
            wordStudyRepository = get(),
            resourceRepository = get(),
            noteRepository = get()
        )
    }
}
