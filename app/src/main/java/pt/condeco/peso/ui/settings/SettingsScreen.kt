package pt.condeco.peso.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import pt.condeco.peso.R
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.ui.navigation.BottomNavigationBar
import pt.condeco.peso.ui.navigation.Screen
import pt.condeco.peso.ui.util.shareText

private enum class SettingsDialog { Height, Goal, Sex, DateOfBirth, ReminderTime, DeleteAll }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WeightViewModel,
    current: Screen,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dataDeletedMessage = stringResource(R.string.data_deleted)
    val undoLabel = stringResource(R.string.undo)
    val versionName = remember { context.appVersionName() }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { context.readText(it)?.let(viewModel::importCsv) }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.toggleReminders(true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.configuration), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(current, onNavigate) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                ProfileSection(
                    state = state,
                    onEditHeight = { dialog = SettingsDialog.Height },
                    onEditGoal = { dialog = SettingsDialog.Goal },
                    onEditSex = { dialog = SettingsDialog.Sex },
                    onEditDob = { dialog = SettingsDialog.DateOfBirth }
                )
            }
            item {
                Text(
                    stringResource(R.string.biometric_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            item { AppearanceSection(state.themeMode, viewModel::updateThemeMode) }
            item {
                UnitsAndRemindersSection(
                    state = state,
                    onUnitChange = viewModel::updateUnitSystem,
                    onReminderToggle = { enabled ->
                        if (enabled && !context.hasNotificationPermission()) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleReminders(enabled)
                        }
                    },
                    onEditReminderTime = { dialog = SettingsDialog.ReminderTime },
                    onReminderDaysChange = viewModel::updateReminderDays
                )
            }
            item {
                DataSection(
                    onExport = { shareText(context, viewModel.exportCsv(), "text/csv") },
                    onImport = { importLauncher.launch("*/*") }
                )
            }
            item { DangerSection(onDeleteAll = { dialog = SettingsDialog.DeleteAll }) }
            item { VersionFooter(versionName) }
        }
    }

    val dismiss = { dialog = null }
    when (dialog) {
        SettingsDialog.Height -> HeightSheet(state.height, state.unitSystem, viewModel::updateHeight, dismiss)
        SettingsDialog.Goal -> GoalWeightSheet(state.goalWeight, state.unitSystem, viewModel::updateGoalWeight, dismiss)
        SettingsDialog.Sex -> BiologicalSexDialog(state.biologicalSex, viewModel::updateSex, dismiss)
        SettingsDialog.DateOfBirth -> DateOfBirthDialog(state.dateOfBirth, viewModel::updateDob, dismiss)
        SettingsDialog.ReminderTime -> ReminderTimeDialog(state.reminderTime, viewModel::updateReminderTime, dismiss)
        SettingsDialog.DeleteAll -> DeleteAllDialog(
            entryCount = state.allEntries.size,
            onConfirm = {
                val snapshot = state.allEntries
                viewModel.nukeAllData()
                scope.launch {
                    val result = snackbarHostState.showSnackbar(dataDeletedMessage, undoLabel, duration = SnackbarDuration.Long)
                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreEntries(snapshot)
                }
            },
            onDismiss = dismiss
        )
        null -> Unit
    }
}

private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun Context.appVersionName(): String =
    runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull().orEmpty()

private fun Context.readText(uri: Uri): String? =
    runCatching { contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()
