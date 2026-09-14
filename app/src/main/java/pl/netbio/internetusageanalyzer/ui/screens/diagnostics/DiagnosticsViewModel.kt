package pl.netbio.internetusageanalyzer.ui.screens.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.netbio.internetusageanalyzer.data.local.entity.DiagnosticEntity
import pl.netbio.internetusageanalyzer.data.repository.DiagnosticRepository
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import pl.netbio.internetusageanalyzer.service.DnsTestEngine
import pl.netbio.internetusageanalyzer.service.NetworkMonitor
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val dataUsageMonitor: DataUsageMonitor,
    private val diagnosticRepository: DiagnosticRepository,
    private val dnsTestEngine: DnsTestEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    val isConnected = networkMonitor.isConnected
    val connectionLost = networkMonitor.connectionLost

    init {
        viewModelScope.launch {
            diagnosticRepository.getRecentDiagnostics(50).collect { diagnostics ->
                _uiState.value = _uiState.value.copy(history = diagnostics)
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true)
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://www.google.com/generate_204")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000; conn.readTimeout = 10000
                    val start = System.currentTimeMillis()
                    conn.connect()
                    val latency = System.currentTimeMillis() - start
                    val code = conn.responseCode
                    conn.disconnect()
                    ConnectionTestResult(isConnected = code == 204, latencyMs = latency.toDouble(), statusCode = code)
                } catch (e: Exception) {
                    ConnectionTestResult(isConnected = false, latencyMs = 0.0, statusCode = -1, error = e.message)
                }
            }
            _uiState.value = _uiState.value.copy(isTestingConnection = false, connectionResult = result)
            diagnosticRepository.insertDiagnostic(
                DiagnosticEntity(type = "connection", result = if (result.isConnected) "OK" else "FAILED",
                    latency = result.latencyMs, isStable = result.isConnected,
                    details = result.error ?: "Status: ${result.statusCode}")
            )
        }
    }

    fun testDns() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingDns = true)
            val results = dnsTestEngine.testDns()
            _uiState.value = _uiState.value.copy(isTestingDns = false, dnsResults = results)
            val avgLatency = results.filter { it.isSuccessful }.map { it.latencyMs }.average()
            diagnosticRepository.insertDiagnostic(
                DiagnosticEntity(type = "dns", result = if (results.any { it.isSuccessful }) "OK" else "FAILED",
                    latency = if (avgLatency.isNaN()) 0.0 else avgLatency,
                    isStable = results.any { it.isSuccessful },
                    details = results.joinToString("\n") { "${it.server}: ${it.resolvedIp} (${String.format("%.1f", it.latencyMs)}ms)" })
            )
        }
    }

    fun testLatency() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingLatency = true)
            val results = withContext(Dispatchers.IO) {
                listOf("https://www.google.com" to "Google", "https://www.cloudflare.com" to "Cloudflare",
                    "https://www.amazon.com" to "Amazon").map { (url, name) ->
                    try {
                        val conn = URL(url).openConnection() as HttpURLConnection
                        conn.connectTimeout = 5000; conn.readTimeout = 5000
                        val start = System.currentTimeMillis(); conn.connect()
                        val latency = System.currentTimeMillis() - start; conn.disconnect()
                        LatencyTestResult(name, latency.toDouble(), true)
                    } catch (e: Exception) { LatencyTestResult(name, 0.0, false, e.message) }
                }
            }
            _uiState.value = _uiState.value.copy(isTestingLatency = false, latencyResults = results)
        }
    }

    fun testStability() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingStability = true)
            val pings = withContext(Dispatchers.IO) {
                (1..20).map {
                    try {
                        val conn = URL("https://www.google.com/generate_204").openConnection() as HttpURLConnection
                        conn.connectTimeout = 3000; conn.readTimeout = 3000
                        val start = System.currentTimeMillis(); conn.connect()
                        val latency = System.currentTimeMillis() - start; conn.disconnect()
                        latency.toDouble()
                    } catch (e: Exception) { -1.0 }
                }
            }
            val validPings = pings.filter { it > 0 }
            val avg = if (validPings.isNotEmpty()) validPings.average() else 0.0
            val jitter = if (validPings.size > 1) validPings.map { kotlin.math.abs(it - avg) }.average() else 0.0
            val loss = ((pings.size - validPings.size).toDouble() / pings.size) * 100.0
            val result = StabilityTestResult(avgLatency = avg, jitter = jitter, packetLoss = loss,
                minLatency = validPings.minOrNull() ?: 0.0, maxLatency = validPings.maxOrNull() ?: 0.0,
                totalPings = pings.size, successfulPings = validPings.size,
                isStable = loss < 5.0 && jitter < 10.0)
            _uiState.value = _uiState.value.copy(isTestingStability = false, stabilityResult = result)
        }
    }

    fun getIpAddress(): String = try {
        val url = URL("https://api.ipify.org")
        val conn = url.openConnection() as HttpURLConnection; conn.connectTimeout = 5000
        conn.connect(); val ip = conn.inputStream.bufferedReader().readText(); conn.disconnect(); ip
    } catch (e: Exception) { "N/A" }
}

data class DiagnosticsUiState(
    val isTestingConnection: Boolean = false, val isTestingDns: Boolean = false,
    val isTestingLatency: Boolean = false, val isTestingStability: Boolean = false,
    val connectionResult: ConnectionTestResult? = null,
    val dnsResults: List<DnsTestEngine.DnsResult> = emptyList(),
    val latencyResults: List<LatencyTestResult> = emptyList(),
    val stabilityResult: StabilityTestResult? = null,
    val history: List<DiagnosticEntity> = emptyList()
)

data class ConnectionTestResult(val isConnected: Boolean, val latencyMs: Double, val statusCode: Int, val error: String? = null)
data class LatencyTestResult(val target: String, val latencyMs: Double, val isSuccessful: Boolean, val error: String? = null)
data class StabilityTestResult(val avgLatency: Double, val jitter: Double, val packetLoss: Double,
    val minLatency: Double, val maxLatency: Double, val totalPings: Int, val successfulPings: Int, val isStable: Boolean)
