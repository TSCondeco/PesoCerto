package pt.condeco.peso.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.condeco.peso.R
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.ui.navigation.BottomNavigationBar
import pt.condeco.peso.ui.navigation.Screen
import pt.condeco.peso.ui.util.Formats
import pt.condeco.peso.ui.util.shareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: WeightViewModel, current: Screen, onNavigate: (Screen) -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMsg = stringResource(R.string.entry_deleted)
    val undoLabel = stringResource(R.string.undo)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.the_ledger), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { shareText(context, viewModel.exportCsv(), "text/csv") }) {
                        Icon(Icons.Default.Download, contentDescription = stringResource(R.string.export_csv), tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(current, onNavigate)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            LedgerHeaderStats(uiState)
            
            val groupedEntries = uiState.allEntries.groupBy { 
                Formats.monthYear(it.date)
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
                            unitSystem = uiState.unitSystem,
                            onDelete = {
                                viewModel.deleteEntry(entry)
                                scope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val result = snackbarHostState.showSnackbar(deletedMsg, undoLabel, duration = SnackbarDuration.Long)
                                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreEntry(entry)
                                }
                            },
                            onEdit = { viewModel.openEditWeight(entry) }
                        )
                        if (index < entries.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}
