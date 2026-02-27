package org.biblestudio.features.reading_plans.data.mappers

import migrations.Reading_plan_progress
import migrations.Reading_plans
import org.biblestudio.features.reading_plans.domain.entities.PlanProgress
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan

// ── ReadingPlan ─────────────────────────────────────────────────────

internal fun Reading_plans.toReadingPlan(): ReadingPlan = ReadingPlan(
    uuid = uuid,
    title = title,
    description = description,
    durationDays = duration_days,
    type = type
)

// ── PlanProgress ────────────────────────────────────────────────────

internal fun Reading_plan_progress.toPlanProgress(): PlanProgress = PlanProgress(
    id = id,
    planId = plan_id,
    day = day,
    completed = completed == 1L,
    completedAt = completed_at
)
