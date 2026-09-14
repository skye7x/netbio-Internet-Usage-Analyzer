package com.bzygordev.netbio.service

import android.content.Context
import android.net.TrafficStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataUsageMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _todayTotal = MutableStateFlow(0L)
    val todayTotal: StateFlow<Long> = _todayTotal.asStateFlow()

    private val _todayWifi = MutableStateFlow(0L)
    val todayWifi: StateFlow<Long> = _todayWifi.asStateFlow()

    private val _todayMobile = MutableStateFlow(0L)
    val todayMobile: StateFlow<Long> = _todayMobile.asStateFlow()

    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    private val _currentRxSpeed = MutableStateFlow(0L)
    val currentRxSpeed: StateFlow<Long> = _currentRxSpeed.asStateFlow()

    private val _currentTxSpeed = MutableStateFlow(0L)
    val currentTxSpeed: StateFlow<Long> = _currentTxSpeed.asStateFlow()

    private val _hourlyUsage = MutableStateFlow<List<Pair<Int, Long>>>(emptyList())
    val hourlyUsage: StateFlow<List<Pair<Int, Long>>> = _hourlyUsage.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private var lastRxBytes = TrafficStats.getTotalRxBytes()
    private var lastTxBytes = TrafficStats.getTotalTxBytes()
    private var lastSampleTime = System.currentTimeMillis()

    private val dailyHourlyUsage = mutableMapOf<Int, Long>()
    private var trackingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var baselineRxBytes = TrafficStats.getTotalRxBytes()
    private var baselineTxBytes = TrafficStats.getTotalTxBytes()
    private var baselineTime = System.currentTimeMillis()

    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastSampleTime = System.currentTimeMillis()
        baselineRxBytes = lastRxBytes
        baselineTxBytes = lastTxBytes
        baselineTime = lastSampleTime

        trackingJob = scope.launch {
            while (isActive) {
                periodicSample()
                delay(1000L)
            }
        }
    }

    fun stopTracking() {
        _isTracking.value = false
        trackingJob?.cancel()
        trackingJob = null
    }

    private suspend fun periodicSample() = withContext(Dispatchers.IO) {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()
        val deltaRx = (currentRx - lastRxBytes).coerceAtLeast(0)
        val deltaTx = (currentTx - lastTxBytes).coerceAtLeast(0)
        val deltaBytes = deltaRx + deltaTx

        _currentRxSpeed.value = deltaRx
        _currentTxSpeed.value = deltaTx
        _currentSpeed.value = calculateSpeed(deltaBytes, currentTime - lastSampleTime)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        dailyHourlyUsage[hour] = (dailyHourlyUsage[hour] ?: 0L) + deltaBytes
        _hourlyUsage.value = dailyHourlyUsage.toSortedMap().map { (k, v) -> k to v }

        _todayTotal.value = _todayTotal.value + deltaBytes
        if (_networkTypeIsWifi()) {
            _todayWifi.value = _todayWifi.value + deltaBytes
        } else {
            _todayMobile.value = _todayMobile.value + deltaBytes
        }

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastSampleTime = currentTime
    }

    private fun calculateSpeed(deltaBytes: Long, deltaMs: Long): Double {
        if (deltaMs <= 0) return 0.0
        return deltaBytes.toDouble() / (deltaMs / 1000.0)
    }

    private fun _networkTypeIsWifi(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        } catch (_: Exception) {
            true
        }
    }

    fun predictMonthlyUsage(): Long {
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (dayOfMonth <= 0) return 0L
        val dailyAverage = _todayTotal.value / dayOfMonth
        return dailyAverage * daysInMonth
    }

    fun getAnomalyScore(): Double {
        val currentTime = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val elapsed = currentTime - baselineTime
        if (elapsed <= 0 || elapsed < 60_000) return 0.0

        val totalDelta = (TrafficStats.getTotalRxBytes() - baselineRxBytes) +
                (TrafficStats.getTotalTxBytes() - baselineTxBytes)
        val projectedDaily = if (elapsed > 0) (totalDelta.toDouble() / elapsed) * dayMs else 0.0
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hourFraction = (currentHour + 1.0) / 24.0
        if (hourFraction <= 0.01) return 0.0
        val expectedDaily = projectedDaily / hourFraction
        val historicalAverage = _todayTotal.value.coerceAtLeast(1L) * 30L
        if (historicalAverage <= 0) return 0.0

        return ((expectedDaily - historicalAverage) / historicalAverage).coerceIn(-1.0, 1.0)
    }

    fun getCurrentRxBytes(): Long = TrafficStats.getTotalRxBytes()

    fun getCurrentTxBytes(): Long = TrafficStats.getTotalTxBytes()

    fun getUidRxBytes(uid: Int): Long = try {
        TrafficStats.getUidRxBytes(uid)
    } catch (_: Exception) {
        0L
    }

    fun getUidTxBytes(uid: Int): Long = try {
        TrafficStats.getUidTxBytes(uid)
    } catch (_: Exception) {
        0L
    }

    fun updateSpeed() {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()
        val timeDiff = (currentTime - lastSampleTime) / 1000.0

        if (timeDiff > 0) {
            _currentRxSpeed.value = ((currentRx - lastRxBytes) / timeDiff).toLong()
            _currentTxSpeed.value = ((currentTx - lastTxBytes) / timeDiff).toLong()
            _currentSpeed.value = (currentRx - lastRxBytes + currentTx - lastTxBytes) / timeDiff
        }

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastSampleTime = currentTime
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

    fun resetDaily() {
        _todayTotal.value = 0L
        _todayWifi.value = 0L
        _todayMobile.value = 0L
        dailyHourlyUsage.clear()
        _hourlyUsage.value = emptyList()
        baselineRxBytes = TrafficStats.getTotalRxBytes()
        baselineTxBytes = TrafficStats.getTotalTxBytes()
        baselineTime = System.currentTimeMillis()
    }
}
