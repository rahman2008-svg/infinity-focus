package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage_records WHERE dateString = :date ORDER BY durationMs DESC")
    fun getUsageForDate(date: String): Flow<List<AppUsageRecord>>

    @Query("SELECT * FROM app_usage_records WHERE dateString = :date ORDER BY durationMs DESC")
    suspend fun getUsageForDateSync(date: String): List<AppUsageRecord>

    @Query("SELECT * FROM app_usage_records ORDER BY dateString DESC, durationMs DESC")
    fun getAllUsage(): Flow<List<AppUsageRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(records: List<AppUsageRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleUsage(record: AppUsageRecord)

    @Query("DELETE FROM app_usage_records")
    suspend fun clearAll()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE isRunning = 1 LIMIT 1")
    fun getActiveSession(): Flow<FocusSession?>

    @Query("SELECT * FROM focus_sessions WHERE isRunning = 1 LIMIT 1")
    suspend fun getActiveSessionSync(): FocusSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    @Query("UPDATE focus_sessions SET isRunning = 0 WHERE isRunning = 1")
    suspend fun stopActiveSessions()

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limits")
    fun getAllLimits(): Flow<List<AppLimit>>

    @Query("SELECT * FROM app_limits")
    suspend fun getAllLimitsSync(): List<AppLimit>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName LIMIT 1")
    suspend fun getLimitForApp(packageName: String): AppLimit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: AppLimit)

    @Delete
    suspend fun deleteLimit(limit: AppLimit)

    @Query("DELETE FROM app_limits WHERE packageName = :packageName")
    suspend fun deleteLimitForApp(packageName: String)

    @Query("DELETE FROM app_limits")
    suspend fun clearAll()
}

@Dao
interface BlockScheduleDao {
    @Query("SELECT * FROM block_schedules")
    fun getAllSchedules(): Flow<List<BlockSchedule>>

    @Query("SELECT * FROM block_schedules")
    suspend fun getAllSchedulesSync(): List<BlockSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BlockSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: BlockSchedule)

    @Query("DELETE FROM block_schedules")
    suspend fun clearAll()
}

@Dao
interface UserGoalDao {
    @Query("SELECT * FROM user_goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<UserGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: UserGoal)

    @Update
    suspend fun updateGoal(goal: UserGoal)

    @Query("DELETE FROM user_goals")
    suspend fun clearAll()
}

@Dao
interface DeviceStatsDao {
    @Query("SELECT * FROM device_stats_summary WHERE dateString = :date LIMIT 1")
    fun getStatsForDate(date: String): Flow<DeviceStatsSummary?>

    @Query("SELECT * FROM device_stats_summary WHERE dateString = :date LIMIT 1")
    suspend fun getStatsForDateSync(date: String): DeviceStatsSummary?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: DeviceStatsSummary)

    @Query("DELETE FROM device_stats_summary")
    suspend fun clearAll()
}

@Dao
interface SystemSettingsDao {
    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SystemSettings?>

    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): SystemSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SystemSettings)
}
