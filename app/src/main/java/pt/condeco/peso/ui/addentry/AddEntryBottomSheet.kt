package pt.condeco.peso.ui.addentry

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import pt.condeco.peso.R
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.ui.components.NumericKeypad
import pt.condeco.peso.ui.components.PesoDragHandle
import pt.condeco.peso.ui.components.StepperDisplay
import pt.condeco.peso.ui.components.handleInput
import pt.condeco.peso.ui.components.step
import pt.condeco.peso.ui.util.Formats
import pt.condeco.peso.ui.util.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryBottomSheet(viewModel: WeightViewModel) {
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val editingEntry by viewModel.editingEntry.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeBottomSheet() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { PesoDragHandle() }
        ) {
            AddEntryContent(
                editingEntry = editingEntry,
                unitSystem = uiState.unitSystem,
                lastWeight = uiState.latestEntry?.weight,
                onSave = { weight, bf, date, note ->
                    viewModel.saveWeight(weight, bf, date, note)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryContent(
    editingEntry: WeightEntry?,
    unitSystem: UnitSystem,
    lastWeight: Float?,
    onSave: (Float, Float?, Long, String?) -> Unit
) {
    var isWeightMode by remember { mutableStateOf(true) }
    var weightInput by remember {
        mutableStateOf((editingEntry?.weight ?: lastWeight)?.let { Units.formatWeight(it, unitSystem).replace(",", ".") } ?: "0")
    }
    var noteInput by remember { mutableStateOf(editingEntry?.note ?: "") }
    var bfInput by remember { mutableStateOf(editingEntry?.bodyFat?.let { String.format(Locale.ROOT, "%.1f", it) } ?: "") }
    var entryDate by remember { mutableLongStateOf(editingEntry?.date ?: System.currentTimeMillis()) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val selectDateLabel = stringResource(R.string.select_date)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector and Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                TabItem(stringResource(R.string.weight), isWeightMode) { isWeightMode = true }
                TabItem(stringResource(R.string.body_fat), !isWeightMode) { isWeightMode = false }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                onClick = { showDatePicker = true },
                modifier = Modifier.semantics { contentDescription = selectDateLabel }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Formats.weekdayDayMonth(entryDate),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        StepperDisplay(
            valueText = if (isWeightMode) weightInput else bfInput.ifEmpty { "0" },
            unitLabel = if (isWeightMode) Units.weightLabel(unitSystem).uppercase() else "%",
            decreaseDescription = stringResource(R.string.weight_decrease),
            increaseDescription = stringResource(R.string.weight_increase),
            onDecrease = { if (isWeightMode) weightInput = step(weightInput, -0.1f) else bfInput = step(bfInput, -0.1f) },
            onIncrease = { if (isWeightMode) weightInput = step(weightInput, 0.1f) else bfInput = step(bfInput, 0.1f) }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        NumericKeypad(onKey = { key ->
            if (isWeightMode) weightInput = handleInput(weightInput, key) else bfInput = handleInput(bfInput, key)
        })

        OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it.take(200) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            label = { Text(stringResource(R.string.note_hint)) },
            singleLine = true
        )

        // Save Button
        Button(
            onClick = {
                // Input is in the display unit; storage is always kg
                val weight = Units.fromDisplayWeight(weightInput.replace(",", ".").toFloatOrNull() ?: 0f, unitSystem)
                val bf = bfInput.replace(",", ".").toFloatOrNull()
                onSave(weight, bf, entryDate, noteInput.trim().ifEmpty { null })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.save_entry), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = entryDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { entryDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
