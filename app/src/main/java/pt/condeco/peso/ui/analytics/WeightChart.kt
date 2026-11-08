package pt.condeco.peso.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import pt.condeco.peso.R
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.Units
import pt.condeco.peso.ui.TimeRange
import pt.condeco.peso.ui.WeightUiState
import pt.condeco.peso.ui.util.Formats

private val TOOLTIP_RESERVED_HEIGHT = 84.dp
private val AXIS_RESERVED_HEIGHT = 24.dp

/** Weight-over-time line chart. Touch or drag on it to inspect individual entries. */
@Composable
fun WeightChartCard(uiState: WeightUiState, modifier: Modifier = Modifier) {
    val entries = uiState.filteredEntries.sortedBy { it.date }
    val unit = uiState.unitSystem
    val goal = Units.toDisplayWeight(uiState.goalWeight, unit)
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val goalColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.surface

    // Reset whenever the visible data changes so the index can never point past the end.
    var selectedIndex by remember(entries) { mutableStateOf<Int?>(null) }
    var chartWidthPx by remember { mutableIntStateOf(0) }
    var tooltipWidthPx by remember { mutableIntStateOf(0) }

    val chartDescription = if (entries.isEmpty()) {
        stringResource(R.string.chart_description_empty)
    } else {
        stringResource(
            R.string.chart_description,
            entries.size,
            Formats.dayMonth(entries.first().date),
            Formats.dayMonth(entries.last().date),
            Units.formatWeight(entries.last().weight, unit),
            Units.weightLabel(unit)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .semantics(mergeDescendants = true) { contentDescription = chartDescription }
        ) {
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = TOOLTIP_RESERVED_HEIGHT, bottom = AXIS_RESERVED_HEIGHT)
                        .onSizeChanged { chartWidthPx = it.width }
                        .pointerInput(entries) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                selectedIndex = nearestIndex(down.position.x, size.width.toFloat(), entries.size)
                                do {
                                    val change = awaitFirstChange() ?: break
                                    if (change.pressed) {
                                        selectedIndex = nearestIndex(change.position.x, size.width.toFloat(), entries.size)
                                        change.consume()
                                    }
                                } while (change.pressed)
                            }
                        }
                ) {
                    val scale = ChartScale(entries.map { Units.toDisplayWeight(it.weight, unit) } + goal, size.width, size.height)

                    for (i in 0..4) {
                        val y = size.height * i / 4
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                    }

                    val goalY = scale.y(goal)
                    drawLine(
                        color = goalColor,
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    fun point(index: Int) =
                        Offset(scale.x(index, entries.size), scale.y(Units.toDisplayWeight(entries[index].weight, unit)))

                    if (entries.size >= 2) {
                        val path = Path()
                        entries.indices.forEach { i ->
                            val p = point(i)
                            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                        }
                        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx()))
                    }

                    // The selected point replaces the default highlight on the latest entry.
                    val center = point(selectedIndex ?: entries.lastIndex)
                    if (selectedIndex != null) {
                        drawLine(lineColor.copy(alpha = 0.4f), Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 1.dp.toPx())
                    }
                    drawCircle(lineColor, radius = 6.dp.toPx(), center = center)
                    drawCircle(dotColor, radius = 3.dp.toPx(), center = center)
                }

                selectedIndex?.let { index ->
                    val x = pointX(index, entries.size, chartWidthPx.toFloat())
                    ChartTooltip(
                        entry = entries[index],
                        previous = entries.getOrNull(index - 1),
                        goalKg = uiState.goalWeight,
                        unit = unit,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .onSizeChanged { tooltipWidthPx = it.width }
                            .offset {
                                val maxLeft = (chartWidthPx - tooltipWidthPx).coerceAtLeast(0).toFloat()
                                IntOffset((x - tooltipWidthPx / 2f).coerceIn(0f, maxLeft).roundToInt(), 0)
                            }
                    )
                }

                Text(
                    "${stringResource(R.string.goal)}: ${Formats.decimal(goal)} ${Units.weightLabel(unit)}",
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = TOOLTIP_RESERVED_HEIGHT + 4.dp, end = 8.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                DateAxis(entries, uiState.selectedTimeRange, Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}

@Composable
private fun DateAxis(entries: List<WeightEntry>, range: TimeRange, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(Formats.dayMonth(entries.first().date), fontSize = 10.sp, color = color)
        Text(
            if (range == TimeRange.ALL) Formats.dayMonth(entries.last().date) else stringResource(R.string.today).uppercase(),
            fontSize = 10.sp,
            color = color
        )
    }
}

private suspend fun AwaitPointerEventScope.awaitFirstChange(): PointerInputChange? =
    awaitPointerEvent().changes.firstOrNull()
