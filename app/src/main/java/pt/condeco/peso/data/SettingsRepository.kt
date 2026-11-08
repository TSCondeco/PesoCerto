package pt.condeco.peso.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import pt.condeco.peso.domain.BiologicalSex
import pt.condeco.peso.domain.ThemeMode
import pt.condeco.peso.domain.UnitSystem

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object Defaults {
    val SEX = BiologicalSex.Male
    val UNIT_SYSTEM = UnitSystem.Metric
    const val REMINDER_TIME = "08:00"
    val THEME_MODE = ThemeMode.System
    const val GOAL_WEIGHT = 70.0f
    val REMINDER_DAYS = setOf("1", "2", "3", "4", "5", "6", "7")

    /** 1990-04-12 at local midnight, so it doesn't shift with the device timezone. */
    fun dateOfBirth(): Long = java.time.LocalDate.of(1990, 4, 12)
        .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
}

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val HEIGHT = intPreferencesKey("height")
        val BIOLOGICAL_SEX = stringPreferencesKey("biological_sex")
        val DATE_OF_BIRTH = longPreferencesKey("date_of_birth")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val REMINDER_DAYS = stringSetPreferencesKey("reminder_days")
        val GOAL_WEIGHT = floatPreferencesKey("goal_weight")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                height = preferences[PreferencesKeys.HEIGHT] ?: 180,
                biologicalSex = BiologicalSex.fromName(preferences[PreferencesKeys.BIOLOGICAL_SEX]),
                dateOfBirth = preferences[PreferencesKeys.DATE_OF_BIRTH] ?: Defaults.dateOfBirth(),
                unitSystem = UnitSystem.fromName(preferences[PreferencesKeys.UNIT_SYSTEM]),
                remindersEnabled = preferences[PreferencesKeys.REMINDERS_ENABLED] ?: false,
                reminderTime = preferences[PreferencesKeys.REMINDER_TIME] ?: Defaults.REMINDER_TIME,
                reminderDays = preferences[PreferencesKeys.REMINDER_DAYS] ?: Defaults.REMINDER_DAYS,
                goalWeight = preferences[PreferencesKeys.GOAL_WEIGHT] ?: Defaults.GOAL_WEIGHT,
                themeMode = ThemeMode.fromName(preferences[PreferencesKeys.THEME_MODE])
            )
        }

    suspend fun updateHeight(height: Int) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.HEIGHT] = height }
    }

    suspend fun updateSex(sex: BiologicalSex) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.BIOLOGICAL_SEX] = sex.name }
    }

    suspend fun updateDob(dob: Long) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.DATE_OF_BIRTH] = dob }
    }

    suspend fun updateUnitSystem(unit: UnitSystem) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.UNIT_SYSTEM] = unit.name }
    }

    suspend fun updateRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun updateReminderTime(time: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.REMINDER_TIME] = time }
    }

    suspend fun updateReminderDays(days: Set<String>) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.REMINDER_DAYS] = days }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun updateGoalWeight(goal: Float) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.GOAL_WEIGHT] = goal }
    }
}

data class UserSettings(
    val height: Int,
    val biologicalSex: BiologicalSex,
    val dateOfBirth: Long,
    val unitSystem: UnitSystem,
    val remindersEnabled: Boolean,
    val reminderTime: String,
    val reminderDays: Set<String>,
    val goalWeight: Float,
    val themeMode: ThemeMode = Defaults.THEME_MODE
)
