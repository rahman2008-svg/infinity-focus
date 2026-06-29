package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Repository(private val db: AppDatabase) {

    private val appUsageDao = db.appUsageDao()
    private val focusSessionDao = db.focusSessionDao()
    private val appLimitDao = db.appLimitDao()
    private val blockScheduleDao = db.blockScheduleDao()
    private val userGoalDao = db.userGoalDao()
    private val deviceStatsDao = db.deviceStatsDao()
    private val systemSettingsDao = db.systemSettingsDao()

    // Date helper
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // App Usage
    fun getUsageForDate(date: String): Flow<List<AppUsageRecord>> = appUsageDao.getUsageForDate(date)
    suspend fun getUsageForDateSync(date: String): List<AppUsageRecord> = appUsageDao.getUsageForDateSync(date)
    fun getAllUsage(): Flow<List<AppUsageRecord>> = appUsageDao.getAllUsage()
    suspend fun insertUsage(records: List<AppUsageRecord>) = appUsageDao.insertUsage(records)
    suspend fun insertSingleUsage(record: AppUsageRecord) = appUsageDao.insertSingleUsage(record)
    suspend fun clearAllUsage() = appUsageDao.clearAll()

    // Focus Sessions
    fun getAllFocusSessions(): Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    fun getActiveFocusSession(): Flow<FocusSession?> = focusSessionDao.getActiveSession()
    suspend fun getActiveFocusSessionSync(): FocusSession? = focusSessionDao.getActiveSessionSync()
    suspend fun insertFocusSession(session: FocusSession): Long = focusSessionDao.insertSession(session)
    suspend fun updateFocusSession(session: FocusSession) = focusSessionDao.updateSession(session)
    suspend fun stopActiveFocusSessions() = focusSessionDao.stopActiveSessions()

    // App Limits
    fun getAllLimits(): Flow<List<AppLimit>> = appLimitDao.getAllLimits()
    suspend fun getAllLimitsSync(): List<AppLimit> = appLimitDao.getAllLimitsSync()
    suspend fun getLimitForApp(packageName: String): AppLimit? = appLimitDao.getLimitForApp(packageName)
    suspend fun insertLimit(limit: AppLimit) = appLimitDao.insertLimit(limit)
    suspend fun deleteLimit(limit: AppLimit) = appLimitDao.deleteLimit(limit)
    suspend fun deleteLimitForApp(packageName: String) = appLimitDao.deleteLimitForApp(packageName)

    // Block Schedules
    fun getAllSchedules(): Flow<List<BlockSchedule>> = blockScheduleDao.getAllSchedules()
    suspend fun getAllSchedulesSync(): List<BlockSchedule> = blockScheduleDao.getAllSchedulesSync()
    suspend fun insertSchedule(schedule: BlockSchedule) = blockScheduleDao.insertSchedule(schedule)
    suspend fun deleteSchedule(schedule: BlockSchedule) = blockScheduleDao.deleteSchedule(schedule)

    // User Goals
    fun getAllGoals(): Flow<List<UserGoal>> = userGoalDao.getAllGoals()
    suspend fun insertGoal(goal: UserGoal) = userGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: UserGoal) = userGoalDao.updateGoal(goal)

    // Device Stats
    fun getStatsForDate(date: String): Flow<DeviceStatsSummary?> = deviceStatsDao.getStatsForDate(date)
    suspend fun getStatsForDateSync(date: String): DeviceStatsSummary? = deviceStatsDao.getStatsForDateSync(date)
    suspend fun insertStats(stats: DeviceStatsSummary) = deviceStatsDao.insertStats(stats)

    // System Settings
    fun getSettings(): Flow<SystemSettings?> = systemSettingsDao.getSettings()
    suspend fun getSettingsSync(): SystemSettings? = systemSettingsDao.getSettingsSync()
    suspend fun insertSettings(settings: SystemSettings) = systemSettingsDao.insertSettings(settings)

    // Clear all tables for developer/testing
    suspend fun clearAllData() {
        appUsageDao.clearAll()
        focusSessionDao.clearAll()
        appLimitDao.clearAll()
        blockScheduleDao.clearAll()
        userGoalDao.clearAll()
        deviceStatsDao.clearAll()
    }
}
