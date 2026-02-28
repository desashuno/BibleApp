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
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.biblestudio.core.pane_registry.PaneCategory
import org.biblestudio.core.pane_registry.PaneRegistry

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
        "bible-reader" to "pane_bible_reader",
        "text-comparison" to "pane_text_comparison",
        "cross-references" to "pane_cross_references",
        "word-study" to "pane_word_study",
        "morphology" to "pane_morphology",
        "interlinear" to "pane_interlinear",
        "passage-guide" to "pane_passage_guide",
        "exegetical-guide" to "pane_exegetical_guide",
        "commentary" to "pane_commentary",
        "dictionary" to "pane_dictionary",
        "resource-library" to "pane_resource_library",
        "note-editor" to "pane_note_editor",
        "sermon-editor" to "pane_sermon_editor",
        "highlights" to "pane_highlights",
        "bookmarks" to "pane_bookmarks",
        "search" to "pane_search",
        "knowledge-graph" to "pane_knowledge_graph",
        "timeline" to "pane_timeline",
        "theological-atlas" to "pane_theological_atlas",
        "reading-plans" to "pane_reading_plans",
        "dashboard" to "pane_dashboard",
        "audio-sync" to "pane_audio_sync",
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
