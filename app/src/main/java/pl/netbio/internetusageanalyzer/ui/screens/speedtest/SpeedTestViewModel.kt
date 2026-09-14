package pl.netbio.internetusageanalyzer.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.local.entity.SpeedTestEntity
import pl.netbio.internetusageanalyzer.data.repository.SpeedTestRepository
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import pl.netbio.internetusageanalyzer.service.NotificationHelper
import pl.netbio.internetusageanalyzer.service.SpeedTestEngine
import pl.netbio.internetusageanalyzer.service.SpeedTestResult
import javax.inject.Inject

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val speedTestEngine: SpeedTestEngine,
    private val speedTestRepository: SpeedTestRepository,
    private val notificationHelper: NotificationHelper,
    private val dataUsageMonitor: DataUsageMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    val isRunning = speedTestEngine.isRunning
    val progress = speedTestEngine.progress
    val phase = speedTestEngine.phase
    val downloadSpeed = speedTestEngine.currentDownload
    val uploadSpeed = speedTestEngine.currentUpload
    val ping = speedTestEngine.ping

    init { loadHistory() }

    private fun loadHistory() {
        viewModelScope.launch {
            speedTestRepository.getRecentTests(20).collect { tests ->
                _uiState.value = _uiState.value.copy(history = tests)
            }
        }
        viewModelScope.launch {
            speedTestRepository.getMaxDownload().collect { max ->
                _uiState.value = _uiState.value.copy(maxDownload = max ?: 0.0)
            }
        }
        viewModelScope.launch {
            speedTestRepository.getAverageDownload(0, System.currentTimeMillis()).collect { avg ->
                _uiState.value = _uiState.value.copy(avgDownload = avg ?: 0.0)
            }
        }
    }

    fun startTest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true, currentResult = null)
            val result = speedTestEngine.startTest()
            _uiState.value = _uiState.value.copy(isTesting = false, currentResult = result)
            if (result.downloadSpeed > 0) {
                val entity = SpeedTestEntity(
                    timestamp = System.currentTimeMillis(),
                    downloadSpeed = result.downloadSpeed,
                    uploadSpeed = result.uploadSpeed,
                    ping = result.ping,
                    jitter = result.jitter,
                    packetLoss = result.packetLoss
                )
                speedTestRepository.insertTest(entity)
                notificationHelper.showSpeedTestResult(
                    dataUsageMonitor.formatSpeedMbps(result.downloadSpeed),
                    dataUsageMonitor.formatSpeedMbps(result.uploadSpeed),
                    String.format("%.1f ms", result.ping)
                )
            }
        }
    }

    fun cancelTest() {
        speedTestEngine.cancel()
        _uiState.value = _uiState.value.copy(isTesting = false)
    }

    fun formatSpeed(bytesPerSecond: Double): String = dataUsageMonitor.formatSpeedMbps(bytesPerSecond)
}

data class SpeedTestUiState(
    val isTesting: Boolean = false,
    val currentResult: SpeedTestResult? = null,
    val history: List<SpeedTestEntity> = emptyList(),
    val maxDownload: Double = 0.0,
    val avgDownload: Double = 0.0
)
