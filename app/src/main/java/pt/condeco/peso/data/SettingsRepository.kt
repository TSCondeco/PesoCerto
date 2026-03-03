package pt.condeco.peso.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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
                biologicalSex = preferences[PreferencesKeys.BIOLOGICAL_SEX] ?: "Male",
                dateOfBirth = preferences[PreferencesKeys.DATE_OF_BIRTH] ?: 640022400000L, // 1990-04-12
                unitSystem = preferences[PreferencesKeys.UNIT_SYSTEM] ?: "Metric",
                remindersEnabled = preferences[PreferencesKeys.REMINDERS_ENABLED] ?: false,
                reminderTime = preferences[PreferencesKeys.REMINDER_TIME] ?: "08:00",
                reminderDays = preferences[PreferencesKeys.REMINDER_DAYS] ?: setOf("1", "2", "3", "4", "5", "6", "7"),
                goalWeight = preferences[PreferencesKeys.GOAL_WEIGHT] ?: 70.0f
            )
        }

    suspend fun updateHeight(height: Int) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.HEIGHT] = height }
    }

    suspend fun updateSex(sex: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.BIOLOGICAL_SEX] = sex }
    }

    suspend fun updateDob(dob: Long) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.DATE_OF_BIRTH] = dob }
    }

    suspend fun updateUnitSystem(unit: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.UNIT_SYSTEM] = unit }
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

    suspend fun updateGoalWeight(goal: Float) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.GOAL_WEIGHT] = goal }
    }
}

data class UserSettings(
    val height: Int,
    val biologicalSex: String,
    val dateOfBirth: Long,
    val unitSystem: String,
    val remindersEnabled: Boolean,
    val reminderTime: String,
    val reminderDays: Set<String>,
    val goalWeight: Float
)
