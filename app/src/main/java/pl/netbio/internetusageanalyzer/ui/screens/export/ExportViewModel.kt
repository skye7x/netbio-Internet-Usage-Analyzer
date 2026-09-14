package pl.netbio.internetusageanalyzer.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.repository.SpeedTestRepository
import pl.netbio.internetusageanalyzer.data.repository.UsageRepository
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import pl.netbio.internetusageanalyzer.util.DataExporter
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val speedTestRepository: SpeedTestRepository,
    private val dataExporter: DataExporter,
    private val dataUsageMonitor: DataUsageMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun exportUsageCsv() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val usages = mutableListOf<DataExporter.ExportUsageData>()
                usageRepository.getAllUsage().first().forEach { entity ->
                    usages.add(DataExporter.ExportUsageData(
                        date = entity.date, timestamp = entity.timestamp,
                        totalBytes = entity.totalBytes, wifiBytes = entity.wifiBytes,
                        mobileBytes = entity.mobileBytes, rxBytes = entity.rxBytes,
                        txBytes = entity.txBytes, packageName = entity.packageName,
                        networkType = entity.networkType
                    ))
                }
                val file = dataExporter.exportUsageToCsv(usages)
                _uiState.value = _uiState.value.copy(isExporting = false, exportedFile = file, exportFormat = "CSV")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
            }
        }
    }

    fun exportUsageJson() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val usages = mutableListOf<DataExporter.ExportUsageData>()
                usageRepository.getAllUsage().first().forEach { entity ->
                    usages.add(DataExporter.ExportUsageData(
                        date = entity.date, timestamp = entity.timestamp,
                        totalBytes = entity.totalBytes, wifiBytes = entity.wifiBytes,
                        mobileBytes = entity.mobileBytes, rxBytes = entity.rxBytes,
                        txBytes = entity.txBytes, packageName = entity.packageName,
                        networkType = entity.networkType
                    ))
                }
                val file = dataExporter.exportUsageToJson(usages)
                _uiState.value = _uiState.value.copy(isExporting = false, exportedFile = file, exportFormat = "JSON")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
            }
        }
    }

    fun exportSpeedTestsCsv() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val tests = speedTestRepository.getAllTests().first().map {
                    DataExporter.ExportSpeedTestData(
                        date = "", timestamp = it.timestamp,
                        downloadSpeed = it.downloadSpeed, uploadSpeed = it.uploadSpeed,
                        ping = it.ping, jitter = it.jitter, packetLoss = it.packetLoss,
                        serverName = it.serverName
                    )
                }
                val file = dataExporter.exportSpeedTestsToCsv(tests)
                _uiState.value = _uiState.value.copy(isExporting = false, exportedFile = file, exportFormat = "CSV")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false, error = e.message)
            }
        }
    }

    fun shareFile() {
        _uiState.value.exportedFile?.let { dataExporter.shareFile(it) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class ExportUiState(
    val isExporting: Boolean = false, val exportedFile: File? = null,
    val exportFormat: String = "", val error: String? = null
)
