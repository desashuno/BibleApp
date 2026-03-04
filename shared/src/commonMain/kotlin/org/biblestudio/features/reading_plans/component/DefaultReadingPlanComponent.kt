package org.biblestudio.features.reading_plans.component

import com.arkivanov.decompose.ComponentContext
import org.biblestudio.core.util.componentScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.biblestudio.core.util.nowIso
import org.biblestudio.core.util.calculateStreak
import org.biblestudio.core.util.generateUuid
import org.biblestudio.core.verse_bus.LinkEvent
import org.biblestudio.core.verse_bus.VerseBus
import org.biblestudio.features.reading_plans.domain.entities.BuiltInPlans
import org.biblestudio.features.reading_plans.domain.entities.ReadingPlan
import org.biblestudio.features.reading_plans.domain.repositories.ReadingPlanRepository

/**
 * Default [ReadingPlanComponent] with streak calculation and progress tracking.
 */
internal class DefaultReadingPlanComponent(
    componentContext: ComponentContext,
    private val repository: ReadingPlanRepository,
    private val verseBus: VerseBus
) : ReadingPlanComponent, ComponentContext by componentContext {

    private val scope = componentScope()
    private val _state = MutableStateFlow(ReadingPlanState())
    override val state: StateFlow<ReadingPlanState> = _state.asStateFlow()

    init {
        scope.launch {
            seedBuiltInPlans()
            loadPlansAsync()
        }
        observeVerseBus()
    }

    private fun observeVerseBus() {
        scope.launch {
            verseBus.events
                .filterIsInstance<LinkEvent.VerseSelected>()
                .collect { /* Reserved for future: highlight relevant plan day */ }
        }
    }

    override fun onPlanSelected(uuid: String) {
        scope.launch {
            repository.getPlanByUuid(uuid).onSuccess { plan ->
                if (plan != null) {
                    _state.update { it.copy(activePlan = plan) }
                    loadProgress(plan)
                }
            }
        }
    }

    override fun onMarkDayCompleted(day: Int) {
        val plan = _state.value.activePlan ?: return
        val now = nowIso()
        scope.launch {
            repository.markDayCompleted(plan.uuid, day.toLong(), now).onSuccess {
                loadProgress(plan)
            }
        }
    }

    override fun onNewPlan(title: String, description: String, durationDays: Int, type: String) {
        val uuid = generateUuid()
        val plan = ReadingPlan(
            uuid = uuid,
            title = title,
            description = description,
            durationDays = durationDays.toLong(),
            type = type
        )
        scope.launch {
            repository.createPlan(plan).onSuccess {
                loadPlansAsync()
                _state.update { it.copy(activePlan = plan) }
                loadProgress(plan)
            }
        }
    }

    override fun onDeletePlan(uuid: String) {
        scope.launch {
            repository.deletePlan(uuid).onSuccess {
                if (_state.value.activePlan?.uuid == uuid) {
                    _state.update {
                        it.copy(
                            activePlan = null,
                            progress = emptyList(),
                            completedDays = 0,
                            currentDay = 1,
                            currentStreak = 0,
                            progressPercent = 0f
                        )
                    }
                }
                loadPlansAsync()
            }
        }
    }

    private suspend fun seedBuiltInPlans() {
        repository.getPlans().onSuccess { existing ->
            val existingUuids = existing.map { it.uuid }.toSet()
            for (plan in BuiltInPlans.ALL) {
                if (plan.uuid !in existingUuids) {
                    repository.createPlan(plan)
                    Napier.i("Seeded built-in plan: ${plan.title}")
                }
            }
        }
    }

    private suspend fun loadPlansAsync() {
        _state.update { it.copy(isLoading = true, error = null) }
        repository.getPlans()
            .onSuccess { plans ->
                _state.update { it.copy(plans = plans, isLoading = false) }
            }
            .onFailure { e ->
                Napier.e("Failed to load plans", e)
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
    }

    private fun loadProgress(plan: ReadingPlan) {
        scope.launch {
            repository.getProgress(plan.uuid).onSuccess { progress ->
                val completed = progress.count { it.completed }
                val total = plan.durationDays.toInt()
                val streak = calculateStreak(progress.filter { it.completed }.map { it.day.toInt() })
                val nextDay = if (completed < total) completed + 1 else total
                val pct = if (total > 0) completed.toFloat() / total else 0f
                _state.update {
                    it.copy(
                        progress = progress,
                        completedDays = completed,
                        currentDay = nextDay,
                        currentStreak = streak,
                        progressPercent = pct
                    )
                }
            }
        }
    }

}
