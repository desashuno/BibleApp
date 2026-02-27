package org.biblestudio.features.reading_plans.data.repositories

import org.biblestudio.database.BibleStudioDatabase
import org.biblestudio.features.reading_plans.data.mappers.toPlanProgress
import org.biblestudio.features.reading_plans.data.mappers.toReadingPlan
import org.biblestudio.features.reading_plans.domain.entities.PlanProgress
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan
import org.biblestudio.features.reading_plans.domain.repositories.ReadingPlanRepository

internal class ReadingPlanRepositoryImpl(
    private val database: BibleStudioDatabase
) : ReadingPlanRepository {

    override suspend fun getPlans(): Result<List<ReadingPlan>> = runCatching {
        database.readingPlanQueries
            .allReadingPlans()
            .executeAsList()
            .map { it.toReadingPlan() }
    }

    override suspend fun getPlanByUuid(uuid: String): Result<ReadingPlan?> = runCatching {
        database.readingPlanQueries
            .readingPlanByUuid(uuid)
            .executeAsOneOrNull()
            ?.toReadingPlan()
    }

    override suspend fun getPlansByType(type: String): Result<List<ReadingPlan>> = runCatching {
        database.readingPlanQueries
            .readingPlansByType(type)
            .executeAsList()
            .map { it.toReadingPlan() }
    }

    override suspend fun getProgress(planId: String): Result<List<PlanProgress>> = runCatching {
        database.readingPlanQueries
            .progressForPlan(planId)
            .executeAsList()
            .map { it.toPlanProgress() }
    }

    override suspend fun getCompletedDays(planId: String): Result<List<PlanProgress>> = runCatching {
        database.readingPlanQueries
            .completedDaysForPlan(planId)
            .executeAsList()
            .map { it.toPlanProgress() }
    }

    override suspend fun markDayCompleted(planId: String, day: Long, completedAt: String): Result<Unit> = runCatching {
        database.readingPlanQueries.markDayCompleted(
            completedAt = completedAt,
            planId = planId,
            day = day
        )
    }

    override suspend fun createPlan(plan: ReadingPlan): Result<Unit> = runCatching {
        database.readingPlanQueries.insertReadingPlan(
            uuid = plan.uuid,
            title = plan.title,
            description = plan.description,
            durationDays = plan.durationDays,
            type = plan.type
        )
    }

    override suspend fun createProgress(progress: PlanProgress): Result<Unit> = runCatching {
        database.readingPlanQueries.insertProgress(
            planId = progress.planId,
            day = progress.day,
            completed = if (progress.completed) 1L else 0L,
            completedAt = progress.completedAt
        )
    }

    override suspend fun deletePlan(uuid: String): Result<Unit> = runCatching {
        database.readingPlanQueries.deleteReadingPlan(uuid)
    }
}
