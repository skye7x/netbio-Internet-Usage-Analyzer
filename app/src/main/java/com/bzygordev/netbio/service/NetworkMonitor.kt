package com.bzygordev.netbio.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkType = MutableStateFlow("none")
    val networkType: StateFlow<String> = _networkType.asStateFlow()

    private val _wifiInfo = MutableStateFlow<WifiInfoData?>(null)
    val wifiInfo: StateFlow<WifiInfoData?> = _wifiInfo.asStateFlow()

    private val _signalStrength = MutableStateFlow(0)
    val signalStrength: StateFlow<Int> = _signalStrength.asStateFlow()

    private val _linkSpeed = MutableStateFlow(0)
    val linkSpeed: StateFlow<Int> = _linkSpeed.asStateFlow()

    private val _frequency = MutableStateFlow(0)
    val frequency: StateFlow<Int> = _frequency.asStateFlow()

    private val _connectionLost = MutableStateFlow(false)
    val connectionLost: StateFlow<Boolean> = _connectionLost.asStateFlow()

    private val _reconnectEvent = MutableStateFlow(0L)
    val reconnectEvent: StateFlow<Long> = _reconnectEvent.asStateFlow()

    private var lastConnected = false
    private var isRegistered = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            _connectionLost.value = false
            if (lastConnected.not()) {
                _reconnectEvent.value = System.currentTimeMillis()
            }
            lastConnected = true
            updateNetworkInfo()
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkType.value = "none"
            _wifiInfo.value = null
            _signalStrength.value = 0
            _linkSpeed.value = 0
            _frequency.value = 0
            _connectionLost.value = true
            lastConnected = false
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            updateNetworkInfo()
        }
    }

    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        if (!isRegistered) {
            try {
                connectivityManager.registerNetworkCallback(request, networkCallback)
                isRegistered = true
            } catch (_: Exception) {
            }
        }
        updateNetworkInfo()
    }

    fun stopMonitoring() {
        if (isRegistered) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
                isRegistered = false
            } catch (_: Exception) {
            }
        }
    }

    fun updateNetworkInfo() {
        val activeNetwork = connectivityManager.activeNetwork ?: run {
            _isConnected.value = false
            _networkType.value = "none"
            return
        }
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: run {
            _isConnected.value = false
            _networkType.value = "none"
            return
        }

        _isConnected.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                _networkType.value = "wifi"
                updateWifiInfo()
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                _networkType.value = "mobile"
                _wifiInfo.value = null
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                _networkType.value = "ethernet"
                _wifiInfo.value = null
            }
            else -> {
                _networkType.value = "other"
                _wifiInfo.value = null
            }
        }
    }

    @Suppress("DEPRECATION")
    fun updateWifiInfo() {
        if (_networkType.value != "wifi") return
        val wifiInfo = wifiManager.connectionInfo ?: return
        val rssi = wifiInfo.rssi
        val freq = wifiInfo.frequency
        val speed = wifiInfo.linkSpeed

        _signalStrength.value = WifiManager.calculateSignalLevel(rssi, 101).coerceIn(0, 100)
        _linkSpeed.value = speed
        _frequency.value = freq

        _wifiInfo.value = WifiInfoData(
            ssid = wifiInfo.ssid?.replace("\"", "") ?: "Unknown",
            bssid = wifiInfo.bssid ?: "",
            rssi = rssi,
            frequency = freq,
            linkSpeed = speed,
            channel = frequencyToChannel(freq),
            isSecure = wifiInfo.ssid != "<unknown ssid>" && wifiInfo.ssid?.isNotEmpty() == true,
            ipAddress = intToIp(wifiInfo.ipAddress)
        )
    }

    fun frequencyToChannel(freq: Int): Int = when {
        freq in 2412..2484 -> (freq - 2407) / 5
        freq in 5170..5825 -> (freq - 5000) / 5
        freq in 5955..7115 -> (freq - 5950) / 5
        else -> 0
    }

    fun intToIp(ip: Int): String {
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    fun getSignalDescription(rssi: Int): String = when {
        rssi >= -50 -> "Excellent"
        rssi >= -60 -> "Good"
        rssi >= -70 -> "Fair"
        rssi >= -80 -> "Weak"
        else -> "Very Weak"
    }

    fun getFrequencyBand(freq: Int): String = when {
        freq in 2400..2500 -> "2.4 GHz"
        freq in 5150..5850 -> "5 GHz"
        freq in 5925..7125 -> "6 GHz"
        else -> "Unknown"
    }
}

data class WifiInfoData(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val linkSpeed: Int,
    val channel: Int,
    val isSecure: Boolean,
    val ipAddress: String
)
