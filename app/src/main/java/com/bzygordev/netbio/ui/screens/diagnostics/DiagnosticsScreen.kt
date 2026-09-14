package com.bzygordev.netbio.ui.screens.diagnostics

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
import com.bzygordev.netbio.ui.components.*
import com.bzygordev.netbio.ui.theme.*

@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Diagnostics", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            val isConnected = uiState.connectionResult?.isReachable == true
            GlassAlert(
                title = if (uiState.connectionResult != null) {
                    if (isConnected) "Connected" else "Disconnected"
                } else "Not Tested",
                message = if (uiState.connectionResult != null) {
                    if (isConnected) "Internet connection is active" else "No internet connection"
                } else "Run a connection test to check status",
                type = if (uiState.connectionResult == null) GlassAlertType.Info else if (isConnected) GlassAlertType.Success else GlassAlertType.Error,
                icon = if (uiState.connectionResult == null) Icons.Default.Help else if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Run Diagnostics")
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassButton(
                        text = "Connection",
                        onClick = { viewModel.testConnection() },
                        icon = Icons.Default.Wifi,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isRunning
                    )
                    GlassButton(
                        text = "DNS",
                        onClick = { viewModel.testDns() },
                        icon = Icons.Default.Dns,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isRunning
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassButton(
                        text = "Latency",
                        onClick = { viewModel.testLatency() },
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isRunning
                    )
                    GlassButton(
                        text = "Stability",
                        onClick = { viewModel.testStability() },
                        icon = Icons.Default.Equalizer,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isRunning
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.connectionResult?.let { result ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Connection Test")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassAlert(
                        title = if (result.isReachable) "Connection OK" else "Connection Failed",
                        message = "IP: ${result.ipAddress} | Host: ${result.hostname}",
                        type = if (result.isReachable) GlassAlertType.Success else GlassAlertType.Error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassMetricRow(label = "Status", value = if (result.isReachable) "Reachable" else "Unreachable", icon = Icons.Default.CheckCircle, iconTint = if (result.isReachable) SuccessGreen else ErrorRed)
                    GlassMetricRow(label = "Latency", value = "${result.latencyMs}ms", icon = Icons.Default.Speed, iconTint = AccentBlue)
                    GlassMetricRow(label = "IP Address", value = result.ipAddress, icon = Icons.Default.Language, iconTint = AccentCyan)
                    GlassMetricRow(label = "Interface", value = result.interfaceName, icon = Icons.Default.Settings, iconTint = TextSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.dnsResults.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "DNS Test Results")
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.dnsResults.forEach { dns ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dns.domain, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Text(dns.resolvedIp, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                            Text(
                                "${dns.resolutionTimeMs}ms",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (dns.isSuccessful) SuccessGreen else ErrorRed
                            )
                        }
                        HorizontalDivider(color = GlassHigh)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.latencyResults.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Latency Test")
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.latencyResults.forEach { lat ->
                        GlassMetricRow(
                            label = lat.target,
                            value = "${lat.latencyMs}ms",
                            icon = if (lat.isReachable) Icons.Default.CheckCircle else Icons.Default.Error,
                            iconTint = if (lat.isReachable) AccentBlue else ErrorRed
                        )
                        HorizontalDivider(color = GlassHigh)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            uiState.stabilityResult?.let { result ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Stability Test")
                    Spacer(modifier = Modifier.height(8.dp))

                    val isStable = result.packetLossPercent < 5f && result.jitterMs < 10f
                    GlassAlert(
                        title = if (isStable) "Stable Connection" else "Unstable Connection",
                        message = "Avg: ${result.averageLatencyMs}ms | Jitter: ${String.format("%.1f", result.jitterMs)}ms | Loss: ${String.format("%.1f", result.packetLossPercent)}%",
                        type = if (isStable) GlassAlertType.Success else GlassAlertType.Warning,
                        icon = if (isStable) Icons.Default.CheckCircle else Icons.Default.Warning
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Avg Ping",
                            value = "${result.averageLatencyMs}ms",
                            icon = Icons.Default.Speed,
                            iconTint = AccentBlue
                        )
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Jitter",
                            value = "${String.format("%.1f", result.jitterMs)}ms",
                            icon = Icons.Default.ShowChart,
                            iconTint = AccentOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Packet Loss",
                            value = "${String.format("%.1f", result.packetLossPercent)}%",
                            icon = Icons.Default.ErrorOutline,
                            iconTint = if (result.packetLossPercent > 5) ErrorRed else SuccessGreen
                        )
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Packets",
                            value = "${result.successfulPings}/${result.totalPings}",
                            icon = Icons.Default.Equalizer,
                            iconTint = AccentCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassMetricRow(label = "Min Latency", value = "${result.minLatencyMs}ms")
                    GlassMetricRow(label = "Max Latency", value = "${result.maxLatencyMs}ms")
                    GlassMetricRow(label = "Failed Pings", value = "${result.failedPings}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
