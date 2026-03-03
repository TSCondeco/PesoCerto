package pt.condeco.peso.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.condeco.peso.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WeightViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    var showHeightDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showSexDialog by remember { mutableStateOf(false) }
    var showDobDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                content?.let { csv -> viewModel.importCsv(csv) }
            } catch (e: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.configuration), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(viewModel)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FB)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsSection(title = stringResource(R.string.subject_profile)) {
                    SettingsItem(label = stringResource(R.string.height), value = "${uiState.height} cm") {
                        showHeightDialog = true
                    }
                    HorizontalDivider(color = Color(0xFFF0F2F5))
                    SettingsItem(label = stringResource(R.string.goal_weight), value = "${uiState.goalWeight} kg") {
                        showGoalDialog = true
                    }
                    HorizontalDivider(color = Color(0xFFF0F2F5))
                    
                    val sexLabel = when(uiState.biologicalSex) {
                        "Male" -> stringResource(R.string.male)
                        "Female" -> stringResource(R.string.female)
                        else -> stringResource(R.string.other)
                    }
                    SettingsItem(label = stringResource(R.string.biological_sex), value = sexLabel) {
                        showSexDialog = true
                    }
                    HorizontalDivider(color = Color(0xFFF0F2F5))
                    SettingsItem(label = stringResource(R.string.date_of_birth), value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(uiState.dateOfBirth))) {
                        showDobDialog = true
                    }
                }
            }
            
            item {
                Text(
                    stringResource(R.string.biometric_note),
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item {
                SettingsSection(title = stringResource(R.string.system)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.unit_system), fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.determines_format), fontSize = 12.sp, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF0F2F5),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                val isMetric = uiState.unitSystem == "Metric"
                                Text(
                                    stringResource(R.string.metric),
                                    modifier = Modifier
                                        .background(if (isMetric) Color.White else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.updateUnitSystem("Metric") }
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = if (isMetric) Color(0xFF3F51B5) else Color.Gray,
                                    fontWeight = if (isMetric) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    stringResource(R.string.imperial),
                                    modifier = Modifier
                                        .background(if (!isMetric) Color.White else Color.Transparent, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.updateUnitSystem("Imperial") }
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = if (!isMetric) Color(0xFF3F51B5) else Color.Gray,
                                    fontWeight = if (!isMetric) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F2F5))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.reminders), fontWeight = FontWeight.SemiBold)
                            Text("${stringResource(R.string.daily_prompt)} ${uiState.reminderTime}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = uiState.remindersEnabled, 
                            onCheckedChange = { viewModel.toggleReminders(it) }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.data_management)) {
                    SettingsButtonItem(
                        label = stringResource(R.string.export_csv),
                        icon = Icons.Default.Download,
                        color = Color(0xFF3F51B5)
                    ) {
                        val csv = viewModel.exportCsv()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/csv"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                    HorizontalDivider(color = Color(0xFFF0F2F5))
                    SettingsButtonItem(
                        label = stringResource(R.string.import_csv),
                        icon = Icons.Default.FileUpload,
                        color = Color(0xFF4CAF50)
                    ) {
                        importLauncher.launch("*/*")
                    }
                }
            }

            item {
                SettingsSection(title = null) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(stringResource(R.string.danger_zone), fontWeight = FontWeight.Bold)
                                Text(stringResource(R.string.irreversible_action), fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.nuke_all_data),
                            color = Color(0xFFEF5350),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { viewModel.nukeAllData() }
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("V1.0.4 (BUILD 89)", fontSize = 12.sp, color = Color.LightGray, letterSpacing = 1.sp)
                    Text("...", color = Color.LightGray)
                }
            }
        }
    }

    if (showHeightDialog) {
        var tempHeight by remember { mutableStateOf(uiState.height.toString()) }
        AlertDialog(
            onDismissRequest = { showHeightDialog = false },
            title = { Text(stringResource(R.string.update_height)) },
            text = {
                OutlinedTextField(
                    value = tempHeight,
                    onValueChange = { if (it.all { char -> char.isDigit() }) tempHeight = it },
                    label = { Text("${stringResource(R.string.height)} (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    tempHeight.toIntOrNull()?.let { viewModel.updateHeight(it) }
                    showHeightDialog = false
                }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    if (showGoalDialog) {
        var tempGoal by remember { mutableStateOf(uiState.goalWeight.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text(stringResource(R.string.update_goal)) },
            text = {
                OutlinedTextField(
                    value = tempGoal,
                    onValueChange = { tempGoal = it },
                    label = { Text("${stringResource(R.string.goal_weight)} (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            },
            confirmButton = {
                Button(onClick = {
                    tempGoal.replace(",", ".").toFloatOrNull()?.let { viewModel.updateGoalWeight(it) }
                    showGoalDialog = false
                }) { Text(stringResource(R.string.ok)) }
            }
        )
    }

    if (showSexDialog) {
        AlertDialog(
            onDismissRequest = { showSexDialog = false },
            title = { Text(stringResource(R.string.biological_sex)) },
            text = {
                Column {
                    listOf("Male", "Female", "Other").forEach { sex ->
                        val sexLabel = when(sex) {
                            "Male" -> stringResource(R.string.male)
                            "Female" -> stringResource(R.string.female)
                            else -> stringResource(R.string.other)
                        }
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                viewModel.updateSex(sex)
                                showSexDialog = false
                            }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = uiState.biologicalSex == sex, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(sexLabel)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showDobDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateOfBirth
        )
        DatePickerDialog(
            onDismissRequest = { showDobDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateDob(it) }
                    showDobDialog = false
                }) { Text(stringResource(R.string.ok)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SettingsSection(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column {
        if (title != null) {
            Text(
                title,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Color(0xFF3F51B5), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun SettingsButtonItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = color, fontWeight = FontWeight.Bold)
    }
}
