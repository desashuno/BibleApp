package org.biblestudio.ui.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.pane_registry.PaneType
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.audio_sync.component.AudioSyncComponent
import org.biblestudio.features.bible_reader.component.BibleReaderComponent
import org.biblestudio.features.bible_reader.component.BibleReaderState
import org.biblestudio.features.bible_reader.component.TextComparisonComponent
import org.biblestudio.features.bible_reader.component.TextComparisonState
import org.biblestudio.features.bookmarks_history.component.BookmarksComponent
import org.biblestudio.features.cross_references.component.CrossReferenceComponent
import org.biblestudio.features.dashboard.component.DashboardComponent
import org.biblestudio.features.exegetical_guide.component.ExegeticalGuideComponent
import org.biblestudio.features.highlights.component.HighlightComponent
import org.biblestudio.features.knowledge_graph.component.KnowledgeGraphComponent
import org.biblestudio.features.morphology_interlinear.component.InterlinearComponent
import org.biblestudio.features.morphology_interlinear.component.ReverseInterlinearComponent
import org.biblestudio.features.note_editor.component.NoteEditorComponent
import org.biblestudio.features.passage_guide.component.PassageGuideComponent
import org.biblestudio.features.reading_plans.component.ReadingPlanComponent
import org.biblestudio.features.resource_library.component.ResourceLibraryComponent
import org.biblestudio.features.search.component.SearchComponent
import org.biblestudio.features.sermon_editor.component.SermonEditorComponent
import org.biblestudio.features.settings.component.SettingsComponent
import org.biblestudio.features.theological_atlas.component.AtlasComponent
import org.biblestudio.features.timeline.component.TimelineComponent
import org.biblestudio.features.word_study.component.WordStudyComponent
import org.biblestudio.features.worship.component.WorshipComponent
import org.biblestudio.ui.panes.AudioSyncPane
import org.biblestudio.ui.panes.BibleReaderPane
import org.biblestudio.ui.panes.BookmarksPane
import org.biblestudio.ui.panes.CrossReferencePane
import org.biblestudio.ui.panes.DashboardPane
import org.biblestudio.ui.panes.ExegeticalGuidePane
import org.biblestudio.ui.panes.HighlightsPane
import org.biblestudio.ui.panes.InterlinearPane
import org.biblestudio.ui.panes.KnowledgeGraphPane
import org.biblestudio.ui.panes.NoteEditorPane
import org.biblestudio.ui.panes.PassageGuidePane
import org.biblestudio.ui.panes.ReadingPlanPane
import org.biblestudio.ui.panes.ResourceLibraryPane
import org.biblestudio.ui.panes.ReverseInterlinearPane
import org.biblestudio.ui.panes.SearchPane
import org.biblestudio.ui.panes.SermonEditorPane
import org.biblestudio.ui.panes.SettingsScreen
import org.biblestudio.ui.panes.SyntaxSearchPane
import org.biblestudio.ui.panes.TextComparisonPane
import org.biblestudio.ui.panes.TheologicalAtlasPane
import org.biblestudio.ui.panes.TimelinePane
import org.biblestudio.ui.panes.WordStudyPane
import org.biblestudio.ui.panes.WorshipPane
import org.biblestudio.ui.theme.BibleReaderSettings
import org.biblestudio.ui.theme.IconSize
import org.biblestudio.ui.theme.LocalAppFontSize
import org.biblestudio.ui.theme.LocalContinuousScroll
import org.biblestudio.ui.theme.LocalParagraphMode
import org.biblestudio.ui.theme.LocalRedLetter
import org.biblestudio.ui.theme.LocalShowVerseNumbers
import org.biblestudio.ui.theme.ProvideBibleReaderSettings
import org.biblestudio.ui.theme.Spacing
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.parametersOf

/**
 * Dispatches a pane type string to its real Composable, creating a Decompose
 * [ComponentContext] with proper lifecycle management for each pane instance.
 *
 * Each pane gets its own component from Koin, connected to its own lifecycle.
 * When the composable leaves the composition (or paneType changes), the
 * lifecycle is destroyed and the component's coroutine scope is cancelled.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "CyclomaticComplexMethod", "ReturnCount")
@Composable
fun PaneContent(paneType: String, modifier: Modifier = Modifier) {
    val lifecycle = remember(paneType) {
        LifecycleRegistry().apply {
            onCreate()
            onStart()
            onResume()
        }
    }
    val componentContext = remember(paneType) {
        DefaultComponentContext(lifecycle = lifecycle)
    }

    DisposableEffect(paneType) {
        onDispose {
            lifecycle.onPause()
            lifecycle.onStop()
            lifecycle.onDestroy()
        }
    }

    val koin = remember {
        try {
            GlobalContext.get()
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException")
            e: Exception
        ) {
            null
        }
    }
    var initError by remember(paneType) { mutableStateOf<String?>(null) }

    if (koin == null) {
        PaneErrorFallback(paneType = paneType, errorMessage = "DI not initialized", modifier = modifier)
        return
    }

    if (initError != null) {
        PaneErrorFallback(paneType = paneType, errorMessage = initError!!, modifier = modifier)
        return
    }

    when (paneType) {
        PaneType.BIBLE_READER -> {
            val c = rememberSafeComponent<BibleReaderComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            // React to continuous scroll setting changes
            val continuousScroll = LocalContinuousScroll.current
            LaunchedEffect(continuousScroll) {
                c.setContinuousScroll(continuousScroll)
            }
            val bibleState by c.state.collectAsState()
            val toolbar: @Composable () -> Unit = {
                BibleReaderToolbar(
                    stateFlow = c.state,
                    onToggleBookPicker = c::toggleBookPicker,
                    onSelectBible = c::selectBible,
                    onPreviousChapter = c::previousChapter,
                    onNextChapter = c::nextChapter,
                    onPreviousVerse = c::previousVerse,
                    onNextVerse = c::nextVerse,
                    onToggleReaderToolbar = c::toggleReaderToolbar
                )
            }
            CompositionLocalProvider(LocalPaneToolbar provides toolbar) {
                ProvideBibleReaderSettings(
                    BibleReaderSettings(
                        fontSize = bibleState.fontSize ?: LocalAppFontSize.current,
                        showVerseNumbers = bibleState.showVerseNumbers ?: LocalShowVerseNumbers.current,
                        redLetter = bibleState.redLetter ?: LocalRedLetter.current,
                        paragraphMode = bibleState.paragraphMode ?: LocalParagraphMode.current,
                        continuousScroll = LocalContinuousScroll.current,
                    )
                ) {
                    BibleReaderPane(
                        stateFlow = c.state,
                        onVerseTapped = c::onVerseTapped,
                        onVerseLongPressed = c::onVerseLongPressed,
                        onBookChapterSelected = c::goToChapter,
                        onToggleBookPicker = c::toggleBookPicker,
                        onCopySelection = c::getSelectedVerseText,
                        onClearSelection = c::clearSelection,
                        onHighlightSelection = c::highlightSelection,
                        onBookmarkVerse = c::bookmarkVerse,
                        onWordTapped = c::onWordTapped,
                        onSearchWord = c::searchWord,
                        onStudyWord = c::studyWord,
                        onDismissWordPopup = c::dismissWordPopup,
                        onToggleShowVerseNumbers = c::toggleShowVerseNumbers,
                        onToggleRedLetter = c::toggleRedLetter,
                        onToggleParagraphMode = c::toggleParagraphMode,
                        onToggleContinuousScroll = { c.setContinuousScroll(!bibleState.continuousScroll) },
                        onAdjustFontSize = c::adjustFontSize,
                        onCrossReferenceTapped = c::onCrossReferenceTapped,
                        modifier = modifier
                    )
                }
            }
        }

        PaneType.TEXT_COMPARISON -> {
            val c = rememberSafeComponent<TextComparisonComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            val toolbar: @Composable () -> Unit = {
                TextComparisonToolbar(
                    stateFlow = c.state,
                    onPreviousVerse = c::previousVerse,
                    onNextVerse = c::nextVerse,
                    onCopy = c::copyComparisonText
                )
            }
            CompositionLocalProvider(LocalPaneToolbar provides toolbar) {
                TextComparisonPane(
                    stateFlow = c.state,
                    onViewModeChanged = c::setViewMode,
                    onSelectedVersionsChanged = c::setSelectedVersions,
                    modifier = modifier
                )
            }
        }

        PaneType.SEARCH -> {
            val c = rememberSafeComponent<SearchComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            SearchPane(
                stateFlow = c.state,
                onQueryChanged = c::onQueryChanged,
                onSearch = c::search,
                onScopeChanged = c::setScope,
                onResultTapped = c::onResultTapped,
                onClearHistory = c::clearHistory,
                modifier = modifier
            )
        }

        PaneType.SYNTAX_SEARCH -> {
            val c = rememberSafeComponent<SearchComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            SyntaxSearchPane(
                stateFlow = c.state,
                onQueryChanged = c::onQueryChanged,
                onSearch = c::search,
                onResultTapped = c::onResultTapped,
                modifier = modifier
            )
        }

        PaneType.CROSS_REFERENCES -> {
            val c = rememberSafeComponent<CrossReferenceComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            CrossReferencePane(
                stateFlow = c.state,
                onReferenceTapped = c::onReferenceTapped,
                onToggleExpansion = c::toggleExpansion,
                modifier = modifier
            )
        }

        PaneType.WORD_STUDY -> {
            val c = rememberSafeComponent<WordStudyComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            WordStudyPane(
                stateFlow = c.state,
                onOccurrenceSelected = c::onOccurrenceSelected,
                modifier = modifier
            )
        }

        PaneType.MORPHOLOGY -> {
            val c = rememberSafeComponent<InterlinearComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            InterlinearPane(
                stateFlow = c.state,
                onWordSelected = c::onWordSelected,
                onDisplayModeChanged = c::onDisplayModeChanged,
                modifier = modifier
            )
        }

        PaneType.INTERLINEAR -> {
            val c = rememberSafeComponent<ReverseInterlinearComponent>(
                paneType, koin, { parametersOf(componentContext) }
            ) {
                initError = it
            } ?: return
            ReverseInterlinearPane(
                stateFlow = c.state,
                onTokenSelected = c::onTokenSelected,
                onClearSelection = c::clearSelection,
                modifier = modifier
            )
        }

        PaneType.PASSAGE_GUIDE -> {
            val c = rememberSafeComponent<PassageGuideComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            PassageGuidePane(
                stateFlow = c.state,
                onRefSelected = c::onRefSelected,
                onWordSelected = c::onWordSelected,
                onSectionToggle = c::onSectionToggle,
                modifier = modifier
            )
        }

        PaneType.KNOWLEDGE_GRAPH -> {
            val c = rememberSafeComponent<KnowledgeGraphComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            KnowledgeGraphPane(
                stateFlow = c.state,
                onEntitySelected = c::onEntitySelected,
                onDepthChanged = c::onDepthChanged,
                onSearch = c::onSearch,
                onClearSelection = c::onClearSelection,
                modifier = modifier
            )
        }

        PaneType.TIMELINE -> {
            val c = rememberSafeComponent<TimelineComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            TimelinePane(
                stateFlow = c.state,
                onEventSelected = c::onEventSelected,
                onZoomChanged = c::onZoomChanged,
                onScrollToYear = c::onScrollToYear,
                onCategoryFilter = c::onCategoryFilter,
                onClearSelection = c::onClearSelection,
                modifier = modifier
            )
        }

        PaneType.THEOLOGICAL_ATLAS -> {
            val c = rememberSafeComponent<AtlasComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            TheologicalAtlasPane(
                stateFlow = c.state,
                onLocationSelected = c::onLocationSelected,
                onMapMoved = c::onMapMoved,
                onSearch = c::onSearch,
                onRegionSelected = c::onRegionSelected,
                onClearSelection = c::onClearSelection,
                modifier = modifier
            )
        }

        PaneType.AUDIO_SYNC -> {
            val c = rememberSafeComponent<AudioSyncComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            AudioSyncPane(
                stateFlow = c.state,
                onPlay = c::onPlay,
                onPause = c::onPause,
                onStop = c::onStop,
                onSeek = c::onSeek,
                onJumpToVerse = c::onJumpToVerse,
                modifier = modifier
            )
        }

        PaneType.NOTE_EDITOR -> {
            val c = rememberSafeComponent<NoteEditorComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            NoteEditorPane(
                stateFlow = c.state,
                onNoteSelected = c::onNoteSelected,
                onTitleChanged = c::onTitleChanged,
                onContentChanged = c::onContentChanged,
                onNewNote = c::onNewNote,
                onDeleteNote = c::onDeleteNote,
                onSearchQueryChanged = c::onSearchQueryChanged,
                modifier = modifier
            )
        }

        PaneType.HIGHLIGHTS -> {
            val c = rememberSafeComponent<HighlightComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            HighlightsPane(
                stateFlow = c.state,
                onColorSelected = c::onColorSelected,
                onDeleteHighlight = c::onDeleteHighlight,
                modifier = modifier
            )
        }

        PaneType.BOOKMARKS -> {
            val c = rememberSafeComponent<BookmarksComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            BookmarksPane(
                stateFlow = c.state,
                onBookmarkTapped = c::onBookmarkTapped,
                onFolderSelected = c::onFolderSelected,
                onCreateFolder = c::onCreateFolder,
                onDeleteBookmark = c::onDeleteBookmark,
                onDeleteFolder = c::onDeleteFolder,
                onHistoryTapped = c::onHistoryTapped,
                onViewModeChanged = c::onViewModeChanged,
                onClearHistory = c::onClearHistory,
                modifier = modifier
            )
        }

        PaneType.SERMON_EDITOR -> {
            val c = rememberSafeComponent<SermonEditorComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            SermonEditorPane(
                stateFlow = c.state,
                onSermonSelected = c::onSermonSelected,
                onTitleChanged = c::onTitleChanged,
                onScriptureRefChanged = c::onScriptureRefChanged,
                onNewSermon = c::onNewSermon,
                onDeleteSermon = c::onDeleteSermon,
                onSectionContentChanged = c::onSectionContentChanged,
                onAddSection = c::onAddSection,
                onDeleteSection = c::onDeleteSection,
                onMoveSectionUp = c::onMoveSectionUp,
                onMoveSectionDown = c::onMoveSectionDown,
                onSearchQueryChanged = c::onSearchQueryChanged,
                modifier = modifier
            )
        }

        PaneType.READING_PLANS -> {
            val c = rememberSafeComponent<ReadingPlanComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            ReadingPlanPane(
                stateFlow = c.state,
                onPlanSelected = c::onPlanSelected,
                onMarkDayCompleted = c::onMarkDayCompleted,
                onDeletePlan = c::onDeletePlan,
                modifier = modifier
            )
        }

        PaneType.RESOURCE_LIBRARY, PaneType.MODULE_MANAGER -> {
            val c = rememberSafeComponent<ResourceLibraryComponent>(
                paneType, koin, { parametersOf(componentContext) }
            ) {
                initError = it
            } ?: return
            ResourceLibraryPane(
                stateFlow = c.state,
                onModuleSelected = c::onModuleSelected,
                onInstallModule = c::onInstallModule,
                onRemoveModule = c::onRemoveModule,
                onCancelDownload = c::onCancelDownload,
                onFilterTypeChanged = c::onFilterTypeChanged,
                onSearchQueryChanged = c::onSearchQueryChanged,
                onToggleModuleActive = c::onToggleModuleActive,
                modifier = modifier
            )
        }

        PaneType.DASHBOARD -> {
            val c = rememberSafeComponent<DashboardComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            DashboardPane(
                stateFlow = c.state,
                onContinueReading = c::onContinueReading,
                modifier = modifier
            )
        }

        PaneType.EXEGETICAL_GUIDE -> {
            val c = rememberSafeComponent<ExegeticalGuideComponent>(
                paneType, koin, { parametersOf(componentContext) }
            ) {
                initError = it
            } ?: return
            ExegeticalGuidePane(
                stateFlow = c.state,
                onToggleSection = c::onToggleSection,
                modifier = modifier
            )
        }

        PaneType.WORSHIP -> {
            val c = rememberSafeComponent<WorshipComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            WorshipPane(
                stateFlow = c.state,
                onTabSelected = c::onTabSelected,
                onSearchQueryChanged = c::onSearchQueryChanged,
                onSongSelected = c::onSongSelected,
                onPlayAll = c::onPlayAll,
                onToggleFavorite = c::onToggleFavorite,
                onCreatePlaylist = c::onCreatePlaylist,
                onDeletePlaylist = c::onDeletePlaylist,
                onClearHistory = c::onClearHistory,
                modifier = modifier
            )
        }

        PaneType.SETTINGS -> {
            val c = rememberSafeComponent<SettingsComponent>(paneType, koin, { parametersOf(componentContext) }) {
                initError = it
            } ?: return
            SettingsScreen(
                stateFlow = c.state,
                onFontSizeChanged = c::setFontSize,
                onThemeChanged = c::setTheme,
                onDefaultBibleChanged = c::setDefaultBible,
                onSaveLayout = c::saveLayout,
                onDeleteLayout = c::deleteLayout,
                onActivateLayout = c::activateLayout,
                onShowVerseNumbersChanged = c::setShowVerseNumbers,
                onRedLetterChanged = c::setRedLetter,
                onParagraphModeChanged = c::setParagraphMode,
                onContinuousScrollChanged = c::setContinuousScroll,
                onSidebarCollapsedChanged = c::setSidebarCollapsed,
                modifier = modifier
            )
        }

        else -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unknown pane: $paneType",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Safely creates a Koin component, catching any resolution errors.
 * Returns null and invokes [onError] if Koin fails to resolve the component.
 */
@Composable
private inline fun <reified T : Any> rememberSafeComponent(
    paneType: String,
    koin: Koin,
    noinline parameters: () -> ParametersHolder,
    crossinline onError: (String) -> Unit
): T? {
    return remember(paneType) {
        try {
            koin.get<T>(parameters = parameters)
        } catch (
            @Suppress("TooGenericExceptionCaught")
            e: Exception
        ) {
            Napier.e("Failed to create component for pane '$paneType'", e)
            onError("${e::class.simpleName}: ${e.message}")
            null
        }
    }
}

private val TOOLBAR_ICON_SIZE = IconSize.Medium
private val TOOLBAR_BUTTON_SIZE = IconSize.Large

/**
 * Toolbar for the Bible Reader pane header.
 * Shows book picker toggle, Bible version dropdown, chapter nav arrows, and breadcrumb.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun BibleReaderToolbar(
    stateFlow: StateFlow<BibleReaderState>,
    onToggleBookPicker: () -> Unit,
    onSelectBible: (Long) -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onPreviousVerse: () -> Unit,
    onNextVerse: () -> Unit,
    onToggleReaderToolbar: (() -> Unit)? = null,
) {
    val state by stateFlow.collectAsState()
    var bibleDropdownExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Book picker toggle
        IconButton(
            onClick = onToggleBookPicker,
            modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
        ) {
            Icon(
                imageVector = Icons.Default.MenuOpen,
                contentDescription = "Book picker",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(TOOLBAR_ICON_SIZE)
            )
        }

        // Breadcrumb: "Book Chapter"
        val book = state.currentBook
        if (book != null) {
            Spacer(Modifier.width(Spacing.Space4))
            Text(
                text = "${book.name} ${state.currentChapter}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(Spacing.Space4))

        // Previous chapter
        IconButton(
            onClick = onPreviousChapter,
            enabled = state.currentChapter > 1,
            modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous chapter",
                modifier = Modifier.size(TOOLBAR_ICON_SIZE)
            )
        }

        // Next chapter
        IconButton(
            onClick = onNextChapter,
            enabled = state.currentChapter < state.chapterCount,
            modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next chapter",
                modifier = Modifier.size(TOOLBAR_ICON_SIZE)
            )
        }

        // Verse navigation chip
        if (state.verses.isNotEmpty()) {
            Spacer(Modifier.width(Spacing.Space4))
            IconButton(
                onClick = onPreviousVerse,
                enabled = state.currentVerseIndex > 0,
                modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous verse",
                    modifier = Modifier.size(TOOLBAR_ICON_SIZE)
                )
            }
            IconButton(
                onClick = onNextVerse,
                enabled = state.currentVerseIndex < state.verses.size - 1,
                modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next verse",
                    modifier = Modifier.size(TOOLBAR_ICON_SIZE)
                )
            }
        }

        Spacer(Modifier.width(Spacing.Space4))

        // Bible version dropdown
        if (state.bibles.isNotEmpty()) {
            Box {
                IconButton(
                    onClick = { bibleDropdownExpanded = true },
                    modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.currentBible?.abbreviation ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Bible version",
                            modifier = Modifier.size(IconSize.ExtraSmall)
                        )
                    }
                }
                DropdownMenu(
                    expanded = bibleDropdownExpanded,
                    onDismissRequest = { bibleDropdownExpanded = false }
                ) {
                    val byLanguage = state.bibles.groupBy { it.language }
                    byLanguage.forEach { (lang, biblesForLang) ->
                        if (byLanguage.size > 1) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = lang.uppercase(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                        }
                        biblesForLang.forEach { bible ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${bible.abbreviation} — ${bible.name}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                onClick = {
                                    onSelectBible(bible.id)
                                    bibleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Reader toolbar toggle button
        if (onToggleReaderToolbar != null) {
            Spacer(Modifier.width(Spacing.Space4))
            IconButton(
                onClick = onToggleReaderToolbar,
                modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Toggle reader toolbar",
                    tint = if (state.showReaderToolbar) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(TOOLBAR_ICON_SIZE)
                )
            }
        }
    }
}

/**
 * Compact toolbar for the Text Comparison pane header.
 * Shows verse reference, prev/next verse arrows, version count, and copy button.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
private fun TextComparisonToolbar(
    stateFlow: StateFlow<TextComparisonState>,
    onPreviousVerse: () -> Unit,
    onNextVerse: () -> Unit,
    onCopy: (() -> String)? = null
) {
    val state by stateFlow.collectAsState()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Verse reference
        state.comparison?.let { cmp ->
            Text(
                text = VerseRefFormatter.format(cmp.globalVerseId),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(Spacing.Space4))

        // Previous verse
        IconButton(
            onClick = onPreviousVerse,
            enabled = state.comparison != null,
            modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous verse",
                modifier = Modifier.size(TOOLBAR_ICON_SIZE)
            )
        }

        // Next verse
        IconButton(
            onClick = onNextVerse,
            enabled = state.comparison != null,
            modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next verse",
                modifier = Modifier.size(TOOLBAR_ICON_SIZE)
            )
        }

        // Version count badge
        if (state.selectedVersions.isNotEmpty()) {
            Spacer(Modifier.width(Spacing.Space4))
            Text(
                text = "${state.selectedVersions.size}v",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Copy button
        if (onCopy != null && state.comparison != null) {
            Spacer(Modifier.width(Spacing.Space4))
            IconButton(
                onClick = {
                    val text = onCopy()
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                },
                modifier = Modifier.size(TOOLBAR_BUTTON_SIZE)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy comparison",
                    modifier = Modifier.size(TOOLBAR_ICON_SIZE)
                )
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PaneErrorFallback(paneType: String, errorMessage: String, modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Failed to load: $paneType",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(Spacing.Space8))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
