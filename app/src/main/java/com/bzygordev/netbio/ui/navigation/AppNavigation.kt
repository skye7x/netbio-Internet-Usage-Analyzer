package com.bzygordev.netbio.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bzygordev.netbio.ui.components.*
import com.bzygordev.netbio.ui.screens.alerts.AlertsScreen
import com.bzygordev.netbio.ui.screens.appusage.AppUsageScreen
import com.bzygordev.netbio.ui.screens.dashboard.DashboardScreen
import com.bzygordev.netbio.ui.screens.diagnostics.DiagnosticsScreen
import com.bzygordev.netbio.ui.screens.export.ExportScreen
import com.bzygordev.netbio.ui.screens.history.HistoryScreen
import com.bzygordev.netbio.ui.screens.settings.SettingsScreen
import com.bzygordev.netbio.ui.screens.speedtest.SpeedTestScreen
import com.bzygordev.netbio.ui.screens.wifi.WifiScreen
import com.bzygordev.netbio.ui.theme.*

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object SpeedTest : Screen("speedtest")
    data object Diagnostics : Screen("diagnostics")
    data object Wifi : Screen("wifi")
    data object History : Screen("history")
    data object Alerts : Screen("alerts")
    data object AppUsage : Screen("appusage")
    data object Export : Screen("export")
    data object Settings : Screen("settings")
}

val bottomNavItems = listOf(
    GlassNavItem(icon = Icons.Default.Home, label = "Home"),
    GlassNavItem(icon = Icons.Default.Speed, label = "Speed"),
    GlassNavItem(icon = Icons.Default.NetworkCheck, label = "Diag"),
    GlassNavItem(icon = Icons.Default.History, label = "History"),
    GlassNavItem(icon = Icons.Default.MoreHoriz, label = "More")
)

@Composable
fun AppNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainRoutes = listOf(
        Screen.Dashboard.route,
        Screen.SpeedTest.route,
        Screen.Diagnostics.route,
        Screen.History.route
    )
    val showBottomBar = currentRoute in mainRoutes || currentRoute == "more"

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                val selectedIndex = when (currentRoute) {
                    Screen.Dashboard.route -> 0
                    Screen.SpeedTest.route -> 1
                    Screen.Diagnostics.route -> 2
                    Screen.History.route -> 3
                    else -> 4
                }
                GlassBottomNavigation(
                    items = bottomNavItems,
                    selectedItem = selectedIndex,
                    onItemClick = { index ->
                        when (index) {
                            0 -> navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                                launchSingleTop = true
                            }
                            1 -> navController.navigate(Screen.SpeedTest.route) {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                            2 -> navController.navigate(Screen.Diagnostics.route) {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                            3 -> navController.navigate(Screen.History.route) {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                            4 -> navController.navigate("more") {
                                popUpTo(Screen.Dashboard.route)
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToSpeedtest = { navController.navigate(Screen.SpeedTest.route) },
                    onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                    onNavigateToWifi = { navController.navigate(Screen.Wifi.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToAppUsage = { navController.navigate(Screen.AppUsage.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.SpeedTest.route) {
                SpeedTestScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Diagnostics.route) {
                DiagnosticsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Wifi.route) {
                WifiScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.History.route) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Alerts.route) {
                AlertsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.AppUsage.route) {
                AppUsageScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Export.route) {
                ExportScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onExport = { navController.navigate(Screen.Export.route) }
                )
            }
            composable("more") {
                MoreScreen(
                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                    onNavigateToAppUsage = { navController.navigate(Screen.AppUsage.route) },
                    onNavigateToExport = { navController.navigate(Screen.Export.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToWifi = { navController.navigate(Screen.Wifi.route) }
                )
            }
        }
    }
}

@Composable
private fun MoreScreen(
    onNavigateToAlerts: () -> Unit,
    onNavigateToAppUsage: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWifi: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        GlassGlowBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                "More",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Tools")
                Spacer(modifier = Modifier.height(8.dp))

                GlassListItem(
                    icon = Icons.Default.Notifications,
                    title = "Alerts & Limits",
                    subtitle = "Manage alerts and usage limits",
                    iconTint = AccentOrange,
                    onClick = onNavigateToAlerts
                )
                HorizontalDivider(color = GlassHigh)
                GlassListItem(
                    icon = Icons.Default.Apps,
                    title = "App Usage",
                    subtitle = "View per-app data usage",
                    iconTint = AccentPurple,
                    onClick = onNavigateToAppUsage
                )
                HorizontalDivider(color = GlassHigh)
                GlassListItem(
                    icon = Icons.Default.Wifi,
                    title = "Wi-Fi Info",
                    subtitle = "Network details and signal strength",
                    iconTint = WifiColor,
                    onClick = onNavigateToWifi
                )
                HorizontalDivider(color = GlassHigh)
                GlassListItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export Data",
                    subtitle = "Export usage data to CSV or JSON",
                    iconTint = AccentBlue,
                    onClick = onNavigateToExport
                )
                HorizontalDivider(color = GlassHigh)
                GlassListItem(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    subtitle = "App configuration and preferences",
                    iconTint = TextTertiary,
                    onClick = onNavigateToSettings
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
