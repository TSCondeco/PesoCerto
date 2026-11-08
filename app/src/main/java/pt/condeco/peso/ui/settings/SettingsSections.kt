package pt.condeco.peso.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import pt.condeco.peso.R
import pt.condeco.peso.domain.ThemeMode
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.Units
import pt.condeco.peso.ui.WeightUiState
import pt.condeco.peso.ui.theme.SemanticColors
import pt.condeco.peso.ui.util.label

@Composable
fun ProfileSection(
    state: WeightUiState,
    onEditHeight: () -> Unit,
    onEditGoal: () -> Unit,
    onEditSex: () -> Unit,
    onEditDob: () -> Unit
) {
    val unit = state.unitSystem
    SettingsSection(title = stringResource(R.string.subject_profile)) {
        SettingsItem(stringResource(R.string.height), Units.formatHeight(state.height, unit), onEditHeight)
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        SettingsItem(
            stringResource(R.string.goal_weight),
            "${Units.formatWeight(state.goalWeight, unit)} ${Units.weightLabel(unit)}",
            onEditGoal
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        SettingsItem(stringResource(R.string.biological_sex), state.biologicalSex.label(), onEditSex)
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        SettingsItem(
            stringResource(R.string.date_of_birth),
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(state.dateOfBirth)),
            onEditDob
        )
    }
}

@Composable
fun AppearanceSection(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val context = LocalContext.current
    SettingsSection(title = stringResource(R.string.appearance)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.theme), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            EnumSegmentedRow(
                options = ThemeMode.entries,
                selected = themeMode,
                label = { it.label() },
                onSelect = onThemeChange
            )
        }
        // Per-app language settings only exist on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            val language = Locale.getDefault().let { it.getDisplayLanguage(it).replaceFirstChar { c -> c.titlecase(it) } }
            SettingsItem(stringResource(R.string.language), language) { openAppLanguageSettings(context) }
        }
    }
}

@Composable
fun UnitsAndRemindersSection(
    state: WeightUiState,
    onUnitChange: (UnitSystem) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onEditReminderTime: () -> Unit,
    onReminderDaysChange: (Set<String>) -> Unit
) {
    SettingsSection(title = stringResource(R.string.system)) {
        SettingsRow(
            title = stringResource(R.string.unit_system),
            subtitle = stringResource(R.string.determines_format)
        ) {
            EnumSegmentedRow(
                options = UnitSystem.entries,
                selected = state.unitSystem,
                label = { it.label() },
                onSelect = onUnitChange,
                modifier = Modifier.width(200.dp)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        SettingsRow(
            title = stringResource(R.string.reminders),
            subtitle = "${stringResource(R.string.daily_prompt)} ${state.reminderTime}"
        ) {
            Switch(checked = state.remindersEnabled, onCheckedChange = onReminderToggle)
        }
        if (state.remindersEnabled) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            SettingsItem(stringResource(R.string.reminder_time), state.reminderTime, onEditReminderTime)
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            ReminderDaysPicker(selectedDays = state.reminderDays, onChange = onReminderDaysChange)
        }
    }
}

/** Day ids follow [java.util.Calendar.DAY_OF_WEEK] ("1" = Sunday), shown Monday-first. */
private val REMINDER_DAYS = listOf(
    "2" to R.string.mon_short, "3" to R.string.tue_short, "4" to R.string.wed_short,
    "5" to R.string.thu_short, "6" to R.string.fri_short, "7" to R.string.sat_short,
    "1" to R.string.sun_short
)

@Composable
private fun ReminderDaysPicker(selectedDays: Set<String>, onChange: (Set<String>) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(R.string.reminder_days), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            REMINDER_DAYS.forEach { (id, shortLabel) ->
                val isSelected = id in selectedDays
                val dayName = DayOfWeek.of(if (id == "1") 7 else id.toInt() - 1)
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .semantics { contentDescription = dayName }
                        .toggleable(value = isSelected, role = Role.Checkbox) {
                            onChange(if (isSelected) selectedDays - id else selectedDays + id)
                        },
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(shortLabel),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DataSection(onExport: () -> Unit, onImport: () -> Unit) {
    SettingsSection(title = stringResource(R.string.data_management)) {
        SettingsButtonItem(
            label = stringResource(R.string.export_csv),
            icon = Icons.Default.Download,
            color = MaterialTheme.colorScheme.secondary,
            onClick = onExport
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        SettingsButtonItem(
            label = stringResource(R.string.import_csv),
            icon = Icons.Default.FileUpload,
            color = SemanticColors.Positive,
            onClick = onImport
        )
    }
}

@Composable
fun DangerSection(onDeleteAll: () -> Unit) {
    SettingsSection(title = null) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = SemanticColors.Danger, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.danger_zone), fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.irreversible_action),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onDeleteAll) {
                Text(
                    stringResource(R.string.nuke_all_data),
                    color = SemanticColors.Danger,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun VersionFooter(versionName: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.app_version, versionName).uppercase(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun <T> EnumSegmentedRow(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size)
            ) { Text(label(option), maxLines = 1) }
        }
    }
}

private fun openAppLanguageSettings(context: Context) {
    context.startActivity(
        Intent(android.provider.Settings.ACTION_APP_LOCALE_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))
    )
}
