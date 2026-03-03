package pt.condeco.peso.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pt.condeco.peso.data.SettingsRepository
import pt.condeco.peso.data.WeightDao
import pt.condeco.peso.data.WeightEntry
import pt.condeco.peso.notifications.ReminderManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

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
    val biologicalSex: String = "Male",
    val dateOfBirth: Long = 640022400000L,
    val unitSystem: String = "Metric",
    val remindersEnabled: Boolean = false,
    val reminderTime: String = "07:00",
    val reminderDays: Set<String> = setOf("1", "2", "3", "4", "5", "6", "7"),
    val bmi: Float = 0f,
    val highestWeight: Float = 0f,
    val lowestWeight: Float = 0f,
    val highestWeightDate: Long = 0,
    val lowestWeightDate: Long = 0,
    val totalLoss: Float = 0f,
    val dailyAvg: Float = 0f,
    val goalWeight: Float = 70.0f,
    val selectedTimeRange: TimeRange = TimeRange.M1
)

class WeightViewModel(
    private val weightDao: WeightDao,
    private val settingsRepository: SettingsRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

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
            
            val last7 = sortedEntries.takeLast(7)
            val avg7 = if (last7.isNotEmpty()) last7.map { it.weight }.average().toFloat() else 0f
            val prev7 = if (sortedEntries.size > 7) sortedEntries.dropLast(7).takeLast(7).map { it.weight }.average().toFloat() else 0f
            
            val last30 = sortedEntries.filter { it.date > System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000 }
            val avg30 = if (last30.isNotEmpty()) last30.map { it.weight }.average().toFloat() else 0f
            
            val net = if (sortedEntries.isNotEmpty()) sortedEntries.last().weight - sortedEntries.first().weight else 0f
            
            val bmi = if (latest != null) latest.weight / (settings.height / 100f).pow(2) else 0f

            val highest = sortedEntries.maxByOrNull { it.weight }
            val lowest = sortedEntries.minByOrNull { it.weight }

            val startTime = when (range) {
                TimeRange.W1 -> System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
                TimeRange.M1 -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                TimeRange.M3 -> System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
                TimeRange.YTD -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.timeInMillis
                }
                TimeRange.ALL -> 0L
            }
            val filtered = sortedEntries.filter { it.date >= startTime }

            WeightUiState(
                allEntries = sortedEntries.reversed(),
                filteredEntries = filtered,
                latestEntry = latest,
                previousEntry = previous,
                avg7d = avg7,
                prevAvg7d = prev7,
                avg30d = avg30,
                netChange = net,
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
                totalLoss = net,
                dailyAvg = avg30,
                goalWeight = settings.goalWeight,
                selectedTimeRange = range
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeightUiState()
        )

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

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
    fun updateSex(newSex: String) { viewModelScope.launch { settingsRepository.updateSex(newSex) } }
    fun updateDob(newDob: Long) { viewModelScope.launch { settingsRepository.updateDob(newDob) } }
    fun updateUnitSystem(newUnit: String) { viewModelScope.launch { settingsRepository.updateUnitSystem(newUnit) } }
    
    fun toggleReminders(enabled: Boolean) { 
        viewModelScope.launch { 
            settingsRepository.updateRemindersEnabled(enabled)
            if (enabled) {
                reminderManager.scheduleReminder(uiState.value.reminderTime)
            } else {
                reminderManager.cancelReminder()
            }
        } 
    }
    
    fun updateReminderTime(time: String) { 
        viewModelScope.launch { 
            settingsRepository.updateReminderTime(time)
            if (uiState.value.remindersEnabled) {
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
    
    fun updateGoalWeight(newGoal: Float) { viewModelScope.launch { settingsRepository.updateGoalWeight(newGoal) } }

    fun saveWeight(weight: Float, bodyFat: Float?, date: Long) {
        viewModelScope.launch {
            val entry = _editingEntry.value
            if (entry == null) {
                weightDao.insert(WeightEntry(weight = weight, bodyFat = bodyFat, date = date))
            } else {
                weightDao.insert(entry.copy(weight = weight, bodyFat = bodyFat, date = date))
            }
            closeBottomSheet()
        }
    }

    fun deleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.delete(entry)
        }
    }

    fun nukeAllData() {
        viewModelScope.launch {
            uiState.value.allEntries.forEach { weightDao.delete(it) }
        }
    }

    fun exportCsv(): String {
        val builder = StringBuilder()
        builder.append("Data,Peso\n")
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        uiState.value.allEntries.sortedBy { it.date }.forEach { entry ->
            builder.append("${dateFormat.format(Date(entry.date))},\"${"%.1f".format(entry.weight).replace(".", ",")}\"\n")
        }
        return builder.toString()
    }

    fun importCsv(csvContent: String) {
        viewModelScope.launch {
            val lines = csvContent.lines()
            val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val csvSplitRegex = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()

            lines.drop(1).forEach { line ->
                if (line.isBlank()) return@forEach
                val parts = if (line.contains("\t")) line.split("\t") else line.split(csvSplitRegex)
                if (parts.size >= 2) {
                    try {
                        val datePart = parts[0].trim().removeSurrounding("\"")
                        val weightPart = parts[1].trim().removeSurrounding("\"").replace(",", ".")
                        val date = dateFormat.parse(datePart)?.time ?: System.currentTimeMillis()
                        val weight = weightPart.toFloat()
                        weightDao.insert(WeightEntry(weight = weight, date = date))
                    } catch (e: Exception) {}
                }
            }
        }
    }
}

sealed class Screen {
    object Dashboard : Screen()
    object Ledger : Screen()
    object Analytics : Screen()
    object Settings : Screen()
}

class WeightViewModelFactory(
    private val weightDao: WeightDao,
    private val settingsRepository: SettingsRepository,
    private val reminderManager: ReminderManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeightViewModel(weightDao, settingsRepository, reminderManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
