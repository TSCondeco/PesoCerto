package pt.condeco.peso.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.data.WeightEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryBottomSheet(viewModel: WeightViewModel) {
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val editingEntry by viewModel.editingEntry.collectAsState()
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeBottomSheet() },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                )
            }
        ) {
            AddEntryContent(
                editingEntry = editingEntry,
                onSave = { weight, bf, date ->
                    viewModel.saveWeight(weight, bf, date)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryContent(
    editingEntry: WeightEntry?,
    onSave: (Float, Float?, Long) -> Unit
) {
    var isWeightMode by remember { mutableStateOf(true) }
    var weightInput by remember { mutableStateOf(editingEntry?.weight?.let { "%.1f".format(it).replace(",", ".") } ?: "0") }
    var bfInput by remember { mutableStateOf(editingEntry?.bodyFat?.let { "%.1f".format(it).replace(",", ".") } ?: "") }
    var entryDate by remember { mutableLongStateOf(editingEntry?.date ?: System.currentTimeMillis()) }
    
    var showDatePicker by remember { mutableStateOf(false) }

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
                    .background(Color(0xFFF0F2F5), RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                TabItem("WEIGHT", isWeightMode) { isWeightMode = true }
                TabItem("BODY FAT %", !isWeightMode) { isWeightMode = false }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(entryDate)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isWeightMode) weightInput else if (bfInput.isEmpty()) "0" else bfInput,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = if (isWeightMode) "KG" else "%",
                fontSize = 14.sp,
                color = Color.Gray,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFF0F2F5))

        // Custom Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", "del")
        )
        
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { key ->
                        KeypadButton(key, modifier = Modifier.weight(1f)) {
                            if (isWeightMode) {
                                weightInput = handleInput(weightInput, key)
                            } else {
                                bfInput = handleInput(bfInput, key)
                            }
                        }
                    }
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val weight = weightInput.replace(",", ".").toFloatOrNull() ?: 0f
                val bf = bfInput.replace(",", ".").toFloatOrNull()
                onSave(weight, bf, entryDate)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0056D2))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Save Entry", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable { onClick() },
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF3F51B5) else Color.Gray
        )
    }
}

@Composable
fun KeypadButton(key: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.6f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (key == "del") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = Color(0xFF1A1C1E)
                )
            } else {
                Text(
                    text = key,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1C1E)
                )
            }
        }
    }
}

private fun handleInput(current: String, input: String): String {
    if (input == "del") {
        if (current.length <= 1) return "0"
        return current.dropLast(1)
    }
    if (input == ".") {
        if (current.contains(".")) return current
        return current + "."
    }
    if (current == "0") return input
    return current + input
}
