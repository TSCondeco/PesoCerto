package pt.condeco.peso.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import pt.condeco.peso.R
import pt.condeco.peso.domain.GoalInfo
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.ui.WeightUiState
import pt.condeco.peso.ui.components.CaptionText
import pt.condeco.peso.ui.components.PesoCard
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.Formats
import pt.condeco.peso.ui.util.currentStreakText
import pt.condeco.peso.ui.util.label
import pt.condeco.peso.ui.util.longestStreakText

@Composable
fun GoalCard(uiState: WeightUiState) {
    val info = uiState.goalInfo ?: return
    val latest = uiState.latestEntry?.weight ?: return
    val unit = uiState.unitSystem
    val label = Units.weightLabel(unit)

    PesoCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CaptionText(stringResource(R.string.goal_progress))
            CaptionText("${Units.formatWeight(uiState.goalWeight, unit)} $label")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (info.reached) stringResource(R.string.goal_reached)
            else stringResource(R.string.goal_remaining, Units.formatWeight(abs(latest - uiState.goalWeight), unit), label),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        val progressDescription = stringResource(R.string.goal_progress_description, (info.progress * 100).toInt())
        LinearProgressIndicator(
            progress = { info.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .semantics { contentDescription = progressDescription },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(16.dp))
        GoalRateRow(info, unit)
        if (uiState.streaks.current > 0 || info.milestoneKg > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            StreakAndMilestone(uiState, info)
        }
    }
}

@Composable
private fun GoalRateRow(info: GoalInfo, unit: pt.condeco.peso.domain.UnitSystem) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LabeledValue(
            label = stringResource(R.string.weekly_rate),
            value = info.weeklyRate?.let { "${Units.formatWeightDelta(it, unit)} ${Units.weightLabel(unit)}" }
                ?: stringResource(R.string.not_enough_data),
            modifier = Modifier.weight(1f)
        )
        if (!info.reached) {
            LabeledValue(
                label = stringResource(R.string.projected_date),
                value = info.projectedDate?.let { Formats.dayMonthYear(it) }
                    ?: stringResource(if (info.weeklyRate == null) R.string.not_enough_data else R.string.not_heading_to_goal),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        CaptionText(label, fontSize = 10.sp)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StreakAndMilestone(uiState: WeightUiState, info: GoalInfo) {
    val unit = uiState.unitSystem
    val streaks = uiState.streaks
    val context = LocalContext.current
    if (streaks.current > 0) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = SemanticColors.Streak.copy(alpha = 0.18f), shape = RoundedCornerShape(4.dp)) {
                Text(
                    context.currentStreakText(streaks),
                    color = SemanticColors.Streak,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            if (streaks.longest > streaks.current) {
                CaptionText(context.longestStreakText(streaks))
            }
        }
    }
    if (info.milestoneKg > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.milestone_moved, Units.formatWeight(info.milestoneKg.toFloat(), unit), Units.weightLabel(unit)),
            color = SemanticColors.Milestone,
            fontSize = 12.sp
        )
    }
}
