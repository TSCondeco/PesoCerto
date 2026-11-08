package pt.condeco.peso.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.WeightUiState
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.Formats
import pt.condeco.peso.ui.util.label

@Composable
fun StatsGrid(uiState: WeightUiState) {
    val entries = uiState.filteredEntries
    val goal = uiState.goalWeight
    val latest = uiState.latestEntry?.weight ?: 0f
    val unit = uiState.unitSystem

    val highest = entries.maxByOrNull { it.weight }
    val lowest = entries.minByOrNull { it.weight }
    val change = if (entries.size >= 2) entries.last().weight - entries.first().weight else 0f
    val avg = if (entries.isNotEmpty()) entries.map { it.weight }.average().toFloat() else 0f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                label = stringResource(R.string.highest),
                value = highest?.weight ?: 0f,
                subtext = highest?.let { Formats.dayMonthYear(it.date) } ?: "--",
                unitSystem = unit,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.lowest),
                value = lowest?.weight ?: 0f,
                subtext = lowest?.let { Formats.dayMonthYear(it.date) } ?: "--",
                unitSystem = unit,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val isGoodChange = isTowardGoal(latest, goal, change)
            StatCard(
                label = stringResource(R.string.period_change),
                value = change,
                subtext = stringResource(R.string.period_change),
                isChange = true,
                isGood = isGoodChange,
                unitSystem = unit,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.avg_weight),
                value = avg,
                subtext = stringResource(R.string.period_average),
                unitSystem = unit,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: Float, subtext: String, unitSystem: UnitSystem, modifier: Modifier = Modifier, isChange: Boolean = false, isGood: Boolean = true) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${if (isChange && value > 0) "+" else ""}${Units.formatWeight(value, unitSystem)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChange) {
                        SemanticColors.forChange(isGood)
                    } else MaterialTheme.colorScheme.onSurface
                )
                Text(" ${Units.weightLabel(unitSystem)}", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(subtext, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}
