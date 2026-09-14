package pl.netbio.internetusageanalyzer.ui.screens.alerts

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
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Alerts", "Limits")
    val scrollState = rememberScrollState()
    var showAddLimitDialog by remember { mutableStateOf(false) }

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
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No alerts", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Text("Alerts will appear when limits are reached", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    } else {
                        uiState.alerts.forEach { alert ->
                            GlassAlert(
                                title = alert.title,
                                message = alert.message,
                                type = when (alert.severity) {
                                    "critical" -> GlassAlertType.Error
                                    "warning" -> GlassAlertType.Warning
                                    else -> GlassAlertType.Info
                                },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
                1 -> {
                    GlassButton(
                        text = "Add Limit",
                        onClick = { showAddLimitDialog = true },
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.limits.forEach { limit ->
                        GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        limit.type.replaceFirstChar { it.uppercase() } + " Limit",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        viewModel.formatBytes(limit.limitBytes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AccentBlue
                                    )
                                    Text(
                                        "Warning: ${String.format("%.0f", limit.warningPercentage)}% | Alert: ${String.format("%.0f", limit.alertPercentage)}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextTertiary
                                    )
                                }
                                GlassToggle(
                                    checked = limit.isEnabled,
                                    onCheckedChange = { viewModel.updateLimit(limit.copy(isEnabled = it)) }
                                )
                            }
                        }
                    }

                    if (uiState.limits.isEmpty()) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("No limits set", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                                Text("Add limits to track your usage", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddLimitDialog) {
        AddLimitDialog(
            onDismiss = { showAddLimitDialog = false },
            onAdd = { type, limit, warn, alert ->
                viewModel.addLimit(type, limit, warn, alert)
                showAddLimitDialog = false
            },
            formatBytes = viewModel::formatBytes
        )
    }
}

@Composable
fun AddLimitDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Long, Float, Float) -> Unit,
    formatBytes: (Long) -> String
) {
    var selectedType by remember { mutableIntStateOf(0) }
    var limitValue by remember { mutableStateOf("") }
    var warningPct by remember { mutableStateOf("80") }
    var alertPct by remember { mutableStateOf("95") }

    val types = listOf("daily", "weekly", "monthly")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("Add Usage Limit", fontWeight = FontWeight.Bold) },
        text = {
            Column {
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
                Spacer(modifier = Modifier.height(8.dp))
                GlassInput(
                    value = warningPct,
                    onValueChange = { warningPct = it },
                    placeholder = "Warning at %",
                    leadingIcon = Icons.Default.Warning
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlassInput(
                    value = alertPct,
                    onValueChange = { alertPct = it },
                    placeholder = "Alert at %",
                    leadingIcon = Icons.Default.Error
                )
            }
        },
        confirmButton = {
            GlassButton(
                text = "Add",
                onClick = {
                    val limitBytes = (limitValue.toDoubleOrNull() ?: 0.0) * 1024 * 1024 * 1024
                    val warn = warningPct.toFloatOrNull() ?: 80f
                    val alert = alertPct.toFloatOrNull() ?: 95f
                    onAdd(types[selectedType], limitBytes.toLong(), warn, alert)
                }
            )
        },
        dismissButton = {
            GlassButton(text = "Cancel", onClick = onDismiss)
        }
    )
}
