package pt.condeco.peso.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pt.condeco.peso.data.SettingsRepository
import pt.condeco.peso.data.WeightDao
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.domain.BiologicalSex
import pt.condeco.peso.domain.ThemeMode
import pt.condeco.peso.domain.UnitSystem
import pt.condeco.peso.domain.WeightCsv
import pt.condeco.peso.domain.computeBmi
import pt.condeco.peso.domain.computeGoalInfo
import pt.condeco.peso.domain.computeStats
import pt.condeco.peso.domain.computeStreaks
import pt.condeco.peso.domain.rangeStart
import pt.condeco.peso.notifications.ReminderManager

class WeightViewModel(
    private val weightDao: WeightDao,
    private val settingsRepository: SettingsRepository,
    private val reminderManager: ReminderManager,
    private val onEntriesChanged: () -> Unit = {}
) : ViewModel() {

    private val _editingEntry = MutableStateFlow<WeightEntry?>(null)
    val editingEntry: StateFlow<WeightEntry?> = _editingEntry.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.M1)

    val uiState: StateFlow<WeightUiState> = weightDao.getAllEntries()
        .combine(settingsRepository.settingsFlow) { entries, settings -> Pair(entries, settings) }
        .combine(_selectedTimeRange) { (entries, settings), range ->
            val sortedEntries = entries.sortedBy { it.date }
            val latest = sortedEntries.lastOrNull()
            val previous = if (sortedEntries.size > 1) sortedEntries[sortedEntries.size - 2] else null
            
            val now = System.currentTimeMillis()
            val stats = computeStats(sortedEntries, now)
            val goalInfo = computeGoalInfo(sortedEntries, settings.goalWeight, now)
            val streaks = computeStreaks(sortedEntries, now)

            val bmi = if (latest != null) computeBmi(latest.weight, settings.height) else 0f

            val highest = sortedEntries.maxByOrNull { it.weight }
            val lowest = sortedEntries.minByOrNull { it.weight }

            val startTime = rangeStart(range, now)
            val filtered = sortedEntries.filter { it.date >= startTime }

            WeightUiState(
                allEntries = sortedEntries.reversed(),
                filteredEntries = filtered,
                latestEntry = latest,
                previousEntry = previous,
                avg7d = stats.avg7d,
                prevAvg7d = stats.prevAvg7d,
                avg30d = stats.avg30d,
                netChange = stats.netChange,
                height = settings.height,
                biologicalSex = settings.biologicalSex,
                dateOfBirth = settings.dateOfBirth,
                unitSystem = settings.unitSystem,
                remindersEnabled = settings.remindersEnabled,
                reminderTime = settings.reminderTime,
                reminderDays = settings.reminderDays,
                bmi = bmi,
                highestWeight = highest?.weight ?: 0f,
                highestWeightDate = highest?.date ?: 0,
                lowestWeight = lowest?.weight ?: 0f,
                lowestWeightDate = lowest?.date ?: 0,
                goalWeight = settings.goalWeight,
                goalInfo = goalInfo,
                streaks = streaks,
                selectedTimeRange = range,
                themeMode = settings.themeMode
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeightUiState()
        )

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun openAddWeight() {
        _editingEntry.value = null
        _showBottomSheet.value = true
    }

    fun openEditWeight(entry: WeightEntry) {
        _editingEntry.value = entry
        _showBottomSheet.value = true
    }

    fun closeBottomSheet() {
        _showBottomSheet.value = false
        _editingEntry.value = null
    }

    fun updateHeight(newHeight: Int) { viewModelScope.launch { settingsRepository.updateHeight(newHeight) } }
    fun updateSex(newSex: BiologicalSex) { viewModelScope.launch { settingsRepository.updateSex(newSex) } }
    fun updateDob(newDob: Long) { viewModelScope.launch { settingsRepository.updateDob(newDob) } }
    fun updateUnitSystem(newUnit: UnitSystem) { viewModelScope.launch { settingsRepository.updateUnitSystem(newUnit) } }
    
    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateRemindersEnabled(enabled)
            if (enabled) {
                // Read the persisted time rather than uiState, which may not have emitted yet
                reminderManager.scheduleReminder(settingsRepository.settingsFlow.first().reminderTime)
            } else {
                reminderManager.cancelReminder()
            }
        }
    }

    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            settingsRepository.updateReminderTime(time)
            if (settingsRepository.settingsFlow.first().remindersEnabled) {
                reminderManager.scheduleReminder(time)
            }
        }
    }
    
    fun updateReminderDays(days: Set<String>) { 
        viewModelScope.launch { 
            settingsRepository.updateReminderDays(days)
            // No need to reschedule alarm itself, the receiver checks the days
        } 
    }
    
    fun updateThemeMode(mode: ThemeMode) { viewModelScope.launch { settingsRepository.updateThemeMode(mode) } }

    fun updateGoalWeight(newGoal: Float) { viewModelScope.launch { settingsRepository.updateGoalWeight(newGoal) } }

    fun saveWeight(weight: Float, bodyFat: Float?, date: Long, note: String?) {
        viewModelScope.launch {
            val entry = _editingEntry.value
            if (entry == null) {
                weightDao.insert(WeightEntry(weight = weight, bodyFat = bodyFat, date = date, note = note))
            } else {
                weightDao.insert(entry.copy(weight = weight, bodyFat = bodyFat, date = date, note = note))
            }
            onEntriesChanged()
            closeBottomSheet()
        }
    }

    fun deleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.delete(entry)
            onEntriesChanged()
        }
    }

    fun restoreEntry(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.insert(entry)
            onEntriesChanged()
        }
    }

    fun restoreEntries(entries: List<WeightEntry>) {
        viewModelScope.launch {
            weightDao.insertAll(entries)
            onEntriesChanged()
        }
    }

    fun nukeAllData() {
        viewModelScope.launch {
            weightDao.deleteAll()
            onEntriesChanged()
        }
    }

    fun exportCsv(): String = WeightCsv.export(uiState.value.allEntries)

    fun importCsv(csvContent: String) {
        viewModelScope.launch {
            WeightCsv.parse(csvContent).forEach { weightDao.insert(it) }
            onEntriesChanged()
        }
    }
}
