package pt.condeco.peso.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R

private val KEY_ROWS = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", "del")
)

/** Large value readout flanked by − / + buttons. */
@Composable
fun StepperDisplay(
    valueText: String,
    unitLabel: String,
    decreaseDescription: String,
    increaseDescription: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilledTonalIconButton(onClick = onDecrease, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Remove, contentDescription = decreaseDescription)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = valueText,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unitLabel,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }
        FilledTonalIconButton(onClick = onIncrease, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Add, contentDescription = increaseDescription)
        }
    }
}

/** Phone-style keypad; keys are reported as "0"-"9", "." or "del" (see [handleInput]). */
@Composable
fun NumericKeypad(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowDecimal: Boolean = true
) {
    Column(modifier = modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KEY_ROWS.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { key ->
                    if (key == "." && !allowDecimal) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        KeypadButton(key, modifier = Modifier.weight(1f)) { onKey(key) }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(key: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.6f).heightIn(min = 48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (key == "del") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.backspace),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Text(
                    text = key,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
