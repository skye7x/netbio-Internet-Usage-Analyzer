package com.bzygordev.netbio.ui.screens.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.bzygordev.netbio.data.local.entity.NetworkInfoEntity
import com.bzygordev.netbio.data.repository.NetworkInfoRepository
import com.bzygordev.netbio.service.NetworkMonitor
import javax.inject.Inject

data class WifiInfoData(
    val ssid: String,
    val bssid: String,
    val ipAddress: String,
    val linkSpeed: Int,
    val frequency: Int,
    val rssi: Int
)

data class WifiUiState(
    val isConnected: Boolean = false,
    val networkType: String = "None",
    val wifiInfo: WifiInfoData? = null,
    val signalStrength: Int = 0,
    val linkSpeed: Int = 0,
    val frequency: Int = 0,
    val connectionQuality: String = "Unknown",
    val networkHistory: List<NetworkInfoEntity> = emptyList(),
    val distinctNetworks: List<String> = emptyList()
)

@HiltViewModel
class WifiViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val networkInfoRepository: NetworkInfoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    init {
        collectNetworkState()
        collectNetworkHistory()
    }

    private fun collectNetworkState() {
        viewModelScope.launch {
            combine(
                networkMonitor.isConnected,
                networkMonitor.networkType,
                networkMonitor.wifiInfo,
                networkMonitor.signalStrength,
                networkMonitor.linkSpeed,
                networkMonitor.frequency
            ) { values ->
                val connected = values[0] as Boolean
                val type = values[1] as String
                val wifi = values[2] as? com.bzygordev.netbio.service.WifiInfoData
                val signal = values[3] as Int
                val link = values[4] as Int
                val freq = values[5] as Int
                WifiStateBundle(connected, type, wifi, signal, link, freq)
            }.catch { e ->
                e.printStackTrace()
            }.collect { bundle ->
                val quality = calculateQuality(bundle.signalStrength)
                val wifiData = bundle.wifiInfo?.let {
                    WifiInfoData(
                        ssid = it.ssid,
                        bssid = it.bssid,
                        ipAddress = it.ipAddress,
                        linkSpeed = it.linkSpeed,
                        frequency = it.frequency,
                        rssi = it.rssi
                    )
                }

                _uiState.update {
                    it.copy(
                        isConnected = bundle.isConnected,
                        networkType = bundle.networkType,
                        wifiInfo = wifiData,
                        signalStrength = bundle.signalStrength,
                        linkSpeed = bundle.linkSpeed,
                        frequency = bundle.frequency,
                        connectionQuality = quality
                    )
                }
            }
        }
    }

    private fun collectNetworkHistory() {
        viewModelScope.launch {
            networkInfoRepository.getAll().catch { e ->
                e.printStackTrace()
            }.collect { history ->
                val networks = history.map { it.ssid }.distinct()
                _uiState.update {
                    it.copy(
                        networkHistory = history,
                        distinctNetworks = networks
                    )
                }
            }
        }
    }

    fun calculateQuality(rssi: Int): String {
        return when {
            rssi >= -50 -> "Excellent"
            rssi >= -65 -> "Good"
            rssi >= -75 -> "Fair"
            else -> "Poor"
        }
    }

    fun getNetworkStats(ssid: String) {
        viewModelScope.launch {
            try {
                val stats = networkInfoRepository.getBySsid(ssid).first()
                _uiState.update {
                    it.copy(
                        networkHistory = stats
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private data class WifiStateBundle(
        val isConnected: Boolean,
        val networkType: String,
        val wifiInfo: com.bzygordev.netbio.service.WifiInfoData?,
        val signalStrength: Int,
        val linkSpeed: Int,
        val frequency: Int
    )
}
