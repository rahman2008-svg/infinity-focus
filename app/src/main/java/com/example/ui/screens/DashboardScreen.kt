package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppUsageRecord
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val todayUsage by viewModel.todayUsageList.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()

    // Aggregate screen time
    val totalScreenTimeMs = todayUsage.sumOf { it.durationMs }
    val totalScreenTimeMins = totalScreenTimeMs / 1000 / 60
    val totalHours = totalScreenTimeMins / 60
    val totalMins = totalScreenTimeMins % 60

    // Goal calculation
    val goalMs = settings.dailyScreenTimeGoalMs
    val goalMins = goalMs / 1000 / 60
    val progress = if (goalMs > 0) totalScreenTimeMs.toFloat() / goalMs.toFloat() else 0f
    val remainingMs = maxOf(0L, goalMs - totalScreenTimeMs)
    val remainingMins = remainingMs / 1000 / 60

    val scrollState = rememberScrollState()

    // Determine Greeting based on system time
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "👋 Good Morning"
        currentHour < 17 -> "👋 Good Afternoon"
        else -> "👋 Good Evening"
    }

    val formattedDate = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header Greeting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            IconButton(
                onClick = { viewModel.syncUsageData() },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // 2. Today Screen Time Circular Progress Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TODAY'S SCREEN TIME",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )

                // Circular Progress Drawing
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { minOf(1.0f, progress) },
                        modifier = Modifier.fillMaxSize(),
                        color = if (progress >= 1.0f) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary,
                        strokeWidth = 12.dp,
                        trackColor = MaterialTheme.colorScheme.tertiary
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%dh %dm", totalHours, totalMins),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (progress >= 1.0f) "Goal Exceeded!" else "of ${goalMins / 60}h goal",
                            color = if (progress >= 1.0f) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Interaction counters: Unlocks, Notifications, Pickups
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetricCounter(
                        icon = Icons.Default.LockOpen,
                        count = todayStats?.unlockCount ?: 78,
                        label = "Unlocks",
                        onClick = { viewModel.incrementUnlock() }
                    )
                    StatMetricCounter(
                        icon = Icons.Default.Notifications,
                        count = todayStats?.notificationCount ?: 132,
                        label = "Notifications",
                        onClick = { viewModel.incrementNotification() }
                    )
                    StatMetricCounter(
                        icon = Icons.Default.PhonelinkRing,
                        count = todayStats?.pickupCount ?: 64,
                        label = "Pickups",
                        onClick = { viewModel.incrementPickup() }
                    )
                }
            }
        }

        // 3. Goal Remaining Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Daily Goal Status", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        if (remainingMins > 0) "${remainingMins}m remaining" else "0m remaining",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { minOf(1.0f, progress) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (progress >= 1.0f) Color(0xFFF43F5E) else Color(0xFF10B981),
                    trackColor = MaterialTheme.colorScheme.background
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Used: ${totalHours}h ${totalMins}m", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Limit: ${goalMins / 60}h", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 4. Most Used Apps
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "MOST USED APPLICATIONS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
            )

            if (todayUsage.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No usage recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                todayUsage.take(4).forEach { record ->
                    AppUsageRow(
                        record = record,
                        maxDurationMs = todayUsage.firstOrNull()?.durationMs ?: 1L,
                        onClick = { onNavigateToDetails(record.packageName) }
                    )
                }
            }
        }

        // 5. Weekly Chart Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "WEEKLY ACTIVITY SUMMARY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )
                )

                // Simple layout for weekly chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    // Simulated previous screen times for standard week
                    val mockTimes = listOf(3.2f, 4.5f, 5.1f, 2.8f, 3.9f, 4.2f, (totalScreenTimeMins / 60f))

                    weekdays.forEachIndexed { index, day ->
                        val duration = mockTimes[index]
                        val heightPercentage = duration / 6f // Max scale 6 hours

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = String.format("%.1fh", duration),
                                fontSize = 10.sp,
                                color = if (index == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(minOf(1f, heightPercentage))
                                    .width(16.dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (index == 6) {
                                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                        } else {
                                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.surfaceVariant))
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                color = if (index == 6) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (index == 6) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCounter(
    icon: ImageVector,
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(96.dp)
            .testTag("stat_${label.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = count.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AppUsageRow(
    record: AppUsageRecord,
    maxDurationMs: Long,
    onClick: () -> Unit
) {
    val durationMins = record.durationMs / 1000 / 60
    val progress = record.durationMs.toFloat() / maxDurationMs.toFloat()

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_usage_row_${record.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle placeholder for Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        record.appName.contains("Facebook", true) -> Icons.Default.Share
                        record.appName.contains("YouTube", true) -> Icons.Default.PlayArrow
                        record.appName.contains("Chrome", true) -> Icons.Default.Language
                        record.appName.contains("Messenger", true) -> Icons.Default.ChatBubble
                        record.appName.contains("Instagram", true) -> Icons.Default.CameraAlt
                        else -> Icons.Default.Android
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = record.appName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (durationMins >= 60) {
                            String.format("%dh %dm", durationMins / 60, durationMins % 60)
                        } else {
                            "${durationMins}m"
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}
