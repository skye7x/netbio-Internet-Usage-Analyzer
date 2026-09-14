package pl.netbio.internetusageanalyzer.ui.screens.appusage

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
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("App Usage", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Total: ${viewModel.formatBytes(uiState.totalUsage)}", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.appUsages.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No app data yet", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        Text("App usage will appear here", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            } else {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "App Ranking", subtitle = "Today's data usage")
                    Spacer(modifier = Modifier.height(12.dp))

                    uiState.appUsages.forEachIndexed { index, app ->
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
                                    app.packageName.substringAfterLast('.'),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    viewModel.formatBytes(app.bytes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                "${String.format("%.1f", app.percentage)}%",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = AccentBlue
                            )
                        }
                        if (index < uiState.appUsages.size - 1) {
                            HorizontalDivider(color = GlassWhite10)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
