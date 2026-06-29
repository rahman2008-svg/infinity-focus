package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onFinish: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(0) } // 0: Splash, 1: Onboarding Page 1, 2: Page 2, 3: Page 3, 4: Page 4, 5: Permissions Setup

    val isUsageGranted by viewModel.isUsagePermissionGranted.collectAsState()
    val isAccessibilityGranted by viewModel.isAccessibilityPermissionGranted.collectAsState()

    // Splash Timer
    if (currentStep == 0) {
        SplashScreenView(onSplashFinished = {
            viewModel.refreshSystemStatus()
            // If already set up, we go straight to home, otherwise slide 1
            scope.launch {
                val settings = viewModel.settings.value
                if (settings.isFirstTimeUser) {
                    currentStep = 1
                } else {
                    onFinish()
                }
            }
        })
    } else if (currentStep in 1..4) {
        OnboardingSlides(
            pageIndex = currentStep - 1,
            onNext = {
                if (currentStep == 4) {
                    currentStep = 5 // Go to Permissions Setup
                } else {
                    currentStep++
                }
            },
            onSkip = { currentStep = 5 }
        )
    } else {
        PermissionsSetupView(
            viewModel = viewModel,
            isUsageGranted = isUsageGranted,
            isAccessibilityGranted = isAccessibilityGranted,
            onComplete = {
                viewModel.setFirstTimeUser(false)
                onFinish()
            }
        )
    }
}

@Composable
fun SplashScreenView(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500) // 2.5 seconds
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant Splash App Icon Card
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "Infinity Focus Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "INFINITY FOCUS",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Master your time. Reclaim your life.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun OnboardingSlides(
    pageIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val titles = listOf(
        "Welcome to Infinity Focus",
        "Empower Your Daily Habits",
        "Take Charge of Screen Time",
        "Privacy-First Integrity"
    )

    val descriptions = listOf(
        "Monitor phone usage and screen time automatically to restore real-life human interactions.",
        "Gain daily reports, configure smart app limits, and minimize compulsive social distractions.",
        "Use deep focus session blockades to completely silence attention-seeking notification loops.",
        "Your statistics are processed offline on-device. We never store or upload your application history."
    )

    val icons = listOf(
        Icons.Default.HourglassEmpty,
        Icons.Default.TrendingUp,
        Icons.Default.Timer,
        Icons.Default.Security
    )

    // Map to beautiful semantic colors matching theme accents
    val iconColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        Color(0xFF10B981),
        Color(0xFFF59E0B)
    )

    var agreedToPrivacy by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header skips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (pageIndex < 3) {
                    TextButton(onClick = onSkip) {
                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Animated slide content
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(iconColors[pageIndex].copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icons[pageIndex],
                    contentDescription = null,
                    tint = iconColors[pageIndex],
                    modifier = Modifier.size(72.dp)
                )
            }

            Text(
                text = titles[pageIndex],
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = descriptions[pageIndex],
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Progress Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                for (i in 0..3) {
                    Box(
                        modifier = Modifier
                            .size(width = if (i == pageIndex) 24.dp else 8.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(if (i == pageIndex) iconColors[pageIndex] else MaterialTheme.colorScheme.tertiary)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            if (pageIndex == 3) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Checkbox(
                        checked = agreedToPrivacy,
                        onCheckedChange = { agreedToPrivacy = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I agree to the offline Privacy Policy & Terms.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Button(
                onClick = onNext,
                enabled = pageIndex < 3 || agreedToPrivacy,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconColors[pageIndex],
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_continue_button")
            ) {
                Text(
                    text = if (pageIndex == 3) "Get Started" else "Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionsSetupView(
    viewModel: MainViewModel,
    isUsageGranted: Boolean,
    isAccessibilityGranted: Boolean,
    onComplete: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "System Setup",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Infinity Focus requires system authorization to track application screen time and block distractions.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Permission Cards
            PermissionCard(
                title = "1. Usage Stats Access",
                description = "Required to measure and aggregate screen times of all foreground applications.",
                isGranted = isUsageGranted,
                actionLabel = "Enable Access",
                onAction = {
                    try {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Safe fallback
                        val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(fallbackIntent)
                    }
                }
            )

            PermissionCard(
                title = "2. Accessibility Service",
                description = "Required to monitor app launches and automatically return home when blocked.",
                isGranted = isAccessibilityGranted,
                actionLabel = "Enable Service",
                onAction = {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(fallbackIntent)
                    }
                }
            )

            PermissionCard(
                title = "3. Battery Optimization",
                description = "Ignore optimization to keep our focus rules working reliably in the background.",
                isGranted = false, // Always let them prompt or skip
                actionLabel = "Configure",
                onAction = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(fallbackIntent)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Refresh Status Button
            OutlinedButton(
                onClick = { viewModel.refreshSystemStatus() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh Permission Status")
            }

            Button(
                onClick = onComplete,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("permissions_done_button")
            ) {
                Text(
                    text = "Continue to Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null)
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFF065F46).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isGranted) Color(0xFF10B981).copy(alpha = 0.4f) else MaterialTheme.colorScheme.tertiary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
