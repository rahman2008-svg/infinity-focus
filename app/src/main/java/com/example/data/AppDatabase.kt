package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppUsageRecord::class,
        FocusSession::class,
        AppLimit::class,
        BlockSchedule::class,
        UserGoal::class,
        DeviceStatsSummary::class,
        SystemSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appUsageDao(): AppUsageDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun appLimitDao(): AppLimitDao
    abstract fun blockScheduleDao(): BlockScheduleDao
    abstract fun userGoalDao(): UserGoalDao
    abstract fun deviceStatsDao(): DeviceStatsDao
    abstract fun systemSettingsDao(): SystemSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "infinity_focus_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
