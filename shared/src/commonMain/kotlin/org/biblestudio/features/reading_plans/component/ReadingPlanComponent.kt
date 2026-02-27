package org.biblestudio.features.reading_plans.component

import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.reading_plans.domain.entities.PlanProgress
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan

/**
 * UI state for the Reading Plans pane.
 */
data class ReadingPlanState(
    val plans: List<ReadingPlan> = emptyList(),
    val activePlan: ReadingPlan? = null,
    val progress: List<PlanProgress> = emptyList(),
    val completedDays: Int = 0,
    val currentDay: Int = 1,
    val currentStreak: Int = 0,
    val progressPercent: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Component for the Reading Plans feature.
 */
interface ReadingPlanComponent {
    val state: StateFlow<ReadingPlanState>
    fun onPlanSelected(uuid: String)
    fun onMarkDayCompleted(day: Int)
    fun onNewPlan(title: String, description: String, durationDays: Int, type: String)
    fun onDeletePlan(uuid: String)
}
