package pt.condeco.peso.ui.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.domain.isTowardGoal
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.Formats

/** Floating card describing the chart point under the user's finger. */
@Composable
internal fun ChartTooltip(
    entry: WeightEntry,
    previous: WeightEntry?,
    goalKg: Float,
    unit: UnitSystem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                "${Formats.dayMonthYear(entry.date)} · ${Formats.time(entry.date)}",
                fontSize = 10.sp
            )
            Text(
                "${Units.formatWeight(entry.weight, unit)} ${Units.weightLabel(unit)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            previous?.let {
                val diff = entry.weight - it.weight
                Text(
                    "${Units.formatWeightDelta(diff, unit)} ${Units.weightLabel(unit)}",
                    fontSize = 11.sp,
                    color = SemanticColors.forChange(isTowardGoal(entry.weight, goalKg, diff))
                )
            }
            entry.bodyFat?.let {
                Text("${Formats.decimal(it)}% ${stringResource(R.string.body_fat)}", fontSize = 10.sp)
            }
        }
    }
}
