package org.biblestudio.features.reading_plans.data.repositories

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.reading_plans.domain.entities.PlanProgress
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan
import org.biblestudio.test.TestDatabase

class ReadingPlanRepositoryImplTest {

    private lateinit var testDb: TestDatabase
    private lateinit var repo: ReadingPlanRepositoryImpl

    @BeforeTest
    fun setUp() {
        testDb = TestDatabase()
        repo = ReadingPlanRepositoryImpl(testDb.database)
    }

    @AfterTest
    fun tearDown() {
        testDb.close()
    }

    private fun plan(uuid: String = "plan-1") = ReadingPlan(
        uuid = uuid,
        title = "Through the Bible",
        description = "Read the whole Bible in a year",
        durationDays = 365L,
        type = "yearly"
    )

    @Test
    fun `create and retrieve plan`() = runTest {
        repo.createPlan(plan()).getOrThrow()

        val result = repo.getPlanByUuid("plan-1").getOrThrow()
        assertNotNull(result)
        assertEquals("Through the Bible", result.title)
        assertEquals(365L, result.durationDays)
    }

    @Test
    fun `getPlans returns all plans`() = runTest {
        repo.createPlan(plan("p1")).getOrThrow()
        repo.createPlan(plan("p2")).getOrThrow()

        val plans = repo.getPlans().getOrThrow()
        assertEquals(2, plans.size)
    }

    @Test
    fun `getPlansByType filters correctly`() = runTest {
        repo.createPlan(plan("p1")).getOrThrow()
        repo.createPlan(
            ReadingPlan("p2", "NT Plan", "New Testament", 90L, "quarterly")
        ).getOrThrow()

        val yearly = repo.getPlansByType("yearly").getOrThrow()
        assertEquals(1, yearly.size)
        assertEquals("p1", yearly.first().uuid)
    }

    @Test
    fun `deletePlan removes plan`() = runTest {
        repo.createPlan(plan()).getOrThrow()
        repo.deletePlan("plan-1").getOrThrow()

        val plans = repo.getPlans().getOrThrow()
        assertTrue(plans.isEmpty())
    }

    @Test
    fun `create and retrieve progress`() = runTest {
        repo.createPlan(plan()).getOrThrow()

        repo.createProgress(
            PlanProgress(
                id = 0L,
                planId = "plan-1",
                day = 1L,
                completed = false,
                completedAt = null
            )
        ).getOrThrow()

        val progress = repo.getProgress("plan-1").getOrThrow()
        assertEquals(1, progress.size)
        assertEquals(1L, progress.first().day)
    }

    @Test
    fun `markDayCompleted updates progress`() = runTest {
        repo.createPlan(plan()).getOrThrow()
        repo.createProgress(
            PlanProgress(0L, "plan-1", 1L, false, null)
        ).getOrThrow()

        repo.markDayCompleted("plan-1", 1L, "2025-06-01T00:00:00Z").getOrThrow()

        val completed = repo.getCompletedDays("plan-1").getOrThrow()
        assertEquals(1, completed.size)
        assertTrue(completed.first().completed)
    }
}
