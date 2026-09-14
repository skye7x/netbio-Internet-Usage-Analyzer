package com.bzygordev.netbio.ui.screens.alerts

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
import java.text.SimpleDateFormat
import java.util.Date
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
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Alerts", "Limits")
    val scrollState = rememberScrollState()
    var showAddLimitSheet by remember { mutableStateOf(false) }

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
                Text("Alerts & Limits", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.unreadCount > 0) {
                    GlassChip(text = "Mark All Read", onClick = { viewModel.markAllAsRead() })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassTabBar(items = tabs, selectedIndex = selectedTab, onSelect = { selectedTab = it })

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    if (uiState.alerts.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No alerts", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Text("Alerts will appear when limits are reached", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    } else {
                        uiState.alerts.forEach { alert ->
                            val alertType = when {
                                alert.type.contains("EXCEED", ignoreCase = true) || alert.type.contains("exceeded", ignoreCase = true) -> GlassAlertType.Error
                                alert.type.contains("WARN", ignoreCase = true) -> GlassAlertType.Warning
                                alert.type.contains("ANOMALY", ignoreCase = true) -> GlassAlertType.Warning
                                else -> GlassAlertType.Info
                            }
                            val alertIcon = when (alertType) {
                                GlassAlertType.Error -> Icons.Default.Error
                                GlassAlertType.Warning -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            }

                            GlassAlert(
                                title = alert.title,
                                message = alert.message,
                                type = alertType,
                                icon = alertIcon,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(alert.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                                if (!alert.isRead) {
                                    GlassChip(
                                        text = "Read",
                                        onClick = { viewModel.markAsRead(alert.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    GlassButton(
                        text = "Add Limit",
                        onClick = { showAddLimitSheet = true },
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.limits.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                Icon(Icons.Default.DataUsage, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No limits set", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Text("Add limits to track your usage", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    } else {
                        uiState.limits.forEach { limit ->
                            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            limit.type.replaceFirstChar { it.uppercase() } + " Limit",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = TextPrimary
                                        )
                                        Text(
                                            formatBytes(limit.limitBytes),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = AccentBlue
                                        )
                                        Text(
                                            "Alert at ${limit.alertThreshold}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextTertiary
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GlassToggle(
                                            checked = limit.isEnabled,
                                            onCheckedChange = { viewModel.updateLimit(limit.copy(isEnabled = it)) }
                                        )
                                        IconButton(onClick = { viewModel.deleteLimit(limit.id) }) {
                                            Icon(Icons.Default.Delete, "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    GlassBottomSheet(
        visible = showAddLimitSheet,
        onDismiss = { showAddLimitSheet = false }
    ) {
        AddLimitContent(
            onAdd = { type, limitBytes, threshold ->
                viewModel.addLimit(type, limitBytes, threshold)
                showAddLimitSheet = false
            },
            onDismiss = { showAddLimitSheet = false }
        )
    }
}

@Composable
private fun AddLimitContent(
    onAdd: (String, Long, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableIntStateOf(0) }
    var limitValue by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf(80f) }

    val types = listOf("daily", "weekly", "monthly")

    Column {
        Text("Add Usage Limit", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)

        Spacer(modifier = Modifier.height(16.dp))

        GlassTabBar(
            items = listOf("Daily", "Weekly", "Monthly"),
            selectedIndex = selectedType,
            onSelect = { selectedType = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        GlassInput(
            value = limitValue,
            onValueChange = { limitValue = it },
            placeholder = "Limit in GB",
            leadingIcon = Icons.Default.DataUsage
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Alert Threshold: ${String.format("%.0f", threshold)}%", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

        Slider(
            value = threshold,
            onValueChange = { threshold = it },
            valueRange = 50f..99f,
            colors = SliderDefaults.colors(
                thumbColor = AccentBlue,
                activeTrackColor = AccentBlue,
                inactiveTrackColor = GlassHigh
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassButton(
                text = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                color = GlassHigh
            )
            GlassButton(
                text = "Add Limit",
                onClick = {
                    val limitBytes = ((limitValue.toDoubleOrNull() ?: 0.0) * 1024 * 1024 * 1024).toLong()
                    onAdd(types[selectedType], limitBytes, threshold)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
