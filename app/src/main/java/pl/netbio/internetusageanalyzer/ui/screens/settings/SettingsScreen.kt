package pl.netbio.internetusageanalyzer.ui.screens.settings

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
import androidx.hilt.navigation.compose.hiltViewModel
import pl.netbio.internetusageanalyzer.ui.components.*
import pl.netbio.internetusageanalyzer.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onExport: () -> Unit = {}
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = true)
    val isPinEnabled by viewModel.isPinEnabled.collectAsState(initial = false)
    val biometricEnabled by viewModel.biometricEnabled.collectAsState(initial = false)
    val dailyLimit by viewModel.dailyLimit.collectAsState(initial = 0L)
    val weeklyLimit by viewModel.weeklyLimit.collectAsState(initial = 0L)
    val monthlyLimit by viewModel.monthlyLimit.collectAsState(initial = 0L)
    val warningPct by viewModel.warningPercentage.collectAsState(initial = 80f)
    val alertPct by viewModel.alertPercentage.collectAsState(initial = 95f)
    val scheduledEnabled by viewModel.scheduledSpeedtestEnabled.collectAsState(initial = false)
    val scheduledInterval by viewModel.scheduledSpeedtestInterval.collectAsState(initial = 60)
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
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Settings", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Appearance")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = isDarkMode, onCheckedChange = { viewModel.setDarkMode(it) })
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
                    Text("PIN Lock", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = isPinEnabled, onCheckedChange = { viewModel.setPinEnabled(it) })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Biometric", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = biometricEnabled, onCheckedChange = { viewModel.setBiometricEnabled(it) })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Usage Limits")
                Spacer(modifier = Modifier.height(8.dp))
                GlassListItem(
                    icon = Icons.Default.DataUsage,
                    title = "Daily Limit",
                    subtitle = if (dailyLimit > 0) "${String.format("%.1f", dailyLimit / (1024.0 * 1024 * 1024))} GB" else "Not set",
                    iconTint = AccentBlue
                )
                HorizontalDivider(color = GlassWhite10)
                GlassListItem(
                    icon = Icons.Default.DateRange,
                    title = "Weekly Limit",
                    subtitle = if (weeklyLimit > 0) "${String.format("%.1f", weeklyLimit / (1024.0 * 1024 * 1024))} GB" else "Not set",
                    iconTint = AccentPurple
                )
                HorizontalDivider(color = GlassWhite10)
                GlassListItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Monthly Limit",
                    subtitle = if (monthlyLimit > 0) "${String.format("%.1f", monthlyLimit / (1024.0 * 1024 * 1024))} GB" else "Not set",
                    iconTint = AccentCyan
                )
                HorizontalDivider(color = GlassWhite10)
                GlassListItem(
                    icon = Icons.Default.Warning,
                    title = "Warning Threshold",
                    subtitle = "${String.format("%.0f", warningPct)}%",
                    iconTint = AccentOrange
                )
                HorizontalDivider(color = GlassWhite10)
                GlassListItem(
                    icon = Icons.Default.Error,
                    title = "Alert Threshold",
                    subtitle = "${String.format("%.0f", alertPct)}%",
                    iconTint = ErrorRed
                )
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
                    Text("Scheduled Tests", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    GlassToggle(checked = scheduledEnabled, onCheckedChange = { viewModel.setScheduledSpeedtest(it) })
                }
                if (scheduledEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassListItem(
                        icon = Icons.Default.Timer,
                        title = "Interval",
                        subtitle = "Every $scheduledInterval minutes",
                        iconTint = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Data Management")
                Spacer(modifier = Modifier.height(8.dp))
                GlassListItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export Data",
                    subtitle = "Export usage and speedtest data",
                    iconTint = AccentBlue,
                    onClick = onExport
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "About")
                Spacer(modifier = Modifier.height(8.dp))
                GlassListItem(icon = Icons.Default.Info, title = "Version", subtitle = "1.0.0", iconTint = TextTertiary)
                HorizontalDivider(color = GlassWhite10)
                GlassListItem(icon = Icons.Default.Code, title = "Developer", subtitle = "NetBio", iconTint = TextTertiary)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
