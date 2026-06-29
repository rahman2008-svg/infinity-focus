package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToAbout: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val isAccessibilityGranted by viewModel.isAccessibilityPermissionGranted.collectAsState()
    val isUsageGranted by viewModel.isUsagePermissionGranted.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showWipeConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "System Settings",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // 1. Theme Configuration Panel
        SettingsSectionHeader(title = "VISUAL INTERFACE")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Dark Theme", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Enables dark premium color spaces", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = settings.isDarkMode,
                        onCheckedChange = { viewModel.updateThemeMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                // Custom Themes choices
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Selected Theme Accent", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val accentThemes = listOf("Space Cyan", "Royal Blue", "Amethyst")
                        val themeIndices = listOf(0, 1, 2)
                        val themeColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)

                        themeIndices.forEach { index ->
                            val isSelected = settings.selectedThemeIndex == index
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.updateThemeIndex(index) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) themeColors[index].copy(alpha = 0.15f) else MaterialTheme.colorScheme.background
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) themeColors[index] else Color.Transparent
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = accentThemes[index],
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) themeColors[index] else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Integration Statuses
        SettingsSectionHeader(title = "HARDWARE & SENSORS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusItemRow(
                    title = "Usage Statistics Access",
                    isGranted = isUsageGranted
                )
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                StatusItemRow(
                    title = "Accessibility Window Tracker",
                    isGranted = isAccessibilityGranted
                )
            }
        }

        // 3. Data & Utilities Panel
        SettingsSectionHeader(title = "DATA OPERATIONS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SettingsUtilityRow(
                    icon = Icons.Default.CloudUpload,
                    title = "Cloud Encrypted Backup",
                    desc = "Synchronize screen patterns to personal cloud backup",
                    onAction = {
                        Toast.makeText(context, "Cloud backup completed successfully (Offline Simulated)", Toast.LENGTH_SHORT).show()
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                SettingsUtilityRow(
                    icon = Icons.Default.CloudDownload,
                    title = "Restore Backup Repository",
                    desc = "Restore historical data records from backup",
                    onAction = {
                        Toast.makeText(context, "Backup restored successfully (Offline Simulated)", Toast.LENGTH_SHORT).show()
                    }
                )
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f))
                SettingsUtilityRow(
                    icon = Icons.Default.DeleteForever,
                    title = "Erase Tracking Database",
                    desc = "Wipe all local sqlite screen time archives",
                    iconColor = Color(0xFFF43F5E),
                    onAction = { showWipeConfirmation = true }
                )
            }
        }

        // 4. Information about App
        SettingsSectionHeader(title = "ABOUT INFINITY FOCUS")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAbout() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Infinity Focus Premium Clocker", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Developer Profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text("Version 1.0.0 Stable (Release Clone)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A full-fidelity, offline-first digital detox clock. Learn more about NexVora Lab's Ofc and the independent developer Prince AR Abdur Rahman.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Wipe Database Dialog
        if (showWipeConfirmation) {
            AlertDialog(
                onDismissRequest = { showWipeConfirmation = false },
                title = { Text("Erase Database archives?") },
                text = { Text("This operation is irreversible and will delete all screen time history logs, goals, and rules.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteData()
                            showWipeConfirmation = false
                            Toast.makeText(context, "All data successfully erased.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Erase All", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWipeConfirmation = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun StatusItemRow(
    title: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isGranted) Color(0xFF065F46).copy(alpha = 0.2f) else Color(0xFF7F1D1D).copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = if (isGranted) "ENABLED" else "DISABLED",
                color = if (isGranted) Color(0xFF10B981) else Color(0xFFF43F5E),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsUtilityRow(
    icon: ImageVector,
    title: String,
    desc: String,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = desc, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
    }
}
