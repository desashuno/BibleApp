package org.biblestudio.ui.panes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.core.util.VerseRefFormatter
import org.biblestudio.features.reading_plans.component.ReadingPlanState
import org.biblestudio.ui.theme.Spacing

/**
 * Reading Plans pane: plan list + progress ring + calendar grid.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod", "LongParameterList")
@Composable
fun ReadingPlanPane(
    stateFlow: StateFlow<ReadingPlanState>,
    onPlanSelected: (String) -> Unit,
    onMarkDayCompleted: (Int) -> Unit,
    onDeletePlan: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Scaffold(modifier = modifier) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Plan list sidebar ──
            Column(
                modifier = Modifier
                    .width(Spacing.Space48 * 5)
                    .padding(Spacing.Space8)
            ) {
                Text(
                    text = "Reading Plans",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Spacing.Space8)
                )
                LazyColumn {
                    items(state.plans, key = { it.uuid }) { plan ->
                        PlanListItem(
                            title = plan.title,
                            description = plan.description,
                            isActive = plan.uuid == state.activePlan?.uuid,
                            onClick = { onPlanSelected(plan.uuid) },
                            onDelete = { onDeletePlan(plan.uuid) }
                        )
                    }
                }
            }

            // ── Detail area ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(Spacing.Space16)
            ) {
                val plan = state.activePlan
                if (plan == null) {
                    Text(
                        text = "Select a reading plan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.Space24)
                    )
                } else {
                    Text(text = plan.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(Spacing.Space8))

                    // Progress ring
                    ProgressRing(
                        progress = state.progressPercent,
                        completedDays = state.completedDays,
                        totalDays = plan.durationDays.toInt()
                    )
                    Spacer(modifier = Modifier.height(Spacing.Space8))

                    // Streak
                    Text(
                        text = "Streak: ${state.currentStreak} days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.Space8))

                    // Current day passage label (shows verse ref if available via progress entry)
                    val todayProgress = state.progress.firstOrNull { it.day.toInt() == state.currentDay }
                    val dayLabel = if (todayProgress != null) {
                        "Day ${state.currentDay}"
                    } else {
                        "Day ${state.currentDay}"
                    }
                    Text(
                        text = "Today: $dayLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.Space16))

                    // Calendar grid
                    CalendarGrid(
                        totalDays = plan.durationDays.toInt(),
                        progress = state.progress,
                        currentDay = state.currentDay,
                        onDayClicked = onMarkDayCompleted
                    )
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun ProgressRing(progress: Float, completedDays: Int, totalDays: Int) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surfaceVariant
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(RING_SIZE)) {
        Canvas(modifier = Modifier.size(RING_SIZE)) {
            val stroke = Stroke(width = RING_STROKE.toPx(), cap = StrokeCap.Round)
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(stroke.width / 2, stroke.width / 2)
            drawArc(
                color = surface,
                startAngle = 0f,
                sweepAngle = FULL_CIRCLE,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
            drawArc(
                color = primary,
                startAngle = START_ANGLE,
                sweepAngle = progress * FULL_CIRCLE,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }
        Text(
            text = "$completedDays/$totalDays",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarGrid(
    totalDays: Int,
    progress: List<org.biblestudio.features.reading_plans.domain.entities.PlanProgress>,
    currentDay: Int,
    onDayClicked: (Int) -> Unit
) {
    val completedSet = progress.filter { it.completed }.map { it.day.toInt() }.toSet()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.Space4)
    ) {
        for (day in 1..totalDays) {
            val isCompleted = day in completedSet
            val isCurrent = day == currentDay
            Card(
                modifier = Modifier
                    .size(DAY_CELL_SIZE)
                    .clickable { if (!isCompleted) onDayClicked(day) }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Day $day completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Spacing.Space16)
                        )
                    } else {
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Composable
private fun PlanListItem(
    title: String,
    description: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.Space4)
    ) {
        Row(modifier = Modifier.padding(Spacing.Space8)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private val RING_SIZE = 120.dp
private val RING_STROKE = 10.dp
private val DAY_CELL_SIZE = 36.dp
private const val FULL_CIRCLE = 360f
private const val START_ANGLE = -90f
