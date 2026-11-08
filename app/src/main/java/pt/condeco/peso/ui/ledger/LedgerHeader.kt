package pt.condeco.peso.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.WeightUiState
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.label

@Composable
fun LedgerHeaderStats(uiState: WeightUiState) {
    val unit = uiState.unitSystem
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderStatItem(stringResource(R.string.entries), uiState.allEntries.size.toString(), MaterialTheme.colorScheme.onSurface)
        HeaderStatItem(stringResource(R.string.avg_30d), "${Units.formatWeight(uiState.avg30d, unit)} ${Units.weightLabel(unit)}", MaterialTheme.colorScheme.onSurface)

        val latest = uiState.latestEntry?.weight ?: 0f
        val netColor = SemanticColors.forChange(isTowardGoal(latest, uiState.goalWeight, uiState.netChange))

        HeaderStatItem(stringResource(R.string.net_change), "${Units.formatWeightDelta(uiState.netChange, unit)} ${Units.weightLabel(unit)}", netColor)
    }
}

@Composable
fun HeaderStatItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun MonthHeader(month: String) {
    Box(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(month.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
