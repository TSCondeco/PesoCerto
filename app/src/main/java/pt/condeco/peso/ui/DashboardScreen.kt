package pt.condeco.peso.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun DashboardScreen(viewModel: WeightViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFF3F51B5),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.app_name).uppercase(),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.config))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(viewModel)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddWeight() },
                containerColor = Color(0xFF0056D2),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_weight))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentWeightCard(uiState)
            }
            item {
                StatsRow(uiState)
            }
            item {
                BMICard(uiState.bmi)
            }
            item {
                RecentEntriesHeader(onViewLedger = { viewModel.navigateTo(Screen.Ledger) })
            }
            items(uiState.allEntries.take(5)) { entry ->
                WeightEntryItem(entry, uiState.allEntries, uiState.goalWeight, onEdit = { viewModel.openEditWeight(entry) })
            }
        }
    }
}

@Composable
fun CurrentWeightCard(uiState: WeightUiState) {
    val entry = uiState.latestEntry
    val previousEntry = uiState.previousEntry
    val goal = uiState.goalWeight
    val diff = if (entry != null && previousEntry != null) entry.weight - previousEntry.weight else 0f
    val dateStr = entry?.let { SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it.date)) } ?: "-- ---"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.current_weight), color = Color.Gray, fontSize = 12.sp)
                Text(dateStr.uppercase(), color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%.1f".format(entry?.weight ?: 0f),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("kg", color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val isGoodChange = if (entry != null && entry.weight > goal) {
                diff <= 0 
            } else if (entry != null && entry.weight < goal) {
                diff >= 0 
            } else {
                diff == 0f
            }

            val tintColor = if (isGoodChange) Color(0xFF4CAF50) else Color(0xFFF44336)
            val bgColor = if (isGoodChange) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            val icon = if (diff <= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("%.1f".format(abs(diff)), color = tintColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.since_yesterday), color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatsRow(uiState: WeightUiState) {
    val net = uiState.netChange
    val goal = uiState.goalWeight
    val latest = uiState.latestEntry?.weight ?: 0f
    
    val isGoodNet = if (latest > goal) net <= 0 else if (latest < goal) net >= 0 else true

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatSmallCard(stringResource(R.string.avg_7d), "%.1f".format(uiState.avg7d), modifier = Modifier.weight(1f))
        StatSmallCard(stringResource(R.string.prev_7d), "%.1f".format(uiState.prevAvg7d), modifier = Modifier.weight(1f))
        StatSmallCard(stringResource(R.string.net), "%.1f".format(net), modifier = Modifier.weight(1f), isHighlight = true, highlightColor = if (isGoodNet) Color(0xFF4CAF50) else Color(0xFFF44336))
    }
}

@Composable
fun StatSmallCard(label: String, value: String, modifier: Modifier = Modifier, isHighlight: Boolean = false, highlightColor: Color = Color(0xFF4CAF50)) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlight) highlightColor else Color.Black
            )
        }
    }
}

@Composable
fun BMICard(bmi: Float) {
    val category = when {
        bmi < 18.5 -> stringResource(R.string.underweight) to Color(0xFF2196F3)
        bmi < 25.0 -> stringResource(R.string.normal) to Color(0xFF009688)
        bmi < 30.0 -> stringResource(R.string.overweight) to Color(0xFFFF9800)
        else -> stringResource(R.string.obese) to Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.bmi_context), color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("%.1f".format(bmi), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = category.second.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        category.first,
                        color = category.second,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(stringResource(R.string.details), color = Color(0xFF3F51B5), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0xFFEEEEEE), CircleShape)
            ) {
                val progress = (bmi - 15) / (35 - 15)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(category.second, CircleShape)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("18.5", color = Color.Gray, fontSize = 10.sp)
                Text("25.0", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun RecentEntriesHeader(onViewLedger: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.recent_entries), fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.view_ledger), 
            color = Color(0xFF3F51B5), 
            fontSize = 14.sp,
            modifier = Modifier.clickable { onViewLedger() }
        )
    }
}

@Composable
fun WeightEntryItem(entry: WeightEntry, allEntries: List<WeightEntry>, goal: Float, onEdit: () -> Unit) {
    val index = allEntries.indexOf(entry)
    val previousEntry = if (index < allEntries.size - 1) allEntries[index + 1] else null
    val diff = if (previousEntry != null) entry.weight - previousEntry.weight else 0f
    
    val date = Date(entry.date)
    val dayStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
    val timeStr = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(date)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dayStr, fontWeight = FontWeight.SemiBold)
                Text(timeStr, color = Color.Gray, fontSize = 12.sp)
            }
            
            if (previousEntry != null) {
                val isGood = if (entry.weight > goal) diff <= 0 else if (entry.weight < goal) diff >= 0 else true
                val color = if (isGood) Color(0xFF4CAF50) else Color(0xFFF44336)
                Text(
                    "${if (diff > 0) "+" else ""}${"%.1f".format(diff)}",
                    color = color,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            Text("${"%.1f".format(entry.weight)} kg", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BottomNavigationBar(viewModel: WeightViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { 
                Icon(
                    if (currentScreen == Screen.Dashboard) Icons.Default.Dashboard else Icons.Outlined.Dashboard, 
                    contentDescription = null
                ) 
            },
            label = { Text(stringResource(R.string.dash), fontSize = 10.sp) },
            selected = currentScreen == Screen.Dashboard,
            onClick = { viewModel.navigateTo(Screen.Dashboard) }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    if (currentScreen == Screen.Ledger) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Outlined.List,
                    contentDescription = null
                ) 
            },
            label = { Text(stringResource(R.string.ledger), fontSize = 10.sp) },
            selected = currentScreen == Screen.Ledger,
            onClick = { viewModel.navigateTo(Screen.Ledger) }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    if (currentScreen == Screen.Analytics) Icons.Default.BarChart else Icons.Outlined.BarChart, 
                    contentDescription = null
                ) 
            },
            label = { Text(stringResource(R.string.trends), fontSize = 10.sp) },
            selected = currentScreen == Screen.Analytics,
            onClick = { viewModel.navigateTo(Screen.Analytics) }
        )
        NavigationBarItem(
            icon = { 
                Icon(
                    if (currentScreen == Screen.Settings) Icons.Default.Settings else Icons.Outlined.Settings, 
                    contentDescription = null
                ) 
            },
            label = { Text(stringResource(R.string.config), fontSize = 10.sp) },
            selected = currentScreen == Screen.Settings,
            onClick = { viewModel.navigateTo(Screen.Settings) }
        )
    }
}
