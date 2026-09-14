package com.bzygordev.netbio.ui.screens.history

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
import java.util.Locale

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        .coerceIn(0, units.size - 1)
    return String.format(
        Locale.US,
        "%.1f %s",
        bytes / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Day", "Week", "Month", "Year")
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
                Text("History", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassTabBar(
                items = tabs,
                selectedIndex = selectedTab,
                onSelect = {
                    selectedTab = it
                    viewModel.setTab(it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    val totalToday = uiState.dailyUsage.sumOf { it.second }
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Today's Usage")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            formatBytes(totalToday),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.hourlyUsage.isNotEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            GlassSectionHeader(title = "Hourly Breakdown")
                            Spacer(modifier = Modifier.height(8.dp))
                            val maxUsage = uiState.hourlyUsage.maxOfOrNull { it.second } ?: 1L
                            (0..23).forEach { hour ->
                                val hourData = uiState.hourlyUsage.find { it.first == hour }
                                val usage = hourData?.second ?: 0L
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
                                        progress = if (maxUsage > 0) (usage.toFloat() / maxUsage).coerceIn(0f, 1f) else 0f,
                                        modifier = Modifier.weight(1f),
                                        trackHeight = 8.dp
                                    )
                                    Text(
                                        formatBytes(usage),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.width(70.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val weekData = uiState.weeklyMonthlyYearlyUsage
                    val totalWeek = weekData.values.sum()
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Weekly Usage")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            formatBytes(totalWeek),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentPurple
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Daily Breakdown")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (weekData.isEmpty()) {
                            Text("No data available", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        } else {
                            val maxDay = weekData.values.maxOrNull() ?: 1L
                            UsageBarChart(
                                data = weekData.entries.map { entry ->
                                    entry.key.takeLast(5) to (entry.value / (1024.0 * 1024)).toFloat()
                                },
                                maxValue = (maxDay / (1024.0 * 1024)).toFloat(),
                                barColor = AccentPurple
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Days")
                        Spacer(modifier = Modifier.height(8.dp))
                        weekData.entries.forEach { (date, total) ->
                            GlassMetricRow(
                                label = date,
                                value = formatBytes(total),
                                icon = Icons.Default.CalendarToday,
                                iconTint = AccentPurple
                            )
                        }
                    }
                }
                2 -> {
                    val monthData = uiState.weeklyMonthlyYearlyUsage
                    val totalMonth = monthData.values.sum()
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Monthly Usage")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            formatBytes(totalMonth),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Usage Chart")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (monthData.isEmpty()) {
                            Text("No data available", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        } else {
                            val maxDay = monthData.values.maxOrNull() ?: 1L
                            UsageBarChart(
                                data = monthData.entries.toList().takeLast(14).map { entry ->
                                    entry.key to (entry.value / (1024.0 * 1024)).toFloat()
                                },
                                maxValue = (maxDay / (1024.0 * 1024)).toFloat(),
                                barColor = AccentCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Daily History")
                        Spacer(modifier = Modifier.height(8.dp))
                        monthData.entries.forEach { (date, total) ->
                            GlassMetricRow(
                                label = date,
                                value = formatBytes(total),
                                icon = Icons.Default.CalendarToday,
                                iconTint = AccentCyan
                            )
                        }
                    }
                }
                3 -> {
                    val yearData = uiState.weeklyMonthlyYearlyUsage
                    val totalYear = yearData.values.sum()
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Yearly Usage")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            formatBytes(totalYear),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = AccentOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        GlassSectionHeader(title = "Monthly Summary")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (yearData.isEmpty()) {
                            Text("No data available", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        } else {
                            val maxMonth = yearData.values.maxOrNull() ?: 1L
                            UsageBarChart(
                                data = yearData.entries.map { (month, total) ->
                                    month to (total / (1024.0 * 1024 * 1024)).toFloat()
                                },
                                maxValue = (maxMonth / (1024.0 * 1024 * 1024)).toFloat(),
                                barColor = AccentOrange
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
