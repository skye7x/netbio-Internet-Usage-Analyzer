package com.bzygordev.netbio.ui.screens.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.bzygordev.netbio.data.local.entity.SpeedTestResultEntity
import com.bzygordev.netbio.data.repository.SpeedTestRepository
import com.bzygordev.netbio.service.SpeedTestEngine
import java.util.Date
import javax.inject.Inject

data class SpeedTestUiState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val phase: String = "",
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val ping: Float = 0f,
    val jitter: Float = 0f,
    val packetLoss: Float = 0f,
    val currentDownload: Float = 0f,
    val currentUpload: Float = 0f,
    val recentResults: List<SpeedTestResultEntity> = emptyList(),
    val latestResult: SpeedTestResultEntity? = null
)

@HiltViewModel
class SpeedTestViewModel @Inject constructor(
    private val speedTestRepository: SpeedTestRepository,
    private val speedTestEngine: SpeedTestEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedTestUiState())
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    init {
        collectRecentResults()
        collectEngineState()
    }

    private fun collectRecentResults() {
        viewModelScope.launch {
            speedTestRepository.getAll().catch { e ->
                e.printStackTrace()
            }.collect { results ->
                _uiState.update {
                    it.copy(
                        recentResults = results,
                        latestResult = results.firstOrNull()
                    )
                }
            }
        }
    }

    private fun collectEngineState() {
        viewModelScope.launch {
            speedTestEngine.isRunning.collect { running ->
                _uiState.update { it.copy(isRunning = running) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.progress.collect { progress ->
                _uiState.update { it.copy(progress = progress) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.phase.collect { phase ->
                _uiState.update { it.copy(phase = phase.name) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.downloadSpeed.collect { speed ->
                _uiState.update { it.copy(downloadSpeed = speed.toFloat()) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.uploadSpeed.collect { speed ->
                _uiState.update { it.copy(uploadSpeed = speed.toFloat()) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.ping.collect { ping ->
                _uiState.update { it.copy(ping = ping.toFloat()) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.jitter.collect { jitter ->
                _uiState.update { it.copy(jitter = jitter.toFloat()) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.packetLoss.collect { loss ->
                _uiState.update { it.copy(packetLoss = loss.toFloat()) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.currentDownload.collect { speed ->
                _uiState.update { it.copy(currentDownload = speed.toFloat()) }
            }
        }
        viewModelScope.launch {
            speedTestEngine.currentUpload.collect { speed ->
                _uiState.update { it.copy(currentUpload = speed.toFloat()) }
            }
        }
    }

    fun startTest() {
        viewModelScope.launch {
            speedTestEngine.startTest()
            val state = _uiState.value
            val result = SpeedTestResultEntity(
                timestamp = System.currentTimeMillis(),
                downloadSpeed = state.downloadSpeed.toDouble(),
                uploadSpeed = state.uploadSpeed.toDouble(),
                ping = state.ping.toDouble(),
                jitter = state.jitter.toDouble(),
                packetLoss = state.packetLoss.toDouble()
            )
            speedTestRepository.insert(result)
        }
    }

    fun cancelTest() {
        speedTestEngine.cancel()
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                speedTestRepository.deleteOld(Long.MAX_VALUE)
                speedTestEngine.reset()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
