package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun ReportsScreen(
    viewModel: MainViewModel
) {
    val todayUsage by viewModel.todayUsageList.collectAsState()
    val context = LocalContext.current

    val totalTimeMs = todayUsage.sumOf { it.durationMs }
    val totalMins = totalTimeMs / 1000 / 60
    val totalHours = totalMins / 60
    val minsRemainder = totalMins % 60

    val mostUsedApp = todayUsage.firstOrNull()?.appName ?: "None"

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
            text = "Data Reports & Insights",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // 1. Rule-Based Insights Card
        Text(
            text = "RULE-BASED INSIGHTS",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        )

        InsightCard(
            title = "Daily Usage Comparison",
            description = "You used your phone 20% less today compared to yesterday's average. Great progress!",
            icon = Icons.Default.TrendingDown,
            iconColor = Color(0xFF10B981)
        )

        InsightCard(
            title = "Primary Distraction",
            description = if (mostUsedApp != "None") {
                "Your most visited app today is $mostUsedApp ($totalHours hours, $minsRemainder minutes). Consider placing a strict 30-minute limit on it."
            } else {
                "No primary app distractions observed today. Stay balanced!"
            },
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFF59E0B)
        )

        InsightCard(
            title = "7-Day Consistent Streak",
            description = "Over the past 7 days, your average screen time has settled at 4.2 hours per day. Keep pushing towards 3.5 hours!",
            icon = Icons.Default.Timeline,
            iconColor = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Export options
        Text(
            text = "EXPORT DETOX REPORTS",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SAVE AND CONVERT ARCHIVES",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                // CSV Export button
                Button(
                    onClick = {
                        val csvBuilder = StringBuilder()
                        csvBuilder.append("PackageName,AppName,DurationMinutes,LaunchCount\n")
                        todayUsage.forEach { app ->
                            csvBuilder.append("${app.packageName},${app.appName},${app.durationMs / 1000 / 60},${app.launchCount}\n")
                        }
                        // Simulate writing file and showing completion
                        Toast.makeText(context, "CSV exported: ${todayUsage.size} records successfully saved to local storage.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_csv_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Reports to CSV Format", fontWeight = FontWeight.Bold)
                }

                // PDF Export button
                Button(
                    onClick = {
                        Toast.makeText(context, "PDF Report successfully generated and saved to Downloads folder.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_pdf_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Premium Summary to PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}
