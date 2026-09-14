package pl.netbio.internetusageanalyzer.ui.screens.export

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Export Data", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Export Format")
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassButton(
                        text = "CSV",
                        onClick = { viewModel.setFormat(ExportFormat.CSV) },
                        icon = Icons.Default.TableChart,
                        modifier = Modifier.weight(1f),
                        color = if (uiState.exportFormat == ExportFormat.CSV) AccentBlue else GlassHigh
                    )
                    GlassButton(
                        text = "JSON",
                        onClick = { viewModel.setFormat(ExportFormat.JSON) },
                        icon = Icons.Default.Code,
                        modifier = Modifier.weight(1f),
                        color = if (uiState.exportFormat == ExportFormat.JSON) AccentBlue else GlassHigh
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                GlassSectionHeader(title = "Date Range")
                Spacer(modifier = Modifier.height(8.dp))
                GlassMetricRow(label = "Start", value = uiState.exportDateRange.first.ifEmpty { "Not set" }, icon = Icons.Default.CalendarToday)
                HorizontalDivider(color = GlassHigh)
                GlassMetricRow(label = "End", value = uiState.exportDateRange.second.ifEmpty { "Not set" }, icon = Icons.Default.CalendarToday)
                HorizontalDivider(color = GlassHigh)
                GlassMetricRow(label = "Format", value = uiState.exportFormat.name, icon = Icons.Default.Description)
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(
                text = "Export Data",
                onClick = { viewModel.exportData() },
                icon = Icons.Default.FileDownload,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isExporting
            )

            if (uiState.isExporting) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Exporting...", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            if (uiState.lastExportPath.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    GlassSectionHeader(title = "Export Ready")
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassMetricRow(label = "File", value = uiState.lastExportPath.substringAfterLast("/"), icon = Icons.Default.Description)
                    GlassMetricRow(label = "Format", value = uiState.exportFormat.name, icon = Icons.Default.TableChart)
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassButton(
                        text = "Share File",
                        onClick = { viewModel.shareExport() },
                        icon = Icons.Default.Share,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
