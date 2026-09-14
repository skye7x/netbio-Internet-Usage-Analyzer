package pl.netbio.internetusageanalyzer.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.netbio.internetusageanalyzer.ui.components.GlassBottomNavigation
import pl.netbio.internetusageanalyzer.ui.components.GlassNavItem
import pl.netbio.internetusageanalyzer.ui.screens.dashboard.DashboardScreen
import pl.netbio.internetusageanalyzer.ui.screens.speedtest.SpeedTestScreen
import pl.netbio.internetusageanalyzer.ui.screens.diagnostics.DiagnosticsScreen
import pl.netbio.internetusageanalyzer.ui.screens.wifi.WifiScreen
import pl.netbio.internetusageanalyzer.ui.screens.history.HistoryScreen
import pl.netbio.internetusageanalyzer.ui.screens.alerts.AlertsScreen
import pl.netbio.internetusageanalyzer.ui.screens.appusage.AppUsageScreen
import pl.netbio.internetusageanalyzer.ui.screens.export.ExportScreen
import pl.netbio.internetusageanalyzer.ui.screens.settings.SettingsScreen
import pl.netbio.internetusageanalyzer.ui.theme.DarkBackground

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

    val bottomBarScreens = listOf(Screen.Dashboard.route, Screen.SpeedTest.route, Screen.Diagnostics.route, Screen.History.route)
    val showBottomBar = currentRoute in bottomBarScreens

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
                            0 -> navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Dashboard.route) { inclusive = true } }
                            1 -> navController.navigate(Screen.SpeedTest.route) { popUpTo(Screen.Dashboard.route) }
                            2 -> navController.navigate(Screen.Diagnostics.route) { popUpTo(Screen.Dashboard.route) }
                            3 -> navController.navigate(Screen.History.route) { popUpTo(Screen.Dashboard.route) }
                            4 -> navController.navigate(Screen.Wifi.route) { popUpTo(Screen.Dashboard.route) }
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
        }
    }
}
