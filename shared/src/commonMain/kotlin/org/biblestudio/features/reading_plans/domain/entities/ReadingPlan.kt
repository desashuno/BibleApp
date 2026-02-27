package org.biblestudio.features.reading_plans.domain.entities

/**
 * A reading plan definition.
 *
 * @param uuid Unique identifier (UUID v4).
 * @param title Plan display title (e.g., "Bible in a Year").
 * @param description Plan description.
 * @param durationDays Total number of days in the plan.
 * @param type Plan type (e.g., "chronological", "canonical", "topical").
 */
data class ReadingPlan(
    val uuid: String,
    val title: String,
    val description: String,
    val durationDays: Long,
    val type: String
)
