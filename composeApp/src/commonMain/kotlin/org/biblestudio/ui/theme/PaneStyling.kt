package org.biblestudio.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.biblestudio.core.pane_registry.PaneCategory
import org.biblestudio.core.pane_registry.PaneRegistry
import org.biblestudio.core.pane_registry.PaneType

/**
 * Global aesthetics utility for all panes and workspace UI.
 *
 * Provides a single source of truth for:
 * - Category → accent colour mapping
 * - Icon-name string → Material [ImageVector] mapping
 * - Combined [PaneDisplayInfo] lookup by pane type key
 *
 * All panes and workspace composables should import this file
 * rather than defining their own colour / icon logic.
 */
object PaneStyling {

    // ── Category → accent colour ──

    /** Returns the theme accent colour for a [PaneCategory]. */
    fun categoryColor(category: PaneCategory): Color = when (category) {
        PaneCategory.Text -> AppColors.PaneText
        PaneCategory.Study -> AppColors.PaneStudy
        PaneCategory.Resource -> AppColors.PaneResource
        PaneCategory.Writing -> AppColors.PaneWriting
        PaneCategory.Tool -> AppColors.PaneTool
        PaneCategory.Media -> AppColors.PaneMedia
    }

    // ── Icon-name → ImageVector ──

    /**
     * Maps the icon-name string stored in [PaneMetadata.icon]
     * to an actual Material [ImageVector].
     */
    @Suppress("CyclomaticComplexMethod")
    fun paneIcon(iconName: String): ImageVector = when (iconName) {
        "auto_stories" -> Icons.Default.AutoStories
        "compare" -> Icons.AutoMirrored.Filled.CompareArrows
        "call_split" -> Icons.AutoMirrored.Filled.CallSplit
        "abc", "spellcheck" -> Icons.Default.Spellcheck
        "data_object" -> Icons.Default.DataObject
        "view_column" -> Icons.Default.ViewColumn
        "lightbulb" -> Icons.Default.Lightbulb
        "history_edu" -> Icons.Default.HistoryEdu
        "rate_review" -> Icons.Default.RateReview
        "find_in_page" -> Icons.Default.FindInPage
        "local_library" -> Icons.Default.LocalLibrary
        "edit_note" -> Icons.Default.EditNote
        "article" -> Icons.AutoMirrored.Filled.Article
        "format_color_fill" -> Icons.Default.FormatColorFill
        "bookmark" -> Icons.Default.Bookmark
        "search" -> Icons.Default.Search
        "hub" -> Icons.Default.Hub
        "timeline" -> Icons.Default.Timeline
        "public" -> Icons.Default.Public
        "calendar_month" -> Icons.Default.CalendarMonth
        "space_dashboard" -> Icons.Default.SpaceDashboard
        "headphones" -> Icons.Default.Headphones
        "music_note" -> Icons.Default.MusicNote
        else -> Icons.AutoMirrored.Filled.List
    }

    // ── Combined pane info lookup ──

    /**
     * Returns the display name, icon, and accent colour for a pane type.
     *
     * Falls back to sensible defaults if the pane type is unknown.
     */
    fun paneInfo(paneType: String): PaneDisplayInfo {
        return try {
            val meta = PaneRegistry.metadata(paneType)
            PaneDisplayInfo(
                displayName = meta.displayName,
                icon = paneIcon(meta.icon),
                accentColor = categoryColor(meta.category),
                category = meta.category,
                description = meta.description
            )
        } catch (_: IllegalArgumentException) {
            PaneDisplayInfo(
                displayName = paneType,
                icon = Icons.AutoMirrored.Filled.List,
                accentColor = AppColors.PaneTool,
                category = PaneCategory.Tool
            )
        }
    }

    /**
     * Map from pane type key to string resource name for i18n.
     */
    val paneTypeToStringKey: Map<String, String> = mapOf(
        PaneType.BIBLE_READER to "pane_bible_reader",
        PaneType.TEXT_COMPARISON to "pane_text_comparison",
        PaneType.CROSS_REFERENCES to "pane_cross_references",
        PaneType.WORD_STUDY to "pane_word_study",
        PaneType.MORPHOLOGY to "pane_morphology",
        PaneType.INTERLINEAR to "pane_interlinear",
        PaneType.PASSAGE_GUIDE to "pane_passage_guide",
        PaneType.EXEGETICAL_GUIDE to "pane_exegetical_guide",
        PaneType.COMMENTARY to "pane_commentary",
        PaneType.DICTIONARY to "pane_dictionary",
        PaneType.RESOURCE_LIBRARY to "pane_resource_library",
        PaneType.NOTE_EDITOR to "pane_note_editor",
        PaneType.SERMON_EDITOR to "pane_sermon_editor",
        PaneType.HIGHLIGHTS to "pane_highlights",
        PaneType.BOOKMARKS to "pane_bookmarks",
        PaneType.SEARCH to "pane_search",
        PaneType.KNOWLEDGE_GRAPH to "pane_knowledge_graph",
        PaneType.TIMELINE to "pane_timeline",
        PaneType.THEOLOGICAL_ATLAS to "pane_theological_atlas",
        PaneType.READING_PLANS to "pane_reading_plans",
        PaneType.DASHBOARD to "pane_dashboard",
        PaneType.AUDIO_SYNC to "pane_audio_sync",
        PaneType.WORSHIP to "pane_worship"
    )
}

/**
 * Pre-resolved display information for a pane type,
 * ready for use in composables.
 */
data class PaneDisplayInfo(
    val displayName: String,
    val icon: ImageVector,
    val accentColor: Color,
    val category: PaneCategory,
    val description: String = ""
)
