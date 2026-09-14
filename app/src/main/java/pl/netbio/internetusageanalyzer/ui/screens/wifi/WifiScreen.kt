package pl.netbio.internetusageanalyzer.ui.screens.wifi

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
fun WifiScreen(
    viewModel: WifiViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val networkType by viewModel.networkType.collectAsState()
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
                Text("Network Info", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Connection status
            GlassAlert(
                title = if (isConnected) "Connected: $networkType" else "Disconnected",
                message = if (isConnected) "Active network: $networkType" else "No active connection",
                type = if (isConnected) GlassAlertType.Success else GlassAlertType.Error,
                icon = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            // WiFi Info
            uiState.wifiInfo?.let { info ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Wi-Fi Information", subtitle = info.ssid)
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassListItem(icon = Icons.Default.Wifi, title = "SSID", subtitle = info.ssid, iconTint = WifiColor)
                    HorizontalDivider(color = GlassWhite10)
                    GlassListItem(icon = Icons.Default.Router, title = "BSSID", subtitle = info.bssid, iconTint = AccentPurple)
                    HorizontalDivider(color = GlassWhite10)
                    GlassListItem(icon = Icons.Default.SignalCellularAlt, title = "Signal Strength", subtitle = "${uiState.signalDescription} (${uiState.signalStrength}%)", iconTint = AccentGreen)
                    HorizontalDivider(color = GlassWhite10)
                    GlassListItem(icon = Icons.Default.Speed, title = "Link Speed", subtitle = "${info.linkSpeed} Mbps", iconTint = AccentBlue)
                    HorizontalDivider(color = GlassWhite10)
                    GlassListItem(icon = Icons.Default.Category, title = "Frequency", subtitle = "${uiState.frequencyBand} (Ch. ${info.channel})", iconTint = AccentOrange)
                    HorizontalDivider(color = GlassWhite10)
                    GlassListItem(icon = Icons.Default.Security, title = "Security", subtitle = if (info.isSecure) "Secured" else "Open", iconTint = if (info.isSecure) SuccessGreen else ErrorRed)
                    HorizontalDivider(color = GlassWhite10)
                    GlassListItem(icon = Icons.Default.Language, title = "IP Address", subtitle = info.ipAddress, iconTint = AccentCyan)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Signal strength progress
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassProgressBar(
                        progress = uiState.signalStrength / 100f,
                        label = "Signal Strength",
                        percentageText = "${uiState.signalStrength}%"
                    )
                }
            } ?: run {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Not connected to Wi-Fi", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save network info
            GlassButton(
                text = "Save Network Info",
                onClick = { viewModel.saveNetworkInfo() },
                icon = Icons.Default.Save,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
