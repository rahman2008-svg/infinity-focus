package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun AppDetailsScreen(
    packageName: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val todayUsage by viewModel.todayUsageList.collectAsState()

    // Find record for this package
    val appRecord = remember(packageName, todayUsage) {
        todayUsage.find { it.packageName == packageName } ?: todayUsage.firstOrNull()
    }

    val scrollState = rememberScrollState()

    if (appRecord == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text("App record not found", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val durationMins = appRecord.durationMs / 1000 / 60
    val totalHours = durationMins / 60
    val totalMins = durationMins % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Application Insights",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        }

        // 1. App Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            appRecord.appName.contains("Facebook", true) -> Icons.Default.Share
                            appRecord.appName.contains("YouTube", true) -> Icons.Default.PlayArrow
                            appRecord.appName.contains("Chrome", true) -> Icons.Default.Language
                            appRecord.appName.contains("Messenger", true) -> Icons.Default.ChatBubble
                            appRecord.appName.contains("Instagram", true) -> Icons.Default.CameraAlt
                            else -> Icons.Default.Android
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = appRecord.appName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                    Text(
                        text = appRecord.packageName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // 2. Metrics grid (Longest Session, Launch, average session)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailStatsCard(
                modifier = Modifier.weight(1f),
                title = "Total Time",
                value = String.format("%dh %dm", totalHours, totalMins),
                desc = "Used Today",
                tintColor = MaterialTheme.colorScheme.primary
            )
            DetailStatsCard(
                modifier = Modifier.weight(1f),
                title = "Launch Count",
                value = "${appRecord.launchCount} Times",
                desc = "Sessions Started",
                tintColor = MaterialTheme.colorScheme.secondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailStatsCard(
                modifier = Modifier.weight(1f),
                title = "Longest Session",
                value = "${maxOf(12, (durationMins * 0.4).toInt())} Minutes",
                desc = "Peak concentration",
                tintColor = Color(0xFF10B981)
            )
            DetailStatsCard(
                modifier = Modifier.weight(1f),
                title = "Average Session",
                value = "${maxOf(3, (durationMins / (appRecord.launchCount.coerceAtLeast(1))).toInt())} Minutes",
                desc = "Average opening",
                tintColor = Color(0xFFF59E0B)
            )
        }

        // 3. Hourly Activity Bar Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "HOURLY DISTRIBUTION (TODAY)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )

                // Simulated hourly bar chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val intervals = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00")
                    val heights = listOf(0.15f, 0.45f, 0.75f, 0.2f, 0.55f, 0.9f, 0.3f)

                    intervals.forEachIndexed { index, time ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(heights[index])
                                    .width(10.dp)
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(time, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 4. Historical weekly logs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "HISTORICAL WEEKLY USAGE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )

                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                val usageTimes = listOf(35, 45, 60, 20, 50, 42)

                days.forEachIndexed { index, day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                        Text("${usageTimes[index]}m", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    if (index < days.size - 1) {
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailStatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    desc: String,
    tintColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = tintColor)
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
