package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppUsageRecord
import com.example.ui.MainViewModel

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Today, 1: Yesterday, 2: 7 Days, 3: 30 Days
    val todayUsage by viewModel.todayUsageList.collectAsState()

    // Generate static mock variations for previous days to look realistic
    val displayedUsage = remember(selectedTab, todayUsage) {
        when (selectedTab) {
            0 -> todayUsage
            1 -> todayUsage.map {
                it.copy(
                    durationMs = (it.durationMs * 0.85).toLong() + 5 * 60 * 1000L,
                    launchCount = (it.launchCount * 0.9).toInt() + 2
                )
            }.sortedByDescending { it.durationMs }
            2 -> todayUsage.map {
                it.copy(
                    durationMs = (it.durationMs * 6.2).toLong(),
                    launchCount = (it.launchCount * 6.5).toInt()
                )
            }.sortedByDescending { it.durationMs }
            else -> todayUsage.map {
                it.copy(
                    durationMs = (it.durationMs * 25.4).toLong(),
                    launchCount = (it.launchCount * 24).toInt()
                )
            }.sortedByDescending { it.durationMs }
        }
    }

    val totalTimeMs = displayedUsage.sumOf { it.durationMs }
    val totalMins = totalTimeMs / 1000 / 60
    val totalHours = totalMins / 60
    val minsRemainder = totalMins % 60

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Analytics & Reports",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // 1. Time Filters
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            val tabs = listOf("Today", "Yesterday", "7 Days", "30 Days")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // 2. Interactive Donut / Pie Chart representation
        if (displayedUsage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Donut Chart Vector
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        Canvas(modifier = Modifier.size(110.dp)) {
                            var startAngle = -90f
                            val colors = listOf(
                                primaryColor, // Periwinkle
                                secondaryColor, // Purple
                                Color(0xFF10B981), // Green
                                Color(0xFFF59E0B), // Yellow
                                onSurfaceVariantColor  // Gray (others)
                            )

                            displayedUsage.take(5).forEachIndexed { index, app ->
                                val sweepAngle = (app.durationMs.toFloat() / totalTimeMs.toFloat()) * 360f
                                if (sweepAngle > 0.5f) {
                                    drawArc(
                                        color = colors.getOrElse(index) { onSurfaceVariantColor },
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalHours}h ${minsRemainder}m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Text(text = "Total Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Chart Legend
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val legendColors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            Color(0xFF10B981),
                            Color(0xFFF59E0B),
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        displayedUsage.take(4).forEachIndexed { index, app ->
                            val pct = (app.durationMs.toFloat() / totalTimeMs.toFloat() * 100).toInt()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(legendColors[index], CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${app.appName} ($pct%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Application List with Launch counts & Percentage
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "APP USAGE DETAILS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
            )

            displayedUsage.forEach { app ->
                val pct = if (totalTimeMs > 0) (app.durationMs.toFloat() / totalTimeMs.toFloat() * 100).toInt() else 0
                StatsAppRow(
                    record = app,
                    percentage = pct,
                    onClick = { onNavigateToDetails(app.packageName) }
                )
            }
        }
    }
}

@Composable
fun StatsAppRow(
    record: AppUsageRecord,
    percentage: Int,
    onClick: () -> Unit
) {
    val durationMins = record.durationMs / 1000 / 60

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stats_row_${record.packageName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
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
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.appName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Launches: ${record.launchCount}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$percentage% of phone",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (durationMins >= 60) {
                        String.format("%dh %dm", durationMins / 60, durationMins % 60)
                    } else {
                        "${durationMins}m"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
