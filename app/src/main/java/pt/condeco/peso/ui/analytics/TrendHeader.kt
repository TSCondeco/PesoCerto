package pt.condeco.peso.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.WeightUiState
import pt.condeco.peso.ui.theme.SemanticColors

@Composable
fun CurrentTrendHeader(uiState: WeightUiState) {
    val entries = uiState.filteredEntries
    val diff = if (entries.size >= 2) entries.last().weight - entries.first().weight else 0f
    val goal = uiState.goalWeight
    val latest = uiState.latestEntry?.weight ?: 0f
    val unit = uiState.unitSystem

    val isGoodChange = isTowardGoal(latest, goal, diff)

    Column {
        Text(stringResource(R.string.current_trend), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = Units.formatWeight(latest, unit),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(" ${Units.weightLabel(unit)}", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            }
            
            val color = SemanticColors.forChange(isGoodChange)
            val bgColor = color.copy(alpha = 0.15f)

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
                        text = "${if (diff > 0) "+" else ""}${Units.formatWeight(diff, unit)} ${Units.weightLabel(unit)}",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
