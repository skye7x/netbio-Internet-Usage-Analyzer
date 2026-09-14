package pl.netbio.internetusageanalyzer.ui.screens.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.netbio.internetusageanalyzer.data.repository.DataUsageRepository
import pl.netbio.internetusageanalyzer.util.DataExporter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ExportFormat {
    CSV, JSON
}

data class ExportUiState(
    val isExporting: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.CSV,
    val exportDateRange: Pair<String, String> = Pair("", ""),
    val lastExportPath: String = "",
    val exportedFileUri: Uri? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataExporter: DataExporter,
    private val dataUsageRepository: DataUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    init {
        val today = dateFormat.format(Date())
        _uiState.update { it.copy(exportDateRange = Pair(today, today)) }
    }

    fun setFormat(format: ExportFormat) {
        _uiState.update { it.copy(exportFormat = format) }
    }

    fun setDateRange(start: String, end: String) {
        _uiState.update { it.copy(exportDateRange = Pair(start, end)) }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val state = _uiState.value
                val startDate = state.exportDateRange.first
                val endDate = state.exportDateRange.second
                val format = state.exportFormat

                val file = withContext(Dispatchers.IO) {
                    val fileName = "netbio_export_${fileDateFormat.format(Date())}.${format.name.lowercase()}"
                    val usageData = dataUsageRepository.getUsageByDateRange(startDate, endDate).first()
                    val exportDir = File(context.filesDir, "exports")
                    if (!exportDir.exists()) {
                        exportDir.mkdirs()
                    }

                    val exportData = listOf(
                        DataExporter.ExportUsageData(
                            date = startDate,
                            timestamp = System.currentTimeMillis(),
                            totalBytes = usageData,
                            wifiBytes = 0L,
                            mobileBytes = 0L,
                            rxBytes = 0L,
                            txBytes = 0L,
                            packageName = "",
                            networkType = ""
                        )
                    )

                    when (format) {
                        ExportFormat.CSV -> {
                            dataExporter.exportToCsv(exportData, fileName)
                        }
                        ExportFormat.JSON -> {
                            dataExporter.exportToJson(exportData, fileName)
                        }
                    }
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportPath = file.absolutePath,
                        exportedFileUri = uri
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isExporting = false) }
            }
        }
    }

    fun shareExport() {
        viewModelScope.launch {
            val uri = _uiState.value.exportedFileUri
            if (uri != null) {
                try {
                    val format = _uiState.value.exportFormat
                    val mimeType = when (format) {
                        ExportFormat.CSV -> "text/csv"
                        ExportFormat.JSON -> "application/json"
                    }

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "NetBio Internet Usage Export")
                        putExtra(Intent.EXTRA_TEXT, "Here is your internet usage data export from NetBio.")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooserIntent = Intent.createChooser(shareIntent, "Share Export")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooserIntent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
