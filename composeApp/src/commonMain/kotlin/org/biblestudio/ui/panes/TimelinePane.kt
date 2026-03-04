package org.biblestudio.ui.panes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.coroutines.flow.StateFlow
import org.biblestudio.features.timeline.component.TimelineState
import org.biblestudio.features.timeline.component.TimelineZoom
import org.biblestudio.features.timeline.domain.entities.TimelineCategory
import org.biblestudio.features.timeline.domain.entities.TimelineEvent
import org.biblestudio.ui.components.DismissableDetailCard
import org.biblestudio.ui.components.ErrorMessage
import org.biblestudio.ui.components.LoadingIndicator
import org.biblestudio.ui.theme.Spacing

/**
 * Timeline pane: horizontal time axis with event markers and detail card.
 */
@Suppress("ktlint:standard:function-naming", "LongMethod")
@Composable
fun TimelinePane(
    stateFlow: StateFlow<TimelineState>,
    onEventSelected: (TimelineEvent) -> Unit,
    onZoomChanged: (TimelineZoom) -> Unit,
    onScrollToYear: (Int) -> Unit,
    onCategoryFilter: (TimelineCategory?) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by stateFlow.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Zoom chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Space16, vertical = Spacing.Space8)
        ) {
            TimelineZoom.entries.forEach { zoom ->
                FilterChip(
                    selected = state.zoomLevel == zoom,
                    onClick = { onZoomChanged(zoom) },
                    label = { Text(zoom.name) },
                    modifier = Modifier.padding(end = Spacing.Space4)
                )
            }
        }

        // Category filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.Space16)
        ) {
            item {
                FilterChip(
                    selected = state.activeCategory == null,
                    onClick = { onCategoryFilter(null) },
                    label = { Text("All") },
                    modifier = Modifier.padding(end = Spacing.Space4)
                )
            }
            items(TimelineCategory.entries.toList()) { cat ->
                FilterChip(
                    selected = state.activeCategory == cat,
                    onClick = { onCategoryFilter(cat) },
                    label = { Text(cat.displayName) },
                    modifier = Modifier.padding(end = Spacing.Space4)
                )
            }
        }

        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        state.error?.let { err ->
            ErrorMessage(message = err)
        }

        // Timeline canvas
        val textMeasurer = rememberTextMeasurer()
        TimelineCanvas(
            events = state.events,
            startYear = state.visibleStartYear,
            endYear = state.visibleEndYear,
            selectedEvent = state.selectedEvent,
            textMeasurer = textMeasurer,
            onEventTapped = onEventSelected,
            onDragYear = onScrollToYear,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        // Event detail card
        state.selectedEvent?.let { event ->
            HorizontalDivider()
            TimelineEventCard(event = event, onDismiss = onClearSelection)
        }
    }
}

@Suppress(
    "ktlint:standard:function-naming",
    "MagicNumber",
    "LongMethod",
    "LongParameterList",
    "CyclomaticComplexMethod"
)
@Composable
private fun TimelineCanvas(
    events: List<TimelineEvent>,
    startYear: Int,
    endYear: Int,
    selectedEvent: TimelineEvent?,
    textMeasurer: TextMeasurer,
    onEventTapped: (TimelineEvent) -> Unit,
    onDragYear: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val yearRange = (endYear - startYear).coerceAtLeast(1)
    val axisColor = MaterialTheme.colorScheme.outline
    val textStyle = MaterialTheme.typography.labelSmall

    Canvas(
        modifier = modifier
            .pointerInput(startYear, endYear) {
                detectHorizontalDragGestures { _, dragAmount ->
                    val yearDelta = (-dragAmount / size.width * yearRange).toInt()
                    val center = (startYear + endYear) / 2 + yearDelta
                    onDragYear(center)
                }
            }
            .pointerInput(events, startYear, endYear) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val tapped = events.firstOrNull { event ->
                        val x = ((event.startYear - startYear).toFloat() / yearRange) * w
                        kotlin.math.abs(tapOffset.x - x) <= TAP_RADIUS
                    }
                    tapped?.let { onEventTapped(it) }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val axisY = h * 0.7f

        // Draw axis line
        drawLine(axisColor, Offset(0f, axisY), Offset(w, axisY), strokeWidth = 2f)

        // Draw year ticks
        val tickInterval = when {
            yearRange > 3000 -> 500
            yearRange > 1000 -> 200
            yearRange > 500 -> 100
            yearRange > 100 -> 50
            else -> 10
        }
        var tickYear = (startYear / tickInterval) * tickInterval
        while (tickYear <= endYear) {
            val x = ((tickYear - startYear).toFloat() / yearRange) * w
            drawLine(axisColor, Offset(x, axisY - 8f), Offset(x, axisY + 8f), strokeWidth = 1f)
            val label = if (tickYear < 0) "${-tickYear} BC" else if (tickYear == 0) "0" else "$tickYear AD"
            val textResult = textMeasurer.measure(label, textStyle)
            drawText(textResult, topLeft = Offset(x - textResult.size.width / 2f, axisY + 12f))
            tickYear += tickInterval
        }

        // Draw event markers
        for (event in events) {
            val x = ((event.startYear - startYear).toFloat() / yearRange) * w
            val markerH = when (event.importance.toInt()) {
                3 -> 40f
                2 -> 28f
                else -> 18f
            }
            val color = Color(event.category.colorHex)
            val isSelected = selectedEvent?.id == event.id
            val radius = if (isSelected) 8f else 5f

            // Vertical marker
            drawLine(color, Offset(x, axisY - markerH), Offset(x, axisY), strokeWidth = 2f)
            drawCircle(color, radius, Offset(x, axisY - markerH))

            // Duration bar
            event.endYear?.let { ey ->
                val x2 = ((ey - startYear).toFloat() / yearRange) * w
                drawRect(color.copy(alpha = 0.3f), Offset(x, axisY - 3f), androidx.compose.ui.geometry.Size(x2 - x, 6f))
            }
        }
    }
}

private const val TAP_RADIUS = 20f

@Suppress("ktlint:standard:function-naming")
@Composable
private fun TimelineEventCard(event: TimelineEvent, onDismiss: () -> Unit) {
    DismissableDetailCard(title = event.title, onDismiss = onDismiss) {
        Spacer(modifier = Modifier.height(Spacing.Space4))
        val yearLabel = buildString {
            if (event.startYear < 0) append("${-event.startYear} BC") else append("${event.startYear} AD")
            event.endYear?.let { ey ->
                append(" – ")
                if (ey < 0) append("${-ey} BC") else append("$ey AD")
            }
        }
        Text(
            text = "$yearLabel  •  ${event.category.displayName}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        if (event.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(Spacing.Space8))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
