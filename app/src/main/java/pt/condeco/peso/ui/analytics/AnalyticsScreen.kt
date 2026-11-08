package pt.condeco.peso.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.condeco.peso.R
import pt.condeco.peso.domain.Units
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.ui.navigation.BottomNavigationBar
import pt.condeco.peso.ui.navigation.Screen
import pt.condeco.peso.ui.util.shareImage
import pt.condeco.peso.ui.util.shareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: WeightViewModel, current: Screen, onNavigate: (Screen) -> Unit, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chartLayer = rememberGraphicsLayer()
    val unitLabel = Units.weightLabel(uiState.unitSystem)
    val shareMessage = stringResource(
        R.string.share_summary,
        Units.formatWeight(uiState.latestEntry?.weight ?: 0f, uiState.unitSystem),
        unitLabel,
        Units.formatWeight(
            uiState.filteredEntries.let { if (it.size >= 2) it.last().weight - it.first().weight else 0f },
            uiState.unitSystem
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analytics), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val image = runCatching { chartLayer.toImageBitmap().asAndroidBitmap() }.getOrNull()
                            if (image != null) shareImage(context, image, shareMessage) else shareText(context, shareMessage)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(current, onNavigate)
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
                CurrentTrendHeader(uiState)
            }
            item {
                WeightChartCard(
                    uiState,
                    Modifier.drawWithContent {
                        chartLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(chartLayer)
                    }
                )
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
