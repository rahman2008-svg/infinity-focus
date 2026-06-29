package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "onboarding"
                ) {
                    composable("onboarding") {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinish = {
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToDetails = { packageName ->
                                navController.navigate("details/$packageName")
                            }
                        )
                    }

                    composable(
                        route = "details/{packageName}",
                        arguments = listOf(navArgument("packageName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                        AppDetailsScreen(
                            packageName = packageName,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh permissions status when coming back from system settings!
        viewModel.refreshSystemStatus()
        viewModel.syncUsageData()
    }
}

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Stats, 2: Focus, 3: Settings

    // Settings nested screens state
    var activeSettingSubScreen by remember { mutableStateOf<String?>(null) } // "limits", "schedules", "goals", "reports"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (activeSettingSubScreen == null) {
                NavigationBar(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF38BDF8)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Home else Icons.Default.Home,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF64748B),
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_dashboard")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.BarChart else Icons.Default.BarChart,
                                contentDescription = "Statistics"
                            )
                        },
                        label = { Text("Statistics") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF64748B),
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_statistics")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.Adjust else Icons.Default.Adjust,
                                contentDescription = "Focus"
                            )
                        },
                        label = { Text("Focus") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF64748B),
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_focus")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 3) Icons.Default.Settings else Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF64748B),
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeSettingSubScreen != null) {
                // Settings Nested Screens
                when (activeSettingSubScreen) {
                    "limits" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppLimitsScreen(viewModel = viewModel)
                            IconButton(
                                onClick = { activeSettingSubScreen = null },
                                modifier = Modifier.padding(16.dp).align(androidx.compose.ui.Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                    "schedules" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            SchedulesScreen(viewModel = viewModel)
                            IconButton(
                                onClick = { activeSettingSubScreen = null },
                                modifier = Modifier.padding(16.dp).align(androidx.compose.ui.Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                    "goals" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            GoalsScreen(viewModel = viewModel)
                            IconButton(
                                onClick = { activeSettingSubScreen = null },
                                modifier = Modifier.padding(16.dp).align(androidx.compose.ui.Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                    "reports" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ReportsScreen(viewModel = viewModel)
                            IconButton(
                                onClick = { activeSettingSubScreen = null },
                                modifier = Modifier.padding(16.dp).align(androidx.compose.ui.Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                    "about" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AboutScreen(onBack = { activeSettingSubScreen = null })
                            // Back button inside AboutScreen is enough, but adding close icon too for UI consistency
                            IconButton(
                                onClick = { activeSettingSubScreen = null },
                                modifier = Modifier.padding(16.dp).align(androidx.compose.ui.Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Main Bottom Bar Navigation tab switcher
                when (selectedTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = onNavigateToDetails
                    )
                    1 -> StatisticsScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = onNavigateToDetails
                    )
                    2 -> FocusModeScreen(
                        viewModel = viewModel
                    )
                    3 -> Column(modifier = Modifier.fillMaxSize()) {
                        // Quick Shortcuts Grid at top of Settings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ShortcutSettingCard(
                                icon = Icons.Default.Timer,
                                title = "Limits",
                                modifier = Modifier.weight(1f),
                                onClick = { activeSettingSubScreen = "limits" }
                            )
                            ShortcutSettingCard(
                                icon = Icons.Default.Schedule,
                                title = "Schedules",
                                modifier = Modifier.weight(1f),
                                onClick = { activeSettingSubScreen = "schedules" }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ShortcutSettingCard(
                                icon = Icons.Default.EmojiEvents,
                                title = "Goals",
                                modifier = Modifier.weight(1f),
                                onClick = { activeSettingSubScreen = "goals" }
                            )
                            ShortcutSettingCard(
                                icon = Icons.Default.Assessment,
                                title = "Reports",
                                modifier = Modifier.weight(1f),
                                onClick = { activeSettingSubScreen = "reports" }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render regular system settings screen below
                        SettingsScreen(viewModel = viewModel, onNavigateToAbout = { activeSettingSubScreen = "about" })
                    }
                }
            }
        }
    }
}

@Composable
fun ShortcutSettingCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
