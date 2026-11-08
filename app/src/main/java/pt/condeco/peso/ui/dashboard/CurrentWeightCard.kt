package pt.condeco.peso.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import pt.condeco.peso.R
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.WeightUiState
import pt.condeco.peso.ui.components.CaptionText
import pt.condeco.peso.ui.components.ChangeChip
import pt.condeco.peso.ui.components.PesoCard
import pt.condeco.peso.ui.util.Formats

@Composable
fun CurrentWeightCard(uiState: WeightUiState) {
    val latest = uiState.latestEntry
    val unit = uiState.unitSystem
    val diff = if (latest != null && uiState.previousEntry != null) latest.weight - uiState.previousEntry.weight else 0f

    PesoCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CaptionText(stringResource(R.string.current_weight))
            CaptionText(latest?.let { Formats.dayMonth(it.date) }?.uppercase() ?: "-- ---")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = Units.formatWeight(latest?.weight ?: 0f, unit),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                Units.weightLabel(unit),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ChangeChip(
            text = Units.formatWeight(abs(diff), unit),
            towardGoal = isTowardGoal(latest?.weight ?: 0f, uiState.goalWeight, diff),
            isDecrease = diff <= 0,
            caption = stringResource(R.string.since_yesterday)
        )
    }
}
