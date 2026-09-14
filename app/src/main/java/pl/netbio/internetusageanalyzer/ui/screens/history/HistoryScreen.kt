package pl.netbio.internetusageanalyzer.ui.screens.history

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
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily", "Hourly", "Calendar")
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
                Text("History", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassTabBar(items = tabs, selectedIndex = selectedTab, onSelect = { selectedTab = it })

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    if (uiState.dailySummaries.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No data yet", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Text("Usage data will appear here", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    } else {
                        uiState.dailySummaries.forEach { summary ->
                            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(summary.date, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    }
                                    Text(
                                        viewModel.formatBytes(summary.total),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AccentBlue
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Hourly Usage", subtitle = "Today")
                        Spacer(modifier = Modifier.height(12.dp))
                        if (uiState.hourlyUsage.isEmpty()) {
                            Text("No hourly data available", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        } else {
                            val maxUsage = uiState.hourlyUsage.maxOfOrNull { it.totalBytes } ?: 1L
                            (0..23).forEach { hour ->
                                val hourData = uiState.hourlyUsage.find { it.hour == hour }
                                val usage = hourData?.totalBytes ?: 0L
                                val fraction = if (maxUsage > 0) usage.toFloat() / maxUsage else 0f
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${String.format("%02d", hour)}:00",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary,
                                        modifier = Modifier.width(45.dp)
                                    )
                                    GlassProgressBar(
                                        progress = fraction,
                                        modifier = Modifier.weight(1f),
                                        trackHeight = 8.dp
                                    )
                                    Text(
                                        viewModel.formatBytes(usage),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.width(70.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Usage Calendar")
                        Spacer(modifier = Modifier.height(12.dp))
                        val summaryMap = uiState.dailySummaries.associate { it.date to it.total }
                        val cal = java.util.Calendar.getInstance()
                        val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        val currentDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
                        val month = cal.get(java.util.Calendar.MONTH) + 1
                        val year = cal.get(java.util.Calendar.YEAR)

                        Text("$year-$month", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        val maxDaily = summaryMap.values.maxOrNull() ?: 1L
                        (1..daysInMonth).forEach { day ->
                            val dateStr = "$year-$month-$day"
                            val usage = summaryMap[dateStr] ?: 0L
                            val fraction = if (maxDaily > 0) usage.toFloat() / maxDaily else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (day == currentDay) AccentBlue else TextTertiary,
                                    modifier = Modifier.width(30.dp)
                                )
                                GlassProgressBar(
                                    progress = fraction,
                                    modifier = Modifier.weight(1f),
                                    trackHeight = 6.dp
                                )
                                Text(
                                    viewModel.formatBytes(usage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.width(70.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
