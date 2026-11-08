package pt.condeco.peso.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import pt.condeco.peso.ui.components.CaptionText
import pt.condeco.peso.ui.components.PesoCard
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.label

@Composable
fun StatsRow(uiState: WeightUiState) {
    val unit = uiState.unitSystem
    val latest = uiState.latestEntry?.weight ?: 0f
    val netTowardGoal = isTowardGoal(latest, uiState.goalWeight, uiState.netChange)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatSmallCard(stringResource(R.string.avg_7d), Units.formatWeight(uiState.avg7d, unit), Modifier.weight(1f))
        StatSmallCard(stringResource(R.string.prev_7d), Units.formatWeight(uiState.prevAvg7d, unit), Modifier.weight(1f))
        StatSmallCard(
            label = stringResource(R.string.net),
            value = Units.formatWeight(uiState.netChange, unit),
            modifier = Modifier.weight(1f),
            valueColor = SemanticColors.forChange(netTowardGoal)
        )
    }
}

@Composable
private fun StatSmallCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    PesoCard(modifier = modifier, contentPadding = 12.dp) {
        CaptionText(label, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
