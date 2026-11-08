package pt.condeco.peso.ui.analytics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import pt.condeco.peso.ui.TimeRange
import pt.condeco.peso.ui.util.label

@Composable
fun TimeRangeSelector(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            val label = stringResource(
                when (range) {
                    TimeRange.W1 -> R.string.range_1w
                    TimeRange.M1 -> R.string.range_1m
                    TimeRange.M3 -> R.string.range_3m
                    TimeRange.YTD -> R.string.range_ytd
                    TimeRange.ALL -> R.string.range_all
                }
            )
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onRangeSelected(range) }
                    ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(modifier = Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
