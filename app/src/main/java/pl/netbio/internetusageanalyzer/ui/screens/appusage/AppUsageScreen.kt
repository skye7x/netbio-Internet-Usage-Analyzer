package pl.netbio.internetusageanalyzer.ui.screens.appusage

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.netbio.internetusageanalyzer.ui.components.*
import pl.netbio.internetusageanalyzer.ui.theme.*

private val pieColors = listOf(AccentBlue, AccentPurple, AccentCyan, AccentOrange, AccentGreen)

@Composable
fun AppUsageScreen(
    viewModel: AppUsageViewModel = hiltViewModel(),
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
                Text("App Usage", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Total Usage", subtitle = "Today")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    viewModel.formatBytes(uiState.totalUsage),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = AccentBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        CircularProgressIndicator(color = AccentBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading app usage...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            } else if (uiState.appUsageList.isNotEmpty() && uiState.appUsageList.size >= 5) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Top 5 Apps")
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val top5 = uiState.appUsageList.take(5)
                        val segments = top5.mapIndexed { index, app ->
                            PieSegment(
                                value = app.usagePercent,
                                color = pieColors[index % pieColors.size],
                                label = app.appName
                            )
                        }
                        GlassPieChart(segments = segments, size = 140.dp)

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            top5.forEachIndexed { index, app ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp)) {
                                        Surface(
                                            modifier = Modifier.size(10.dp),
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = pieColors[index % pieColors.size]
                                        ) {}
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        app.appName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!uiState.isLoading && uiState.appUsageList.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No app data yet", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        Text("App usage will appear here", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            } else if (!uiState.isLoading) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "App Ranking", subtitle = "${uiState.appUsageList.size} apps")
                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.appUsageList.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = when (index) {
                                    0 -> AccentBlue
                                    1 -> AccentPurple
                                    2 -> AccentCyan
                                    else -> TextTertiary
                                },
                                modifier = Modifier.width(30.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    app.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    viewModel.formatBytes(app.usageBytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                GlassProgressBar(
                                    progress = app.usagePercent / 100f,
                                    trackHeight = 4.dp,
                                    fillColor = when (index) {
                                        0 -> AccentBlue
                                        1 -> AccentPurple
                                        2 -> AccentCyan
                                        3 -> AccentOrange
                                        4 -> AccentGreen
                                        else -> AccentBlue
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "${String.format("%.1f", app.usagePercent)}%",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = when (index) {
                                    0 -> AccentBlue
                                    1 -> AccentPurple
                                    2 -> AccentCyan
                                    else -> TextTertiary
                                }
                            )
                        }
                        if (index < uiState.appUsageList.size - 1) {
                            HorizontalDivider(color = GlassHigh)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
