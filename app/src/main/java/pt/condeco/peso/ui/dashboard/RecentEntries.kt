package pt.condeco.peso.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.components.CaptionText
import pt.condeco.peso.ui.components.PesoCard
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.Formats

@Composable
fun RecentEntriesHeader(onViewLedger: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.recent_entries), fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.view_ledger),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onViewLedger)
                .padding(vertical = 12.dp)
        )
    }
}

/** A compact row for one entry; [previous] is the chronologically preceding entry, if any. */
@Composable
fun RecentEntryItem(
    entry: WeightEntry,
    previous: WeightEntry?,
    goalKg: Float,
    unitSystem: UnitSystem,
    onEdit: () -> Unit
) {
    PesoCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(Formats.dayMonth(entry.date), fontWeight = FontWeight.SemiBold)
                CaptionText(Formats.time(entry.date))
            }
            if (previous != null) {
                val diff = entry.weight - previous.weight
                Text(
                    Units.formatWeightDelta(diff, unitSystem),
                    color = SemanticColors.forChange(isTowardGoal(entry.weight, goalKg, diff)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            Text(
                "${Units.formatWeight(entry.weight, unitSystem)} ${Units.weightLabel(unitSystem)}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
