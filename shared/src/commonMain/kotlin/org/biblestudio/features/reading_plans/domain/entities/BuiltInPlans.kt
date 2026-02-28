package org.biblestudio.features.reading_plans.domain.entities

/**
 * Built-in reading plan definitions that ship with the application.
 * These are seeded into the database on first launch if not already present.
 */
object BuiltInPlans {

    val BIBLE_IN_A_YEAR = ReadingPlan(
        uuid = "built-in-bible-in-a-year",
        title = "Bible in a Year",
        description = "Read through the entire Bible in 365 days following a chronological order.",
        durationDays = 365,
        type = "chronological"
    )

    val NT_IN_90_DAYS = ReadingPlan(
        uuid = "built-in-nt-90-days",
        title = "New Testament in 90 Days",
        description = "Complete the New Testament in 90 days with focused daily readings.",
        durationDays = 90,
        type = "canonical"
    )

    val PSALMS_AND_PROVERBS = ReadingPlan(
        uuid = "built-in-psalms-proverbs",
        title = "Psalms & Proverbs",
        description = "Read through Psalms and Proverbs in 60 days — one month of wisdom and worship.",
        durationDays = 60,
        type = "topical"
    )

    val GOSPELS = ReadingPlan(
        uuid = "built-in-gospels",
        title = "The Four Gospels",
        description = "Walk through the life of Jesus in 30 days across Matthew, Mark, Luke, and John.",
        durationDays = 30,
        type = "canonical"
    )

    /** All built-in plans, ordered for display. */
    val ALL: List<ReadingPlan> = listOf(
        BIBLE_IN_A_YEAR,
        NT_IN_90_DAYS,
        PSALMS_AND_PROVERBS,
        GOSPELS
    )

    /** UUIDs of all built-in plans for identification. */
    val UUIDS: Set<String> = ALL.map { it.uuid }.toSet()
}
