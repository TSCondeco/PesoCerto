package pt.condeco.peso.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import pt.condeco.peso.R

/** Standard handle shown at the top of the app's bottom sheets. */
@Composable
fun PesoDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .width(40.dp)
            .height(4.dp)
            .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
    )
}

/**
 * Bottom sheet for entering a single number with the keypad and −/+ steppers,
 * matching the daily weight entry sheet. Values are in display units; [range] bounds what can be saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumericEntrySheet(
    title: String,
    initialValue: Float,
    unitLabel: String,
    stepSize: Float,
    decimals: Int,
    range: ClosedFloatingPointRange<Float>,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf(String.format(Locale.ROOT, "%.${decimals}f", initialValue)) }
    // The first digit replaces the prefilled value instead of being appended to it.
    var untouched by remember { mutableStateOf(true) }
    val value = text.toFloatOrNull()
    val isValid = value != null && value in range

    fun adjust(delta: Float) {
        text = step(text, delta, decimals)
        untouched = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { PesoDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            StepperDisplay(
                valueText = text,
                unitLabel = unitLabel.uppercase(),
                decreaseDescription = stringResource(R.string.decrease),
                increaseDescription = stringResource(R.string.increase),
                onDecrease = { adjust(-stepSize) },
                onIncrease = { adjust(stepSize) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            NumericKeypad(
                allowDecimal = decimals > 0,
                onKey = { key ->
                    val base = if (untouched && key != "del") "0" else text
                    text = handleInput(base, key).take(MAX_INPUT_LENGTH)
                    untouched = false
                }
            )
            Button(
                onClick = {
                    value?.let(onConfirm)
                    onDismiss()
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(stringResource(R.string.save), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Check, contentDescription = null)
                }
            }
        }
    }
}

private const val MAX_INPUT_LENGTH = 6
