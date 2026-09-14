package pl.netbio.internetusageanalyzer.ui.screens.export

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
fun ExportScreen(
    viewModel: ExportViewModel = hiltViewModel(),
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
                Text("Export Data", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            uiState.error?.let { error ->
                GlassAlert(title = "Error", message = error, type = GlassAlertType.Error, onDismiss = { viewModel.clearError() })
                Spacer(modifier = Modifier.height(12.dp))
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Usage Data")
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassButton(
                        text = "CSV",
                        onClick = { viewModel.exportUsageCsv() },
                        icon = Icons.Default.TableChart,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isExporting
                    )
                    GlassButton(
                        text = "JSON",
                        onClick = { viewModel.exportUsageJson() },
                        icon = Icons.Default.Code,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isExporting
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Speed Test Data")
                Spacer(modifier = Modifier.height(12.dp))

                GlassButton(
                    text = "Export Speed Tests (CSV)",
                    onClick = { viewModel.exportSpeedTestsCsv() },
                    icon = Icons.Default.Speed,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isExporting
                )
            }

            if (uiState.exportedFile != null) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Export Ready", subtitle = uiState.exportedFile!!.name)
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(
                        text = "Share File",
                        onClick = { viewModel.shareFile() },
                        icon = Icons.Default.Share,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.isExporting) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Exporting...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
