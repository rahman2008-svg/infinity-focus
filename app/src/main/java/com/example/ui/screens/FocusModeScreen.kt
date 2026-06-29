package com.example.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun FocusModeScreen(
    viewModel: MainViewModel
) {
    val activeSession by viewModel.activeFocusSession.collectAsState()
    val todayUsage by viewModel.todayUsageList.collectAsState()

    if (activeSession != null && activeSession?.isRunning == true) {
        ActiveFocusCountdown(
            sessionTitle = activeSession!!.title,
            startTimeMs = activeSession!!.startTime,
            durationMin = activeSession!!.durationMin,
            onStopSession = { viewModel.stopActiveFocusSession() }
        )
    } else {
        ConfigureFocusSession(
            viewModel = viewModel,
            appSelectionPool = todayUsage.map { it.packageName to it.appName }
        )
    }
}

@Composable
fun ActiveFocusCountdown(
    sessionTitle: String,
    startTimeMs: Long,
    durationMin: Int,
    onStopSession: () -> Unit
) {
    val totalDurationSecs = durationMin * 60
    var secondsRemaining by remember { mutableStateOf(totalDurationSecs) }

    LaunchedEffect(key1 = startTimeMs, key2 = durationMin) {
        while (true) {
            val elapsedSecs = (System.currentTimeMillis() - startTimeMs) / 1000
            val remaining = totalDurationSecs - elapsedSecs
            secondsRemaining = maxOf(0L, remaining).toInt()
            if (secondsRemaining <= 0) {
                onStopSession()
                break
            }
            delay(1000)
        }
    }

    val displayHours = secondsRemaining / 3600
    val displayMins = (secondsRemaining % 3600) / 60
    val displaySecs = secondsRemaining % 60

    val progress = secondsRemaining.toFloat() / totalDurationSecs.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "INFINITY DEEP FOCUS",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )

            Text(
                text = "Session: $sessionTitle",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground)
            )

            // Huge circular visual timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 10.dp,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d:%02d", displayHours, displayMins, displaySecs),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "REMAINING TIME",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            Text(
                text = "Your blocked apps are actively quarantined.\nOpening them will trigger automatic returns to this safe space.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStopSession,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF43F5E),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("stop_focus_button")
            ) {
                Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                Spacer(modifier = Modifier.width(8.dp))
                Text("End Deep Focus Session", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ConfigureFocusSession(
    viewModel: MainViewModel,
    appSelectionPool: List<Pair<String, String>>
) {
    var sessionName by remember { mutableStateOf("Deep Concentration") }
    var durationSelection by remember { mutableStateOf(30) } // mins
    val selectedApps = remember { mutableStateListOf<String>() }

    // Standard preset apps if selection pool is empty
    val displayedAppPool = remember(appSelectionPool) {
        if (appSelectionPool.isEmpty()) {
            listOf(
                "com.facebook.katana" to "Facebook",
                "com.google.android.youtube" to "YouTube",
                "com.instagram.android" to "Instagram",
                "com.facebook.orca" to "Messenger"
            )
        } else {
            appSelectionPool
        }
    }

    // Auto-select standard social networks initially to save user time!
    LaunchedEffect(key1 = displayedAppPool) {
        if (selectedApps.isEmpty()) {
            displayedAppPool.forEach { (packageName, _) ->
                if (packageName.contains("facebook") || packageName.contains("instagram") || packageName.contains("youtube")) {
                    selectedApps.add(packageName)
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Initiate Deep Focus",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // 1. Session Name Customizer
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SESSION NAME", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            OutlinedTextField(
                value = sessionName,
                onValueChange = { sessionName = it },
                modifier = Modifier.fillMaxWidth().testTag("focus_session_name_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 2. Timer Presets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SESSION DURATION", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val durations = listOf(15, 30, 60, 120)
                durations.forEach { duration ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { durationSelection = duration },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (durationSelection == duration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (duration >= 60) "${duration / 60}h" else "${duration}m",
                                fontWeight = FontWeight.Bold,
                                color = if (durationSelection == duration) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Selective Checklist of Apps to Quarantine
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("QUARANTINE APPLICATIONS", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)

            displayedAppPool.forEach { (packageName, appName) ->
                val isSelected = selectedApps.contains(packageName)
                Card(
                    onClick = {
                        if (isSelected) {
                            selectedApps.remove(packageName)
                        } else {
                            selectedApps.add(packageName)
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                                        appName.contains("Facebook", true) -> Icons.Default.Share
                                        appName.contains("YouTube", true) -> Icons.Default.PlayArrow
                                        appName.contains("Chrome", true) -> Icons.Default.Language
                                        appName.contains("Messenger", true) -> Icons.Default.ChatBubble
                                        appName.contains("Instagram", true) -> Icons.Default.CameraAlt
                                        else -> Icons.Default.Android
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = appName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) {
                                    selectedApps.remove(packageName)
                                } else {
                                    selectedApps.add(packageName)
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Start Focus Button
        Button(
            onClick = {
                if (selectedApps.isNotEmpty()) {
                    viewModel.startFocusSession(sessionName, durationSelection, selectedApps.toList())
                }
            },
            enabled = selectedApps.isNotEmpty(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_focus_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Start")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Begin Quarantine Blockade", fontWeight = FontWeight.Bold)
        }
    }
}
