package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_records")
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val dateString: String, // "YYYY-MM-DD"
    val durationMs: Long,
    val launchCount: Int,
    val lastUsedTimestamp: Long
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startTime: Long,
    val durationMin: Int,
    val isRunning: Boolean,
    val blockedAppsCsv: String // Comma-separated package names
)

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMs: Long,
    val isEnabled: Boolean
)

@Entity(tableName = "block_schedules")
data class BlockSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeekCsv: String, // e.g. "MON,TUE,WED,THU,FRI,SAT,SUN"
    val isEnabled: Boolean,
    val blockedCategory: String // "SOCIAL", "GAMES", "ALL", etc.
)

@Entity(tableName = "user_goals")
data class UserGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val goalType: String, // "DAILY", "WEEKLY", "MONTHLY"
    val targetDurationMs: Long,
    val isCompleted: Boolean,
    val dateString: String // The date or week identifier
)

@Entity(tableName = "device_stats_summary")
data class DeviceStatsSummary(
    @PrimaryKey val dateString: String, // "YYYY-MM-DD"
    val totalScreenTimeMs: Long,
    val unlockCount: Int,
    val notificationCount: Int,
    val pickupCount: Int
)

@Entity(tableName = "system_settings")
data class SystemSettings(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = true,
    val languageCode: String = "en",
    val isFirstTimeUser: Boolean = true,
    val selectedThemeIndex: Int = 0, // 0: Dark Premium, 1: Amoled Black, 2: Cyber Glow
    val dailyScreenTimeGoalMs: Long = 4 * 60 * 60 * 1000L // 4 hours
)
