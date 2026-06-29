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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun GoalsScreen(
    viewModel: MainViewModel
) {
    val goals by viewModel.userGoals.collectAsState()
    val limits by viewModel.appLimits.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val todayUsage by viewModel.todayUsageList.collectAsState()

    val context = LocalContext.current
    var goalTitle by remember { mutableStateOf("Reduce Social Media") }
    var goalType by remember { mutableStateOf("DAILY") } // DAILY, WEEKLY, MONTHLY
    var targetMins by remember { mutableStateOf("60") }

    val totalScreenTimeMins = todayUsage.sumOf { it.durationMs } / 1000 / 60

    // Achievements calculation
    val achievementRookieUnlocked = totalScreenTimeMins in 1..180 // under 3 hours
    val achievementScholarUnlocked = focusSessions.isNotEmpty()
    val achievementLimitsUnlocked = limits.isNotEmpty()
    val achievementMonkUnlocked = focusSessions.any { it.durationMin >= 60 }

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
            text = "Goals & Achievements",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // 1. Configure Goal Form
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
                    text = "ADD NEW GOAL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Goal Title", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        modifier = Modifier.fillMaxWidth().testTag("goal_title_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("DAILY", "WEEKLY", "MONTHLY")
                    types.forEach { type ->
                        val isSelected = goalType == type
                        Card(
                            onClick = { goalType = type },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Target Screen Time limit (Minutes)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = targetMins,
                        onValueChange = { targetMins = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth().testTag("goal_target_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                        ),
                        leadingIcon = { Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Button(
                    onClick = {
                        val mins = targetMins.toLongOrNull() ?: 0L
                        if (mins > 0 && goalTitle.isNotEmpty()) {
                            viewModel.addUserGoal(goalTitle, goalType, mins * 60 * 1000L)
                            Toast.makeText(context, "Goal added successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_goal_button")
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Commit to Goal", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Goals List
        Text(
            text = "YOUR ACTIVE GOALS",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No active goals tracked. Save a goal above!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            goals.forEach { goal ->
                val targetHours = goal.targetDurationMs / 1000 / 60 / 60
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
                            Checkbox(
                                checked = goal.isCompleted,
                                onCheckedChange = { viewModel.toggleGoalCompletion(goal) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = goal.title,
                                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(text = "Target: Under $targetHours hours (${goal.goalType.lowercase()})", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }

                        if (goal.isCompleted) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color(0xFF10B981))
                        }
                    }
                }
            }
        }

        // 3. Achievements Trophy Panel
        Text(
            text = "UNLOCKED DETOX TROPHIES",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        TrophyCard(
            title = "Detox Rookie",
            description = "Limit daily phone usage under 3 hours.",
            isUnlocked = achievementRookieUnlocked,
            icon = Icons.Default.DirectionsWalk,
            accentColor = MaterialTheme.colorScheme.primary
        )

        TrophyCard(
            title = "Focus Scholar",
            description = "Successfully complete at least one deep concentration blockade.",
            isUnlocked = achievementScholarUnlocked,
            icon = Icons.Default.School,
            accentColor = MaterialTheme.colorScheme.secondary
        )

        TrophyCard(
            title = "Distraction Sentry",
            description = "Enforce at least one daily application quota limit.",
            isUnlocked = achievementLimitsUnlocked,
            icon = Icons.Default.Shield,
            accentColor = Color(0xFF10B981)
        )

        TrophyCard(
            title = "Zen Master",
            description = "Complete a deep focus blockade of 1 hour or more.",
            isUnlocked = achievementMonkUnlocked,
            icon = Icons.Default.SelfImprovement,
            accentColor = Color(0xFFF59E0B)
        )
    }
}

@Composable
fun TrophyCard(
    title: String,
    description: String,
    isUnlocked: Boolean,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) accentColor.copy(alpha = 0.4f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (isUnlocked) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) accentColor else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            if (isUnlocked) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "UNLOCKED",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "LOCKED",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
