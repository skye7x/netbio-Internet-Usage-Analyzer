package com.bzygordev.netbio.ui.screens.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ConnectivityResult(
    val isReachable: Boolean,
    val ipAddress: String,
    val hostname: String,
    val latencyMs: Long,
    val interfaceName: String
)

data class DnsResult(
    val domain: String,
    val resolvedIp: String,
    val resolutionTimeMs: Long,
    val isSuccessful: Boolean
)

data class LatencyResult(
    val target: String,
    val latencyMs: Long,
    val isReachable: Boolean
)

data class StabilityResult(
    val totalPings: Int,
    val successfulPings: Int,
    val failedPings: Int,
    val averageLatencyMs: Long,
    val jitterMs: Float,
    val packetLossPercent: Float,
    val minLatencyMs: Long,
    val maxLatencyMs: Long
)

data class DiagnosticsUiState(
    val connectionResult: ConnectivityResult? = null,
    val dnsResults: List<DnsResult> = emptyList(),
    val latencyResults: List<LatencyResult> = emptyList(),
    val stabilityResult: StabilityResult? = null,
    val isRunning: Boolean = false
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    val startTime = System.currentTimeMillis()
                    val reachable = InetAddress.getByName("google.com").isReachable(5000)
                    val endTime = System.currentTimeMillis()
                    val latency = endTime - startTime

                    val ip = InetAddress.getLocalHost().hostAddress ?: "Unknown"
                    val hostname = InetAddress.getLocalHost().hostName ?: "Unknown"

                    val interfaceName = try {
                        NetworkInterface.getNetworkInterfaces().asSequence()
                            .firstOrNull { it.isUp && !it.isLoopback && it.interfaceAddresses.any { addr -> addr.address is java.net.Inet4Address } }
                            ?.displayName ?: "Unknown"
                    } catch (e: Exception) {
                        "Unknown"
                    }

                    ConnectivityResult(
                        isReachable = reachable,
                        ipAddress = ip,
                        hostname = hostname,
                        latencyMs = latency,
                        interfaceName = interfaceName
                    )
                }
                _uiState.update {
                    it.copy(
                        connectionResult = result,
                        isRunning = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        connectionResult = ConnectivityResult(
                            isReachable = false,
                            ipAddress = "N/A",
                            hostname = "N/A",
                            latencyMs = 0,
                            interfaceName = "N/A"
                        ),
                        isRunning = false
                    )
                }
            }
        }
    }

    fun testDns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }
            try {
                val domains = listOf("google.com", "cloudflare.com", "dns.google", "opendns.com", "quad9.net")
                val results = withContext(Dispatchers.IO) {
                    domains.map { domain ->
                        try {
                            val startTime = System.currentTimeMillis()
                            val address = InetAddress.getByName(domain)
                            val endTime = System.currentTimeMillis()
                            DnsResult(
                                domain = domain,
                                resolvedIp = address.hostAddress ?: "N/A",
                                resolutionTimeMs = endTime - startTime,
                                isSuccessful = true
                            )
                        } catch (e: Exception) {
                            DnsResult(
                                domain = domain,
                                resolvedIp = "Failed",
                                resolutionTimeMs = 0,
                                isSuccessful = false
                            )
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        dnsResults = results,
                        isRunning = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    fun testLatency() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }
            try {
                val targets = listOf("8.8.8.8", "1.1.1.1", "208.67.222.222", "9.9.9.9", "8.8.4.4")
                val results = withContext(Dispatchers.IO) {
                    targets.map { target ->
                        try {
                            val startTime = System.currentTimeMillis()
                            val reachable = InetAddress.getByName(target).isReachable(3000)
                            val endTime = System.currentTimeMillis()
                            LatencyResult(
                                target = target,
                                latencyMs = endTime - startTime,
                                isReachable = reachable
                            )
                        } catch (e: Exception) {
                            LatencyResult(
                                target = target,
                                latencyMs = 0,
                                isReachable = false
                            )
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        latencyResults = results,
                        isRunning = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    fun testStability() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    val target = "8.8.8.8"
                    val pingCount = 20
                    val latencies = mutableListOf<Long>()

                    repeat(pingCount) {
                        try {
                            val startTime = System.currentTimeMillis()
                            val reachable = InetAddress.getByName(target).isReachable(3000)
                            val endTime = System.currentTimeMillis()
                            if (reachable) {
                                latencies.add(endTime - startTime)
                            }
                        } catch (e: Exception) {
                            // Skip failed pings
                        }
                    }

                    val successful = latencies.size
                    val failed = pingCount - successful
                    val avg = if (successful > 0) latencies.average().toLong() else 0L
                    val jitter = if (successful > 1) {
                        val squaredDiffs = latencies.map { (it - avg) * (it - avg) }
                        Math.sqrt(squaredDiffs.average()).toFloat()
                    } else 0f

                    StabilityResult(
                        totalPings = pingCount,
                        successfulPings = successful,
                        failedPings = failed,
                        averageLatencyMs = avg,
                        jitterMs = jitter,
                        packetLossPercent = (failed.toFloat() / pingCount.toFloat()) * 100f,
                        minLatencyMs = latencies.minOrNull() ?: 0L,
                        maxLatencyMs = latencies.maxOrNull() ?: 0L
                    )
                }
                _uiState.update {
                    it.copy(
                        stabilityResult = result,
                        isRunning = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }
}
