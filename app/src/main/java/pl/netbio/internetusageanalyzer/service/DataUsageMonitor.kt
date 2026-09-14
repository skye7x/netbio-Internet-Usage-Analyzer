package pl.netbio.internetusageanalyzer.service

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.TrafficStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataUsageMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    private val _currentRxBytes = MutableStateFlow(0L)
    val currentRxBytes: StateFlow<Long> = _currentRxBytes.asStateFlow()

    private val _currentTxBytes = MutableStateFlow(0L)
    val currentTxBytes: StateFlow<Long> = _currentTxBytes.asStateFlow()

    private val _totalRxBytes = MutableStateFlow(TrafficStats.getTotalRxBytes())
    val totalRxBytes: StateFlow<Long> = _totalRxBytes.asStateFlow()

    private val _totalTxBytes = MutableStateFlow(TrafficStats.getTotalTxBytes())
    val totalTxBytes: StateFlow<Long> = _totalTxBytes.asStateFlow()

    private val _downloadSpeed = MutableStateFlow(0.0)
    val downloadSpeed: StateFlow<Double> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0.0)
    val uploadSpeed: StateFlow<Double> = _uploadSpeed.asStateFlow()

    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTimestamp = System.currentTimeMillis()

    fun getCurrentRxBytes(): Long = TrafficStats.getTotalRxBytes()

    fun getCurrentTxBytes(): Long = TrafficStats.getTotalTxBytes()

    fun getUidRxBytes(uid: Int): Long = try {
        TrafficStats.getUidRxBytes(uid)
    } catch (e: Exception) {
        0L
    }

    fun getUidTxBytes(uid: Int): Long = try {
        TrafficStats.getUidTxBytes(uid)
    } catch (e: Exception) {
        0L
    }

    fun updateSpeed() {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()
        val timeDiff = (currentTime - lastTimestamp) / 1000.0

        if (timeDiff > 0) {
            _downloadSpeed.value = (currentRx - lastRxBytes) / timeDiff
            _uploadSpeed.value = (currentTx - lastTxBytes) / timeDiff
        }

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastTimestamp = currentTime

        _currentRxBytes.value = currentRx
        _currentTxBytes.value = currentTx
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }

    fun formatSpeed(bytesPerSecond: Double): String {
        if (bytesPerSecond < 1024) return String.format("%.0f B/s", bytesPerSecond)
        val kbps = bytesPerSecond / 1024.0
        if (kbps < 1024) return String.format("%.1f KB/s", kbps)
        val mbps = kbps / 1024.0
        return String.format("%.1f MB/s", mbps)
    }

    fun formatSpeedMbps(bytesPerSecond: Double): String {
        val mbps = (bytesPerSecond * 8) / 1_000_000.0
        return String.format("%.1f Mbps", mbps)
    }
}