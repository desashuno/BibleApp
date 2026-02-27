package org.biblestudio.features.reading_plans.domain.entities

/**
 * Progress entry for a single day in a [ReadingPlan].
 *
 * @param id Auto-generated database ID.
 * @param planId FK to the parent [ReadingPlan] UUID.
 * @param day Day number (1-based) within the plan.
 * @param completed Whether this day has been completed.
 * @param completedAt ISO 8601 timestamp of when the day was marked complete, or null.
 */
data class PlanProgress(
    val id: Long,
    val planId: String,
    val day: Long,
    val completed: Boolean,
    val completedAt: String?
)
