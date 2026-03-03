package pt.condeco.peso.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.data.WeightEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: WeightViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analytics), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(viewModel)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FB)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentTrendHeader(uiState)
            }
            item {
                WeightChartCard(uiState)
            }
            item {
                TimeRangeSelector(
                    selectedRange = uiState.selectedTimeRange,
                    onRangeSelected = { viewModel.setTimeRange(it) }
                )
            }
            item {
                StatsGrid(uiState)
            }
        }
    }
}

@Composable
fun CurrentTrendHeader(uiState: WeightUiState) {
    val entries = uiState.filteredEntries
    val diff = if (entries.size >= 2) entries.last().weight - entries.first().weight else 0f
    val goal = uiState.goalWeight
    val latest = uiState.latestEntry?.weight ?: 0f
    
    val isGoodChange = if (latest > goal) diff <= 0 else if (latest < goal) diff >= 0 else true

    Column {
        Text(stringResource(R.string.current_trend), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.1f".format(latest),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(" kg", color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            }
            
            val color = if (isGoodChange) Color(0xFF00BFA5) else Color(0xFFF44336)
            val bgColor = if (isGoodChange) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (diff <= 0) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (diff > 0) "+" else ""}${"%.1f".format(diff)} kg", 
                        color = color, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeightChartCard(uiState: WeightUiState) {
    val entries = uiState.filteredEntries.sortedBy { it.date }
    val goal = uiState.goalWeight
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F2F5))
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_data), color = Color.Gray)
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp)) {
                    val width = size.width
                    val height = size.height
                    
                    for (i in 0..4) {
                        val y = height * i / 4
                        drawLine(
                            color = Color(0xFFF0F2F5),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val weights = entries.map { it.weight } + goal
                    val minWeight = weights.min() - 1f
                    val maxWeight = weights.max() + 1f
                    val weightRange = maxWeight - minWeight

                    // Goal Line
                    val goalY = height - ((goal - minWeight) / weightRange * height)
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, goalY),
                        end = Offset(width, goalY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Weight Path
                    if (entries.size >= 2) {
                        val path = Path()
                        entries.forEachIndexed { index, entry ->
                            val x = index.toFloat() / (entries.size - 1) * width
                            val y = height - ((entry.weight - minWeight) / weightRange * height)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF0056D2),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // Last point
                    val lastX = if (entries.size > 1) width else width / 2
                    val lastY = height - ((entries.last().weight - minWeight) / weightRange * height)
                    drawCircle(Color(0xFF0056D2), radius = 6.dp.toPx(), center = Offset(lastX, lastY))
                    drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
                }
            }

            if (entries.isNotEmpty()) {
                Text(
                    "${stringResource(R.string.goal)}: %.1f".format(goal),
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 8.dp),
                    fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                if (entries.isNotEmpty()) {
                    Text(dateFormat.format(Date(entries.first().date)), fontSize = 10.sp, color = Color.LightGray)
                    Text(if (uiState.selectedTimeRange == TimeRange.ALL) dateFormat.format(Date(entries.last().date)) else stringResource(R.string.today).uppercase(), fontSize = 10.sp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun TimeRangeSelector(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFF0F2F5), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            val label = when(range) {
                TimeRange.W1 -> "1W"
                TimeRange.M1 -> "1M"
                TimeRange.M3 -> "3M"
                TimeRange.YTD -> "YTD"
                TimeRange.ALL -> "ALL"
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRangeSelected(range) },
                color = if (isSelected) Color(0xFF0056D2) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun StatsGrid(uiState: WeightUiState) {
    val entries = uiState.filteredEntries
    val goal = uiState.goalWeight
    val latest = uiState.latestEntry?.weight ?: 0f
    
    val highest = entries.maxByOrNull { it.weight }
    val lowest = entries.minByOrNull { it.weight }
    val change = if (entries.size >= 2) entries.last().weight - entries.first().weight else 0f
    val avg = if (entries.isNotEmpty()) entries.map { it.weight }.average().toFloat() else 0f

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                label = stringResource(R.string.highest),
                value = highest?.weight ?: 0f,
                subtext = highest?.let { dateFormat.format(Date(it.date)) } ?: "--",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.lowest),
                value = lowest?.weight ?: 0f,
                subtext = lowest?.let { dateFormat.format(Date(it.date)) } ?: "--",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val isGoodChange = if (latest > goal) change <= 0 else if (latest < goal) change >= 0 else true
            StatCard(
                label = stringResource(R.string.period_change),
                value = change,
                subtext = stringResource(R.string.period_change),
                isChange = true,
                isGood = isGoodChange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.avg_weight),
                value = avg,
                subtext = "Period average",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: Float, subtext: String, modifier: Modifier = Modifier, isChange: Boolean = false, isGood: Boolean = true) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F2F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${if (isChange && value > 0) "+" else ""}${"%.1f".format(value)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChange) {
                        if (isGood) Color(0xFF00BFA5) else Color(0xFFF44336)
                    } else Color.Black
                )
                Text(" kg", color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(Color.LightGray, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(subtext, color = Color.LightGray, fontSize = 10.sp)
            }
        }
    }
}
