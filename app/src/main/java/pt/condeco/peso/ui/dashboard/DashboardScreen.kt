package pt.condeco.peso.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.ui.navigation.BottomNavigationBar
import pt.condeco.peso.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: WeightViewModel, current: Screen, onNavigate: (Screen) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
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
                    IconButton(onClick = { onNavigate(Screen.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.config))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(current, onNavigate)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddWeight() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CurrentWeightCard(uiState)
            }
            item {
                GoalCard(uiState)
            }
            item {
                StatsRow(uiState)
            }
            item {
                BmiCard(uiState.bmi)
            }
            item {
                RecentEntriesHeader(onViewLedger = { onNavigate(Screen.Ledger) })
            }
            val entries = uiState.allEntries
            items(entries.take(5)) { entry ->
                RecentEntryItem(
                    entry = entry,
                    previous = entries.getOrNull(entries.indexOf(entry) + 1),
                    goalKg = uiState.goalWeight,
                    unitSystem = uiState.unitSystem,
                    onEdit = { viewModel.openEditWeight(entry) }
                )
            }
        }
    }
}
