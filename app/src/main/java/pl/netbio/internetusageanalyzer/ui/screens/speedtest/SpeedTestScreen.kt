package pl.netbio.internetusageanalyzer.ui.screens.speedtest

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.netbio.internetusageanalyzer.ui.components.*
import pl.netbio.internetusageanalyzer.ui.theme.*
import pl.netbio.internetusageanalyzer.service.SpeedTestPhase

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val phase by viewModel.phase.collectAsState()
    val currentDownload by viewModel.downloadSpeed.collectAsState()
    val currentUpload by viewModel.uploadSpeed.collectAsState()
    val ping by viewModel.ping.collectAsState()

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        GlassGlowBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Speed Test", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main speed display
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (isRunning) {
                        GlassCircularProgress(
                            progress = progress,
                            size = 180.dp,
                            strokeWidth = 14.dp,
                            label = when (phase) {
                                SpeedTestPhase.PING -> "Testing Ping..."
                                SpeedTestPhase.DOWNLOAD -> "Testing Download..."
                                SpeedTestPhase.UPLOAD -> "Testing Upload..."
                                else -> "Preparing..."
                            },
                            value = when (phase) {
                                SpeedTestPhase.DOWNLOAD -> String.format("%.1f", currentDownload)
                                SpeedTestPhase.UPLOAD -> String.format("%.1f", currentUpload)
                                SpeedTestPhase.PING -> String.format("%.0f", ping)
                                else -> ""
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (phase) {
                                SpeedTestPhase.DOWNLOAD -> "Mbps ↓"
                                SpeedTestPhase.UPLOAD -> "Mbps ↑"
                                SpeedTestPhase.PING -> "ms"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    } else if (uiState.currentResult != null) {
                        val result = uiState.currentResult!!
                        Text(
                            text = String.format("%.1f", result.downloadSpeed),
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                            color = AccentBlue
                        )
                        Text("Mbps Download", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                    } else {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to Start", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    GlassButton(
                        text = if (isRunning) "Cancel" else if (uiState.currentResult != null) "Test Again" else "Start Test",
                        onClick = { if (isRunning) viewModel.cancelTest() else viewModel.startTest() },
                        icon = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results
            if (uiState.currentResult != null) {
                val result = uiState.currentResult!!
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Results")
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Download",
                            value = String.format("%.1f Mbps", result.downloadSpeed),
                            icon = Icons.Default.ArrowDownward,
                            iconTint = AccentBlue
                        )
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Upload",
                            value = String.format("%.1f Mbps", result.uploadSpeed),
                            icon = Icons.Default.ArrowUpward,
                            iconTint = AccentPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Ping",
                            value = String.format("%.0f ms", result.ping),
                            icon = Icons.Default.NetworkCheck,
                            iconTint = AccentGreen
                        )
                        GlassStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Jitter",
                            value = String.format("%.1f ms", result.jitter),
                            icon = Icons.Default.ShowChart,
                            iconTint = AccentOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassStatCard(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Packet Loss",
                        value = String.format("%.1f%%", result.packetLoss),
                        icon = Icons.Default.ErrorOutline,
                        iconTint = if (result.packetLoss > 5) ErrorRed else SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History
            if (uiState.history.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Recent Tests", subtitle = "${uiState.history.size} tests")
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.history.take(10).forEach { test ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "↓ ${String.format("%.1f", test.downloadSpeed)} / ↑ ${String.format("%.1f", test.uploadSpeed)} Mbps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    "Ping: ${String.format("%.0f", test.ping)}ms | Jitter: ${String.format("%.1f", test.jitter)}ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                        HorizontalDivider(color = GlassWhite10)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
