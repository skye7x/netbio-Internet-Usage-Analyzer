package pl.netbio.internetusageanalyzer.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.netbio.internetusageanalyzer.ui.components.*
import pl.netbio.internetusageanalyzer.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onExport: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        GlassGlowBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Settings", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Limits")
                Spacer(modifier = Modifier.height(8.dp))
                GlassListItem(
                    icon = Icons.Default.DataUsage,
                    title = "Daily Limit",
                    subtitle = if (uiState.dailyLimit > 0) "${String.format("%.1f", uiState.dailyLimit / (1024.0 * 1024 * 1024))} GB" else "Not set",
                    iconTint = AccentBlue
                )
                HorizontalDivider(color = GlassHigh)
                GlassListItem(
                    icon = Icons.Default.DateRange,
                    title = "Weekly Limit",
                    subtitle = if (uiState.weeklyLimit > 0) "${String.format("%.1f", uiState.weeklyLimit / (1024.0 * 1024 * 1024))} GB" else "Not set",
                    iconTint = AccentPurple
                )
                HorizontalDivider(color = GlassHigh)
                GlassListItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Monthly Limit",
                    subtitle = if (uiState.monthlyLimit > 0) "${String.format("%.1f", uiState.monthlyLimit / (1024.0 * 1024 * 1024))} GB" else "Not set",
                    iconTint = AccentCyan
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Alerts")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Alert Threshold", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Text("${String.format("%.0f", uiState.alertThreshold)}%", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = uiState.alertThreshold,
                    onValueChange = { viewModel.updateSetting("alertThreshold", it) },
                    valueRange = 50f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentBlue,
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = GlassHigh
                    )
                )
                HorizontalDivider(color = GlassHigh)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Peak Hours Alerts", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = uiState.peakHoursEnabled, onCheckedChange = { viewModel.updateSetting("peakHoursEnabled", it) })
                }
                HorizontalDivider(color = GlassHigh)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Anomaly Detection", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = uiState.anomalyDetectionEnabled, onCheckedChange = { viewModel.updateSetting("anomalyDetectionEnabled", it) })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Speed Test")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto Speed Test", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = uiState.autoSpeedTestEnabled, onCheckedChange = { viewModel.updateSetting("autoSpeedTestEnabled", it) })
                }
                if (uiState.autoSpeedTestEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassListItem(
                        icon = Icons.Default.Timer,
                        title = "Interval",
                        subtitle = "Every ${uiState.autoSpeedTestInterval} minutes",
                        iconTint = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Security")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("App Lock", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = uiState.isAppLockEnabled, onCheckedChange = { viewModel.updateSetting("isAppLockEnabled", it) })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Data")
                Spacer(modifier = Modifier.height(8.dp))
                GlassListItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export Data",
                    subtitle = "Export usage and speedtest data",
                    iconTint = AccentBlue,
                    onClick = onExport
                )
                HorizontalDivider(color = GlassHigh)
                GlassListItem(
                    icon = Icons.Default.Delete,
                    title = "Clear History",
                    subtitle = "Remove all usage data",
                    iconTint = ErrorRed,
                    onClick = { viewModel.resetAllData() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "About")
                Spacer(modifier = Modifier.height(8.dp))
                GlassListItem(icon = Icons.Default.Info, title = "Version", subtitle = uiState.appVersion, iconTint = TextTertiary)
                HorizontalDivider(color = GlassHigh)
                GlassListItem(icon = Icons.Default.Code, title = "Developer", subtitle = "NetBio", iconTint = TextTertiary)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
