package org.biblestudio.features.reading_plans.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.biblestudio.features.reading_plans.domain.entities.PlanProgress
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan
import org.biblestudio.features.reading_plans.domain.repositories.ReadingPlanRepository

class DefaultReadingPlanComponentTest {

    private val testPlan = ReadingPlan(
        uuid = "rp1",
        title = "Through the Bible",
        description = "Read the entire Bible in 365 days",
        durationDays = 365,
        type = "chronological"
    )

    private val fakeRepo = object : ReadingPlanRepository {
        private val plans = mutableListOf(testPlan)
        private val progressList = mutableListOf<PlanProgress>()

        override suspend fun getPlans() = Result.success(plans.toList())
        override suspend fun getPlanByUuid(uuid: String) = Result.success(plans.firstOrNull { it.uuid == uuid })
        override suspend fun getPlansByType(type: String) = Result.success(plans.filter { it.type == type })
        override suspend fun getProgress(planId: String) = Result.success(progressList.filter { it.planId == planId })
        override suspend fun getCompletedDays(planId: String) = Result.success(
            progressList.filter { it.planId == planId && it.completed }
        )
        override suspend fun markDayCompleted(planId: String, day: Long, completedAt: String): Result<Unit> {
            progressList.add(PlanProgress(progressList.size.toLong() + 1, planId, day, true, completedAt))
            return Result.success(Unit)
        }
        override suspend fun createPlan(plan: ReadingPlan) = Result.success(Unit).also { plans.add(plan) }
        override suspend fun createProgress(progress: PlanProgress) = Result.success(Unit).also {
            progressList.add(progress)
        }
        override suspend fun deletePlan(uuid: String) = Result.success(Unit).also {
            plans.removeAll { it.uuid == uuid }
        }
    }

    private fun createComponent(): DefaultReadingPlanComponent {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle = lifecycle)
        return DefaultReadingPlanComponent(
            componentContext = context,
            repository = fakeRepo
        )
    }

    @Test
    fun initialStateLoadsPlans() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.plans.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertEquals(1, component.state.value.plans.size)
        assertNull(component.state.value.activePlan)
    }

    @Test
    fun selectPlanLoadsProgress() = runTest {
        val component = createComponent()

        val timeout = 5_000L
        val start = System.currentTimeMillis()
        while (component.state.value.plans.isEmpty() && System.currentTimeMillis() - start < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        component.onPlanSelected("rp1")

        val start2 = System.currentTimeMillis()
        while (component.state.value.activePlan == null && System.currentTimeMillis() - start2 < timeout) {
            @Suppress("MagicNumber")
            kotlinx.coroutines.delay(50)
        }

        assertNotNull(component.state.value.activePlan)
        assertEquals("Through the Bible", component.state.value.activePlan?.title)
        assertEquals(0, component.state.value.completedDays)
    }

    @Test
    fun streakCalculation() {
        assertEquals(0, DefaultReadingPlanComponent.computeStreak(emptyList()))
        assertEquals(1, DefaultReadingPlanComponent.computeStreak(listOf(1)))
        assertEquals(3, DefaultReadingPlanComponent.computeStreak(listOf(1, 2, 3)))
        assertEquals(2, DefaultReadingPlanComponent.computeStreak(listOf(1, 3, 4)))
        assertEquals(1, DefaultReadingPlanComponent.computeStreak(listOf(1, 3, 5)))
    }
}
