package org.biblestudio.features.reading_plans.domain.repositories

import org.biblestudio.features.reading_plans.domain.entities.PlanProgress
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan

/**
 * CRUD operations for reading plans and user progress tracking.
 */
interface ReadingPlanRepository {

    /** Returns all available reading plans. */
    suspend fun getPlans(): Result<List<ReadingPlan>>

    /** Finds a reading plan by UUID. */
    suspend fun getPlanByUuid(uuid: String): Result<ReadingPlan?>

    /** Returns plans filtered by type (e.g., "chronological"). */
    suspend fun getPlansByType(type: String): Result<List<ReadingPlan>>

    /** Returns all progress entries for a plan, ordered by day. */
    suspend fun getProgress(planId: String): Result<List<PlanProgress>>

    /** Returns only completed days for a plan. */
    suspend fun getCompletedDays(planId: String): Result<List<PlanProgress>>

    /** Marks a specific day as completed in a plan. */
    suspend fun markDayCompleted(planId: String, day: Long, completedAt: String): Result<Unit>

    /** Creates a new reading plan. */
    suspend fun createPlan(plan: ReadingPlan): Result<Unit>

    /** Creates a progress entry for a plan day. */
    suspend fun createProgress(progress: PlanProgress): Result<Unit>

    /** Deletes a reading plan by UUID. */
    suspend fun deletePlan(uuid: String): Result<Unit>
}
