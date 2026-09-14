package pl.netbio.internetusageanalyzer.ui.screens.diagnostics

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
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
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
                Text("Diagnostics", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Connection status
            GlassAlert(
                title = if (isConnected) "Connected" else "Disconnected",
                message = if (isConnected) "Your internet connection is active" else "No internet connection detected",
                type = if (isConnected) GlassAlertType.Success else GlassAlertType.Error,
                icon = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Test buttons
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Run Diagnostics")
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassButton(
                        text = "Connection",
                        onClick = { viewModel.testConnection() },
                        icon = Icons.Default.Wifi,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "DNS",
                        onClick = { viewModel.testDns() },
                        icon = Icons.Default.Dns,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassButton(
                        text = "Latency",
                        onClick = { viewModel.testLatency() },
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Stability",
                        onClick = { viewModel.testStability() },
                        icon = Icons.Default.Equalizer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Connection test result
            uiState.connectionResult?.let { result ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Connection Test")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassAlert(
                        title = if (result.isConnected) "Connection OK" else "Connection Failed",
                        message = "Latency: ${String.format("%.0f", result.latencyMs)}ms | Status: ${result.statusCode}",
                        type = if (result.isConnected) GlassAlertType.Success else GlassAlertType.Error
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // DNS results
            if (uiState.dnsResults.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "DNS Test Results")
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.dnsResults.forEach { dns ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dns.server, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Text(dns.resolvedIp, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                            Text(
                                "${String.format("%.1f", dns.latencyMs)}ms",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (dns.isSuccessful) SuccessGreen else ErrorRed
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Latency results
            if (uiState.latencyResults.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Latency Test")
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.latencyResults.forEach { lat ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lat.target, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text(
                                "${String.format("%.0f", lat.latencyMs)}ms",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (lat.isSuccessful) AccentBlue else ErrorRed
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Stability result
            uiState.stabilityResult?.let { result ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Stability Test")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(modifier = Modifier.weight(1f), label = "Avg Ping", value = "${String.format("%.0f", result.avgLatency)}ms")
                        GlassStatCard(modifier = Modifier.weight(1f), label = "Jitter", value = "${String.format("%.1f", result.jitter)}ms")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(modifier = Modifier.weight(1f), label = "Packet Loss", value = "${String.format("%.1f", result.packetLoss)}%")
                        GlassStatCard(modifier = Modifier.weight(1f), label = "Stability", value = if (result.isStable) "Stable" else "Unstable")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
