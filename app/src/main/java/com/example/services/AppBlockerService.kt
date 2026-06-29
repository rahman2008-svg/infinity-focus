package com.example.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.BlockActivity
import com.example.utils.UsageTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class AppBlockerService : AccessibilityService() {

    private lateinit var repository: Repository
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        repository = Repository(db)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Never block our own app or launcher/system installer
        if (packageName == this.packageName || packageName == "com.android.settings" || packageName == "android") {
            return
        }

        serviceScope.launch {
            var blockReason: String? = null

            // 1. Check Active Focus Session
            val activeFocus = repository.getActiveFocusSession().firstOrNull()
            if (activeFocus != null && activeFocus.isRunning) {
                val blockedApps = activeFocus.blockedAppsCsv.split(",").map { it.trim() }
                if (blockedApps.contains(packageName)) {
                    blockReason = "Focus Mode is Active!\nTitle: ${activeFocus.title}"
                }
            }

            // 2. Check App Limits
            if (blockReason == null) {
                val appLimit = repository.getLimitForApp(packageName)
                if (appLimit != null && appLimit.isEnabled) {
                    // Query real-time screen time for today
                    val todayUsage = UsageTracker.queryUsageStats(applicationContext)
                    val currentAppUsage = todayUsage.find { it.packageName == packageName }
                    val currentDurationMs = currentAppUsage?.durationMs ?: 0L

                    if (currentDurationMs >= appLimit.dailyLimitMs) {
                        val limitMins = appLimit.dailyLimitMs / 1000 / 60
                        blockReason = "Daily Limit Reached!\nLimit: $limitMins minutes"
                    }
                }
            }

            // 3. Check Active Block Schedules
            if (blockReason == null) {
                val schedules = repository.getAllSchedulesSync()
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                val dayString = when (currentDayOfWeek) {
                    Calendar.SUNDAY -> "SUN"
                    Calendar.MONDAY -> "MON"
                    Calendar.TUESDAY -> "TUE"
                    Calendar.WEDNESDAY -> "WED"
                    Calendar.THURSDAY -> "THU"
                    Calendar.FRIDAY -> "FRI"
                    Calendar.SATURDAY -> "SAT"
                    else -> ""
                }

                for (schedule in schedules) {
                    if (schedule.isEnabled) {
                        val days = schedule.daysOfWeekCsv.split(",").map { it.trim() }
                        if (days.contains(dayString)) {
                            // Check time window
                            val isWithinTime = isTimeWithinWindow(
                                currentHour, currentMinute,
                                schedule.startHour, schedule.startMinute,
                                schedule.endHour, schedule.endMinute
                            )

                            if (isWithinTime) {
                                // Simple categorization matching (e.g., if social category is blocked)
                                val isSocialApp = isSocialAppPackage(packageName)
                                val isGamingApp = isGamingAppPackage(packageName)

                                if (schedule.blockedCategory == "ALL" ||
                                    (schedule.blockedCategory == "SOCIAL" && isSocialApp) ||
                                    (schedule.blockedCategory == "GAMES" && isGamingApp)) {
                                    blockReason = "Scheduled Blocker Active!\nSchedule: ${schedule.title}"
                                    break
                                }
                            }
                        }
                    }
                }
            }

            if (blockReason != null) {
                // Minimize the blocked app (Go Home)
                performGlobalAction(GLOBAL_ACTION_HOME)

                // Launch Block Screen Activity
                val blockIntent = Intent(applicationContext, BlockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("BLOCKED_PACKAGE", packageName)
                    putExtra("BLOCK_REASON", blockReason)
                }
                startActivity(blockIntent)
            }
        }
    }

    private fun isTimeWithinWindow(
        curHour: Int, curMin: Int,
        startHour: Int, startMin: Int,
        endHour: Int, endMin: Int
    ): Boolean {
        val curVal = curHour * 60 + curMin
        val startVal = startHour * 60 + startMin
        val endVal = endHour * 60 + endMin

        return if (startVal <= endVal) {
            curVal in startVal..endVal
        } else {
            // Over midnight (e.g., 22:00 to 06:00)
            curVal >= startVal || curVal <= endVal
        }
    }

    private fun isSocialAppPackage(packageName: String): Boolean {
        val socialKeywords = listOf("facebook", "instagram", "twitter", "tiktok", "messenger", "whatsapp", "linkedin", "snapchat")
        return socialKeywords.any { packageName.lowercase().contains(it) }
    }

    private fun isGamingAppPackage(packageName: String): Boolean {
        val gamingKeywords = listOf("game", "pubg", "clash", "candy", "subway", "roblox", "minecraft")
        return gamingKeywords.any { packageName.lowercase().contains(it) }
    }

    override fun onInterrupt() {
        // Required method
    }
}
