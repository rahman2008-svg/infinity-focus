package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.UsageTracker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository

    // Permissions and system states
    val isUsagePermissionGranted = MutableStateFlow(false)
    val isAccessibilityPermissionGranted = MutableStateFlow(false)

    // Current Date
    val todayDateString = MutableStateFlow("")

    // Settings
    private val _settings = MutableStateFlow(SystemSettings())
    val settings: StateFlow<SystemSettings> = _settings.asStateFlow()

    // Real App Usage
    private val _todayUsageList = MutableStateFlow<List<AppUsageRecord>>(emptyList())
    val todayUsageList: StateFlow<List<AppUsageRecord>> = _todayUsageList.asStateFlow()

    // All database tables
    val focusSessions: StateFlow<List<FocusSession>>
    val activeFocusSession: StateFlow<FocusSession?>
    val appLimits: StateFlow<List<AppLimit>>
    val blockSchedules: StateFlow<List<BlockSchedule>>
    val userGoals: StateFlow<List<UserGoal>>
    val todayStats: StateFlow<DeviceStatsSummary?>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = Repository(db)

        todayDateString.value = repository.getTodayDateString()

        // Load Settings
        viewModelScope.launch {
            repository.getSettings().collect { savedSettings ->
                if (savedSettings != null) {
                    _settings.value = savedSettings
                } else {
                    // Create default settings
                    val defaultSettings = SystemSettings()
                    repository.insertSettings(defaultSettings)
                    _settings.value = defaultSettings
                }
            }
        }

        // Connect Room streams
        focusSessions = repository.getAllFocusSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeFocusSession = repository.getActiveFocusSession()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        appLimits = repository.getAllLimits()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        blockSchedules = repository.getAllSchedules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userGoals = repository.getAllGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        todayStats = repository.getStatsForDate(todayDateString.value)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        // Initial system check & data sync
        refreshSystemStatus()
        syncUsageData()
    }

    fun refreshSystemStatus() {
        val context = getApplication<Application>()
        isUsagePermissionGranted.value = UsageTracker.isPermissionGranted(context)
        isAccessibilityPermissionGranted.value = isAccessibilityServiceEnabled()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val context = getApplication<Application>()
        val expectedComponentName = "${context.packageName}/com.example.services.AppBlockerService"
        val enabledServices = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(expectedComponentName, ignoreCase = true) }
    }

    fun syncUsageData() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            // Query stats (real or fallback mock)
            val records = UsageTracker.queryUsageStats(context)
            _todayUsageList.value = records

            // Sync to database
            repository.insertUsage(records)

            // Update Device stats summary for today
            val totalTime = records.sumOf { it.durationMs }
            val existingStats = repository.getStatsForDateSync(todayDateString.value)

            val currentStats = DeviceStatsSummary(
                dateString = todayDateString.value,
                totalScreenTimeMs = totalTime,
                unlockCount = existingStats?.unlockCount ?: 78, // default starting unlocks if none
                notificationCount = existingStats?.notificationCount ?: 132, // default notifications if none
                pickupCount = existingStats?.pickupCount ?: 64 // default pickups if none
            )
            repository.insertStats(currentStats)
        }
    }

    // Settings actions
    fun updateThemeMode(isDark: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(isDarkMode = isDark)
            repository.insertSettings(updated)
        }
    }

    fun updateThemeIndex(index: Int) {
        viewModelScope.launch {
            val updated = _settings.value.copy(selectedThemeIndex = index)
            repository.insertSettings(updated)
        }
    }

    fun setFirstTimeUser(isFirstTime: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(isFirstTimeUser = isFirstTime)
            repository.insertSettings(updated)
        }
    }

    fun updateScreenTimeGoal(goalMs: Long) {
        viewModelScope.launch {
            val updated = _settings.value.copy(dailyScreenTimeGoalMs = goalMs)
            repository.insertSettings(updated)
        }
    }

    // Limits actions
    fun addOrUpdateAppLimit(packageName: String, appName: String, limitMs: Long) {
        viewModelScope.launch {
            val limit = AppLimit(packageName, appName, limitMs, true)
            repository.insertLimit(limit)
            syncUsageData()
        }
    }

    fun toggleAppLimit(limit: AppLimit, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = limit.copy(isEnabled = isEnabled)
            repository.insertLimit(updated)
        }
    }

    fun removeAppLimit(limit: AppLimit) {
        viewModelScope.launch {
            repository.deleteLimit(limit)
        }
    }

    // Focus Session actions
    fun startFocusSession(title: String, durationMins: Int, blockedApps: List<String>) {
        viewModelScope.launch {
            // Stop any active focus session first
            repository.stopActiveFocusSessions()

            val csv = blockedApps.joinToString(",")
            val session = FocusSession(
                title = title,
                startTime = System.currentTimeMillis(),
                durationMin = durationMins,
                isRunning = true,
                blockedAppsCsv = csv
            )
            repository.insertFocusSession(session)
        }
    }

    fun stopActiveFocusSession() {
        viewModelScope.launch {
            repository.stopActiveFocusSessions()
        }
    }

    // Block Schedule actions
    fun addBlockSchedule(
        title: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        days: List<String>,
        category: String
    ) {
        viewModelScope.launch {
            val schedule = BlockSchedule(
                title = title,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                daysOfWeekCsv = days.joinToString(","),
                isEnabled = true,
                blockedCategory = category
            )
            repository.insertSchedule(schedule)
        }
    }

    fun toggleSchedule(schedule: BlockSchedule, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = schedule.copy(isEnabled = isEnabled)
            repository.insertSchedule(updated)
        }
    }

    fun removeSchedule(schedule: BlockSchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }

    // Goals actions
    fun addUserGoal(title: String, type: String, targetMs: Long) {
        viewModelScope.launch {
            val goal = UserGoal(
                title = title,
                goalType = type,
                targetDurationMs = targetMs,
                isCompleted = false,
                dateString = todayDateString.value
            )
            repository.insertGoal(goal)
        }
    }

    fun toggleGoalCompletion(goal: UserGoal) {
        viewModelScope.launch {
            val updated = goal.copy(isCompleted = !goal.isCompleted)
            repository.updateGoal(updated)
        }
    }

    // Backup & Restore
    fun deleteData() {
        viewModelScope.launch {
            repository.clearAllData()
            syncUsageData()
        }
    }

    // Add Simulated Counts for interactive fun
    fun incrementUnlock() {
        viewModelScope.launch {
            val current = repository.getStatsForDateSync(todayDateString.value) ?: return@launch
            val updated = current.copy(unlockCount = current.unlockCount + 1)
            repository.insertStats(updated)
        }
    }

    fun incrementNotification() {
        viewModelScope.launch {
            val current = repository.getStatsForDateSync(todayDateString.value) ?: return@launch
            val updated = current.copy(notificationCount = current.notificationCount + 1)
            repository.insertStats(updated)
        }
    }

    fun incrementPickup() {
        viewModelScope.launch {
            val current = repository.getStatsForDateSync(todayDateString.value) ?: return@launch
            val updated = current.copy(pickupCount = current.pickupCount + 1)
            repository.insertStats(updated)
        }
    }
}
