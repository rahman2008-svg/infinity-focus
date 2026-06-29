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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLimit
import com.example.ui.MainViewModel

@Composable
fun AppLimitsScreen(
    viewModel: MainViewModel
) {
    val limits by viewModel.appLimits.collectAsState()
    val todayUsage by viewModel.todayUsageList.collectAsState()

    val context = LocalContext.current
    var selectedPackage by remember { mutableStateOf("") }
    var selectedAppName by remember { mutableStateOf("") }
    var limitMins by remember { mutableStateOf("30") }

    val appPool = remember(todayUsage) {
        if (todayUsage.isEmpty()) {
            listOf(
                "com.facebook.katana" to "Facebook",
                "com.google.android.youtube" to "YouTube",
                "com.instagram.android" to "Instagram",
                "com.facebook.orca" to "Messenger"
            )
        } else {
            todayUsage.map { it.packageName to it.appName }
        }
    }

    // Set initial app selection
    LaunchedEffect(key1 = appPool) {
        if (selectedPackage.isEmpty() && appPool.isNotEmpty()) {
            selectedPackage = appPool.first().first
            selectedAppName = appPool.first().second
        }
    }

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
            text = "App Time Limits",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // 1. Create App Limit Form
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
                    text = "CONFIGURE NEW LIMIT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Select App Dropdown (represented beautifully with simple choice chips or card selectors for accessibility)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Select Target Application", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState(), enabled = false)
                                .height(52.dp)
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = selectedAppName, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                        }
                    }
                }

                // Preset List for Quick Select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    appPool.take(4).forEach { (packageName, appName) ->
                        val isSelected = selectedPackage == packageName
                        Card(
                            onClick = {
                                selectedPackage = packageName
                                selectedAppName = appName
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = appName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Limit Minutes Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Daily Usage Allowed (Minutes)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = limitMins,
                        onValueChange = { limitMins = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth().testTag("limit_mins_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                        ),
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Button(
                    onClick = {
                        val mins = limitMins.toLongOrNull() ?: 0L
                        if (mins > 0 && selectedPackage.isNotEmpty()) {
                            viewModel.addOrUpdateAppLimit(selectedPackage, selectedAppName, mins * 60 * 1000L)
                            Toast.makeText(context, "Limit set: $selectedAppName to $mins mins/day", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_limit_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enforce Daily Limit", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Active Limits List
        Text(
            text = "ACTIVE ENFORCED LIMITS",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        if (limits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No app limits enforced yet. Put a limit above!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            limits.forEach { limit ->
                val limitMinutes = limit.dailyLimitMs / 1000 / 60
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        limit.appName.contains("Facebook", true) -> Icons.Default.Share
                                        limit.appName.contains("YouTube", true) -> Icons.Default.PlayArrow
                                        limit.appName.contains("Chrome", true) -> Icons.Default.Language
                                        limit.appName.contains("Messenger", true) -> Icons.Default.ChatBubble
                                        limit.appName.contains("Instagram", true) -> Icons.Default.CameraAlt
                                        else -> Icons.Default.Android
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = limit.appName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Limit: $limitMinutes min/day", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = limit.isEnabled,
                                onCheckedChange = { viewModel.toggleAppLimit(limit, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.removeAppLimit(limit) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF43F5E))
                            }
                        }
                    }
                }
            }
        }
    }
}
