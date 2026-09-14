package pl.netbio.internetusageanalyzer.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.netbio.internetusageanalyzer.ui.components.*
import pl.netbio.internetusageanalyzer.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSpeedtest: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToWifi: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAppUsage: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        GlassGlowBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 80.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "NetBio",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text("Internet Usage Analyzer", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
                Row {
                    if (uiState.unreadAlerts > 0) {
                        BadgedBox(badge = { Badge { Text("${uiState.unreadAlerts}") } }) {
                            IconButton(onClick = onNavigateToAlerts) {
                                Icon(Icons.Default.Notifications, "Alerts", tint = AccentOrange)
                            }
                        }
                    } else {
                        IconButton(onClick = onNavigateToAlerts) {
                            Icon(Icons.Default.Notifications, "Alerts", tint = TextTertiary)
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextTertiary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    GlassSectionHeader(title = "Today's Usage", subtitle = "Real-time monitoring")
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = viewModel.formatBytes(uiState.todayUsage),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    if (uiState.usageChangePercent != 0f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (uiState.usageChangePercent > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (uiState.usageChangePercent > 0) ErrorRed else SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${String.format("%.1f", kotlin.math.abs(uiState.usageChangePercent))}% vs yesterday",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.usageChangePercent > 0) ErrorRed else SuccessGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Wi-Fi",
                            value = viewModel.formatBytes(uiState.todayWifiUsage),
                            icon = Icons.Default.Wifi,
                            iconTint = WifiColor
                        )
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Mobile",
                            value = viewModel.formatBytes(uiState.todayMobileUsage),
                            icon = Icons.Default.SignalCellularAlt,
                            iconTint = MobileDataColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.dailyLimit > 0) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassProgressBar(
                        progress = (uiState.dailyLimitPercentage / 100f).coerceIn(0f, 1f),
                        label = "Daily Limit",
                        percentageText = "${String.format("%.0f", uiState.dailyLimitPercentage)}%"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${viewModel.formatBytes(uiState.todayUsage)} / ${viewModel.formatBytes(uiState.dailyLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassStatCard(
                    modifier = Modifier.weight(1f),
                    label = "This Week",
                    value = viewModel.formatBytes(uiState.weekUsage),
                    icon = Icons.Default.DateRange,
                    iconTint = AccentPurple
                )
                GlassStatCard(
                    modifier = Modifier.weight(1f),
                    label = "This Month",
                    value = viewModel.formatBytes(uiState.monthUsage),
                    icon = Icons.Default.CalendarMonth,
                    iconTint = AccentCyan
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Real-Time Speed")
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Download",
                        value = viewModel.formatSpeed(uiState.currentDownloadSpeed),
                        icon = Icons.Default.ArrowDownward,
                        iconTint = AccentGreen
                    )
                    GlassStatCard(
                        modifier = Modifier.weight(1f),
                        label = "Upload",
                        value = viewModel.formatSpeed(uiState.currentUploadSpeed),
                        icon = Icons.Default.ArrowUpward,
                        iconTint = AccentBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.hourlyUsage.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Hourly Usage", subtitle = "Today")
                    Spacer(modifier = Modifier.height(8.dp))
                    val maxHourly = uiState.hourlyUsage.maxOfOrNull { it.second } ?: 1L
                    uiState.hourlyUsage.forEach { (hour, bytes) ->
                        GlassMetricRow(
                            label = "${String.format("%02d", hour)}:00",
                            value = viewModel.formatBytes(bytes),
                            icon = Icons.Default.AccessTime,
                            iconTint = AccentBlue
                        )
                        GlassProgressBar(
                            progress = if (maxHourly > 0) (bytes.toFloat() / maxHourly).coerceIn(0f, 1f) else 0f,
                            trackHeight = 6.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.predictedMonthlyUsage > 0) {
                val currentMonthUsage = uiState.monthUsage
                val predictedPct = if (currentMonthUsage > 0) {
                    ((uiState.predictedMonthlyUsage.toFloat() / currentMonthUsage.toFloat()) * 100f).toInt()
                } else 0

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Monthly Prediction", subtitle = "Based on current usage")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = viewModel.formatBytes(uiState.predictedMonthlyUsage),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = AccentPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Predicted ${predictedPct}% of current month usage",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.weeklyDailyUsage.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Weekly Comparison", subtitle = "Mon - Sun")
                    Spacer(modifier = Modifier.height(8.dp))
                    val maxWeekly = uiState.weeklyDailyUsage.maxOfOrNull { it.second } ?: 1L
                    UsageBarChart(
                        data = uiState.weeklyDailyUsage.map { (day, bytes) ->
                            day to (bytes / (1024.0 * 1024)).toFloat()
                        },
                        maxValue = (maxWeekly / (1024.0 * 1024)).toFloat(),
                        barColor = AccentPurple
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.anomalyScore > 0.3f) {
                GlassAlert(
                    title = "Usage Anomaly Detected",
                    message = "Your current usage pattern is ${String.format("%.0f", uiState.anomalyScore * 100)}% higher than normal.",
                    type = GlassAlertType.Warning,
                    icon = Icons.Default.Warning
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Quick Actions")
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassChip(text = "Speedtest", icon = Icons.Default.Speed, onClick = onNavigateToSpeedtest, selected = true)
                    GlassChip(text = "Diagnostics", icon = Icons.Default.NetworkCheck, onClick = onNavigateToDiagnostics)
                    GlassChip(text = "Wi-Fi Info", icon = Icons.Default.Wifi, onClick = onNavigateToWifi)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassChip(text = "History", icon = Icons.Default.History, onClick = onNavigateToHistory)
                    GlassChip(text = "App Usage", icon = Icons.Default.Apps, onClick = onNavigateToAppUsage)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
