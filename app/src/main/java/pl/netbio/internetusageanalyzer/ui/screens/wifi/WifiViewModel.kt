package pl.netbio.internetusageanalyzer.ui.screens.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.local.entity.NetworkInfoEntity
import pl.netbio.internetusageanalyzer.data.repository.NetworkRepository
import pl.netbio.internetusageanalyzer.service.NetworkMonitor
import pl.netbio.internetusageanalyzer.service.WifiInfoData
import javax.inject.Inject

@HiltViewModel
class WifiViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val networkRepository: NetworkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    val isConnected = networkMonitor.isConnected
    val networkType = networkMonitor.networkType
    val wifiInfo = networkMonitor.wifiInfo
    val signalStrength = networkMonitor.signalStrength
    val linkSpeed = networkMonitor.linkSpeed
    val frequency = networkMonitor.frequency

    init {
        viewModelScope.launch {
            combine(networkMonitor.wifiInfo, networkMonitor.signalStrength, networkMonitor.linkSpeed, networkMonitor.frequency) { info, signal, speed, freq ->
                WifiUiState(
                    wifiInfo = info,
                    signalStrength = signal,
                    linkSpeed = speed,
                    frequency = freq,
                    frequencyBand = networkMonitor.getFrequencyBand(freq),
                    signalDescription = networkMonitor.getSignalLevelDescription(-(100 - signal))
                )
            }.collect { state -> _uiState.value = state }
        }
        viewModelScope.launch {
            networkRepository.getLatestNetworkInfo().collect { info ->
                _uiState.value = _uiState.value.copy(lastSavedInfo = info)
            }
        }
    }

    fun saveNetworkInfo() {
        viewModelScope.launch {
            val info = networkMonitor.wifiInfo.value ?: return@launch
            networkRepository.insertNetworkInfo(
                NetworkInfoEntity(
                    networkName = info.ssid, networkType = "wifi", ssid = info.ssid,
                    bssid = info.bssid, frequency = info.frequency, linkSpeed = info.linkSpeed,
                    rssi = info.rssi, ipAddress = info.ipAddress, channel = info.channel,
                    isSecure = info.isSecure
                )
            )
        }
    }
}

data class WifiUiState(
    val wifiInfo: WifiInfoData? = null, val signalStrength: Int = 0,
    val linkSpeed: Int = 0, val frequency: Int = 0,
    val frequencyBand: String = "", val signalDescription: String = "",
    val lastSavedInfo: NetworkInfoEntity? = null
)
