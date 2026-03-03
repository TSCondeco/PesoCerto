package pt.condeco.peso.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
fun LedgerScreen(viewModel: WeightViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.the_ledger), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Export */ }) {
                        Icon(Icons.Default.Download, contentDescription = stringResource(R.string.export_csv), tint = Color(0xFF3F51B5))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(viewModel)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.White)) {
            LedgerHeaderStats(uiState)
            
            val groupedEntries = uiState.allEntries.groupBy { 
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.date))
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                groupedEntries.forEach { (month, entries) ->
                    item {
                        MonthHeader(month)
                    }
                    itemsIndexed(entries) { index, entry ->
                        val prevEntry = if (index + 1 < entries.size) entries[index + 1] else null
                        LedgerEntryItem(
                            entry = entry, 
                            prevEntry = prevEntry,
                            goal = uiState.goalWeight,
                            onDelete = { viewModel.deleteEntry(entry) },
                            onEdit = { viewModel.openEditWeight(entry) }
                        )
                        if (index < entries.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerHeaderStats(uiState: WeightUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderStatItem(stringResource(R.string.entries), uiState.allEntries.size.toString(), Color.Black)
        HeaderStatItem(stringResource(R.string.avg_30d), "${"%.1f".format(uiState.avg30d)} kg", Color.Black)
        
        val latest = uiState.latestEntry?.weight ?: 0f
        val goal = uiState.goalWeight
        val isGoodNet = if (latest > goal) uiState.netChange <= 0 else if (latest < goal) uiState.netChange >= 0 else true
        val netColor = if (isGoodNet) Color(0xFF00BFA5) else Color(0xFFF44336)
        
        HeaderStatItem(stringResource(R.string.net_change), "${if (uiState.netChange > 0) "+" else ""}${"%.1f".format(uiState.netChange)} kg", netColor)
    }
}

@Composable
fun HeaderStatItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun MonthHeader(month: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FB)).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(month.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

@Composable
fun LedgerEntryItem(entry: WeightEntry, prevEntry: WeightEntry?, goal: Float, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showOptions by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().clickable { showOptions = !showOptions }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(SimpleDateFormat("dd", Locale.getDefault()).format(Date(entry.date)), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(SimpleDateFormat("MMM", Locale.getDefault()).format(Date(entry.date)).uppercase(), fontSize = 10.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${"%.1f".format(entry.weight)} kg", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                entry.bodyFat?.let {
                    Text("${"%.1f".format(it)}% ${stringResource(R.string.body_fat)}", fontSize = 12.sp, color = Color.LightGray)
                }
            }
            
            if (prevEntry != null) {
                val diff = entry.weight - prevEntry.weight
                val isGood = if (entry.weight > goal) diff <= 0 else if (entry.weight < goal) diff >= 0 else true
                val color = if (isGood) Color(0xFF00BFA5) else Color(0xFFF44336)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (diff <= 0) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("%.1f".format(abs(diff)), color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else {
                Text("- 0.0", color = Color.LightGray, fontSize = 14.sp)
            }
        }
        
        if (showOptions) {
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFFE8EAF6))
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { 
                        onEdit()
                        showOptions = false
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = Color(0xFF3F51B5))
                }
                Box(
                    modifier = Modifier.width(64.dp).fillMaxHeight().background(Color(0xFFEF5350)).clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color.White)
                }
            }
        }
    }
}
