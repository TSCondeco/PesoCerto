package pt.condeco.peso.ui

import pt.condeco.peso.data.Defaults
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.BiologicalSex
import pt.condeco.peso.domain.GoalInfo
import pt.condeco.peso.domain.Streaks
import pt.condeco.peso.domain.ThemeMode
import pt.condeco.peso.domain.UnitSystem

enum class TimeRange {
    W1, M1, M3, YTD, ALL
}

data class WeightUiState(
    val allEntries: List<WeightEntry> = emptyList(),
    val filteredEntries: List<WeightEntry> = emptyList(),
    val latestEntry: WeightEntry? = null,
    val previousEntry: WeightEntry? = null,
    val avg7d: Float = 0f,
    val prevAvg7d: Float = 0f,
    val avg30d: Float = 0f,
    val netChange: Float = 0f,
    val height: Int = 180,
    val biologicalSex: BiologicalSex = Defaults.SEX,
    val dateOfBirth: Long = Defaults.dateOfBirth(),
    val unitSystem: UnitSystem = Defaults.UNIT_SYSTEM,
    val remindersEnabled: Boolean = false,
    val reminderTime: String = Defaults.REMINDER_TIME,
    val reminderDays: Set<String> = Defaults.REMINDER_DAYS,
    val bmi: Float = 0f,
    val highestWeight: Float = 0f,
    val lowestWeight: Float = 0f,
    val highestWeightDate: Long = 0,
    val lowestWeightDate: Long = 0,
    val goalWeight: Float = 70.0f,
    val goalInfo: GoalInfo? = null,
    val streaks: Streaks = Streaks(0, 0),
    val selectedTimeRange: TimeRange = TimeRange.M1,
    val themeMode: ThemeMode = Defaults.THEME_MODE
)
