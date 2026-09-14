package pl.netbio.internetusageanalyzer.ui.screens.wifi

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
fun WifiScreen(
    viewModel: WifiViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
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
                Text("Network Info", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassAlert(
                title = if (uiState.isConnected) "Connected: ${uiState.networkType}" else "Disconnected",
                message = if (uiState.isConnected) "Active network: ${uiState.networkType}" else "No active connection",
                type = if (uiState.isConnected) GlassAlertType.Success else GlassAlertType.Error,
                icon = if (uiState.isConnected) Icons.Default.CheckCircle else Icons.Default.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            uiState.wifiInfo?.let { info ->
                val frequencyBand = when {
                    info.frequency in 2400..2500 -> "2.4 GHz"
                    info.frequency in 5150..5850 -> "5 GHz"
                    info.frequency in 5925..7125 -> "6 GHz"
                    else -> "Unknown"
                }
                val channel = when {
                    info.frequency in 2412..2484 -> (info.frequency - 2407) / 5
                    info.frequency in 5170..5825 -> (info.frequency - 5000) / 5
                    info.frequency in 5955..7115 -> (info.frequency - 5950) / 5
                    else -> 0
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Wi-Fi Information", subtitle = info.ssid)
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassListItem(icon = Icons.Default.Wifi, title = "SSID", subtitle = info.ssid, iconTint = WifiColor)
                    HorizontalDivider(color = GlassHigh)
                    GlassListItem(icon = Icons.Default.Router, title = "BSSID", subtitle = info.bssid, iconTint = AccentPurple)
                    HorizontalDivider(color = GlassHigh)
                    GlassListItem(icon = Icons.Default.Language, title = "IP Address", subtitle = info.ipAddress, iconTint = AccentCyan)
                    HorizontalDivider(color = GlassHigh)
                    GlassListItem(icon = Icons.Default.Category, title = "Frequency", subtitle = "$frequencyBand (Ch. $channel)", iconTint = AccentOrange)
                    HorizontalDivider(color = GlassHigh)
                    GlassListItem(icon = Icons.Default.Speed, title = "Link Speed", subtitle = "${info.linkSpeed} Mbps", iconTint = AccentBlue)
                    HorizontalDivider(color = GlassHigh)
                    GlassListItem(icon = Icons.Default.SignalCellularAlt, title = "RSSI", subtitle = "${info.rssi} dBm", iconTint = AccentGreen)
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Signal Strength")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassProgressBar(
                        progress = uiState.signalStrength / 100f,
                        label = "Signal Level",
                        percentageText = "${uiState.signalStrength}%",
                        fillColor = when {
                            uiState.signalStrength >= 75 -> SuccessGreen
                            uiState.signalStrength >= 50 -> AccentBlue
                            uiState.signalStrength >= 25 -> AccentOrange
                            else -> ErrorRed
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GlassMetricRow(
                        label = "Connection Quality",
                        value = uiState.connectionQuality,
                        icon = Icons.Default.TrendingUp,
                        iconTint = when (uiState.connectionQuality) {
                            "Excellent" -> SuccessGreen
                            "Good" -> AccentBlue
                            "Fair" -> AccentOrange
                            else -> ErrorRed
                        }
                    )
                }
            } ?: run {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Not connected to Wi-Fi", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        Text("Connect to a Wi-Fi network to see details", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            }

            if (uiState.networkHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Network History", subtitle = "${uiState.networkHistory.size} entries")
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.networkHistory.take(5).forEach { entry ->
                        GlassMetricRow(
                            label = entry.ssid.ifEmpty { entry.networkType },
                            value = java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(entry.timestamp)),
                            icon = Icons.Default.History,
                            iconTint = TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
