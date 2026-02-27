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
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Component for the Dashboard feature.
 */
interface DashboardComponent {
    val state: StateFlow<DashboardState>
    fun refresh()
}
