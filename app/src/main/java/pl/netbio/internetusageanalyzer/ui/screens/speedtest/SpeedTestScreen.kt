package pl.netbio.internetusageanalyzer.ui.screens.speedtest

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.netbio.internetusageanalyzer.ui.components.*
import pl.netbio.internetusageanalyzer.ui.theme.*

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel = hiltViewModel(),
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
                Text("Speed Test", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (uiState.isRunning) {
                        GlassCircularProgress(
                            progress = uiState.progress,
                            size = 180.dp,
                            strokeWidth = 14.dp,
                            label = uiState.phase,
                            value = when {
                                uiState.phase.contains("ownload", ignoreCase = true) -> String.format("%.1f", uiState.currentDownload)
                                uiState.phase.contains("pload", ignoreCase = true) -> String.format("%.1f", uiState.currentUpload)
                                uiState.phase.contains("ing", ignoreCase = true) -> String.format("%.0f", uiState.ping)
                                else -> ""
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                uiState.phase.contains("ownload", ignoreCase = true) -> "Mbps"
                                uiState.phase.contains("pload", ignoreCase = true) -> "Mbps"
                                uiState.phase.contains("ing", ignoreCase = true) -> "ms"
                                else -> uiState.phase
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    } else if (uiState.latestResult != null) {
                        val result = uiState.latestResult!!
                        Text(
                            text = String.format("%.1f", result.downloadSpeed),
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                            color = AccentBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        text = if (uiState.isRunning) "Cancel" else if (uiState.latestResult != null) "Test Again" else "Start Test",
                        onClick = { if (uiState.isRunning) viewModel.cancelTest() else viewModel.startTest() },
                        icon = if (uiState.isRunning) Icons.Default.Close else Icons.Default.PlayArrow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.latestResult != null) {
                val result = uiState.latestResult!!
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

            if (uiState.recentResults.isNotEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(
                        title = "Recent Tests",
                        subtitle = "${uiState.recentResults.size} tests",
                        action = {
                            GlassChip(text = "Clear", onClick = { viewModel.clearHistory() })
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    uiState.recentResults.take(5).forEach { test ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "\u2193 ${String.format("%.1f", test.downloadSpeed)} / \u2191 ${String.format("%.1f", test.uploadSpeed)} Mbps",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    "Ping: ${String.format("%.0f", test.ping)}ms | Jitter: ${String.format("%.1f", test.jitter)}ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                            Text(
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(test.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                        HorizontalDivider(color = GlassHigh)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
