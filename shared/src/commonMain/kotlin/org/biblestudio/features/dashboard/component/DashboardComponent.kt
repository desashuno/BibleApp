package org.biblestudio.features.dashboard.component

import kotlinx.coroutines.flow.StateFlow

/**
 * UI state for the Dashboard pane — aggregates stats from multiple features.
 */
data class DashboardState(
    val totalNotes: Int = 0,
    val totalHighlights: Int = 0,
    val totalBookmarks: Int = 0,
    val totalSermons: Int = 0,
    val activePlans: Int = 0,
    val recentHistory: List<String> = emptyList(),
    val dailyVerse: DailyVerse? = null,
    val continueReading: ContinueReading? = null,
    val readingPlanProgress: ReadingPlanWidget? = null,
    val recentNotes: List<RecentNote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Daily verse widget data.
 */
data class DailyVerse(
    val globalVerseId: Long,
    val text: String,
    val reference: String
)

/**
 * Continue-reading widget — the last passage the user was reading.
 */
data class ContinueReading(
    val globalVerseId: Long,
    val reference: String,
    val timestamp: String
)

/**
 * Compact reading-plan progress widget.
 */
data class ReadingPlanWidget(
    val planTitle: String,
    val progressPercent: Float,
    val currentDay: Int,
    val totalDays: Int,
    val streak: Int
)

/**
 * A recent note summary for the dashboard.
 */
data class RecentNote(
    val uuid: String,
    val title: String,
    val preview: String
)

/**
 * Component for the Dashboard feature.
 */
interface DashboardComponent {
    val state: StateFlow<DashboardState>
    fun refresh()
}
