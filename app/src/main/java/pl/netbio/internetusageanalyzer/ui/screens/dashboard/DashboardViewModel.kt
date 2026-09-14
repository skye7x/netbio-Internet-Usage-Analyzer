package pl.netbio.internetusageanalyzer.ui.screens.dashboard

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
import pl.netbio.internetusageanalyzer.data.repository.AlertRepository
import pl.netbio.internetusageanalyzer.data.repository.DataUsageRepository
import pl.netbio.internetusageanalyzer.data.repository.LimitRepository
import pl.netbio.internetusageanalyzer.data.repository.SpeedTestRepository
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import pl.netbio.internetusageanalyzer.service.NetworkMonitor
import pl.netbio.internetusageanalyzer.service.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val todayUsage: Long = 0L,
    val todayWifiUsage: Long = 0L,
    val todayMobileUsage: Long = 0L,
    val weekUsage: Long = 0L,
    val monthUsage: Long = 0L,
    val yearUsage: Long = 0L,
    val usageChangePercent: Float = 0f,
    val predictedMonthlyUsage: Long = 0L,
    val currentDownloadSpeed: Float = 0f,
    val currentUploadSpeed: Float = 0f,
    val currentSpeed: Float = 0f,
    val dailyLimit: Long = 0L,
    val dailyLimitPercentage: Float = 0f,
    val weeklyLimit: Long = 0L,
    val monthlyLimit: Long = 0L,
    val unreadAlerts: Int = 0,
    val isConnected: Boolean = false,
    val networkType: String = "None",
    val anomalyScore: Float = 0f,
    val hourlyUsage: List<Pair<Int, Long>> = emptyList(),
    val weeklyDailyUsage: List<Pair<String, Long>> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dataUsageRepository: DataUsageRepository,
    private val speedTestRepository: SpeedTestRepository,
    private val alertRepository: AlertRepository,
    private val limitRepository: LimitRepository,
    private val networkMonitor: NetworkMonitor,
    private val dataUsageMonitor: DataUsageMonitor,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        collectNetworkState()
        collectDataUsage()
        collectSpeedTest()
        collectAlerts()
        collectLimits()
        collectHourlyUsage()
        collectCurrentSpeed()
    }

    private fun collectNetworkState() {
        viewModelScope.launch {
            combine(
                networkMonitor.isConnected,
                networkMonitor.networkType
            ) { connected, type ->
                Pair(connected, type)
            }.catch { e ->
                e.printStackTrace()
            }.collect { (connected, type) ->
                _uiState.update {
                    it.copy(
                        isConnected = connected,
                        networkType = type
                    )
                }
            }
        }
    }

    private fun collectDataUsage() {
        viewModelScope.launch {
            dataUsageMonitor.todayTotal.catch { e ->
                e.printStackTrace()
            }.collect { total ->
                _uiState.update { it.copy(todayUsage = total) }
            }
        }
        viewModelScope.launch {
            dataUsageMonitor.todayWifi.catch { e ->
                e.printStackTrace()
            }.collect { wifi ->
                _uiState.update { it.copy(todayWifiUsage = wifi) }
            }
        }
        viewModelScope.launch {
            dataUsageMonitor.todayMobile.catch { e ->
                e.printStackTrace()
            }.collect { mobile ->
                _uiState.update { it.copy(todayMobileUsage = mobile) }
            }
        }
        viewModelScope.launch {
            try {
                val weekUsage = dataUsageRepository.getWeekUsage().first()
                _uiState.update { it.copy(weekUsage = weekUsage) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            try {
                val monthUsage = dataUsageRepository.getMonthUsage().first()
                _uiState.update { it.copy(monthUsage = monthUsage) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            try {
                val yearUsage = dataUsageRepository.getYearUsage().first()
                _uiState.update { it.copy(yearUsage = yearUsage) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            try {
                val changePercent = dataUsageRepository.getUsageChangePercent("daily").first()
                _uiState.update { it.copy(usageChangePercent = changePercent) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            try {
                val predicted = dataUsageMonitor.predictMonthlyUsage()
                _uiState.update { it.copy(predictedMonthlyUsage = predicted) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            try {
                val anomalyScore = dataUsageMonitor.getAnomalyScore()
                _uiState.update { it.copy(anomalyScore = anomalyScore.toFloat()) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun collectSpeedTest() {
        viewModelScope.launch {
            try {
                val latest = speedTestRepository.getLatest().first()
                if (latest != null) {
                    _uiState.update {
                        it.copy(
                            currentDownloadSpeed = latest.downloadSpeed.toFloat(),
                            currentUploadSpeed = latest.uploadSpeed.toFloat()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun collectAlerts() {
        viewModelScope.launch {
            alertRepository.getUnreadCount().catch { e ->
                e.printStackTrace()
            }.collect { count ->
                _uiState.update { it.copy(unreadAlerts = count) }
            }
        }
    }

    private fun collectLimits() {
        viewModelScope.launch {
            try {
                val daily = limitRepository.getDailyLimit().first()
                val weekly = limitRepository.getWeeklyLimit().first()
                val monthly = limitRepository.getMonthlyLimit().first()
                val dailyLimitBytes = daily?.limitBytes ?: 0L
                val weeklyLimitBytes = weekly?.limitBytes ?: 0L
                val monthlyLimitBytes = monthly?.limitBytes ?: 0L
                val todayUsage = _uiState.value.todayUsage
                val dailyPercent = if (dailyLimitBytes > 0) (todayUsage.toFloat() / dailyLimitBytes.toFloat()) * 100f else 0f
                _uiState.update {
                    it.copy(
                        dailyLimit = dailyLimitBytes,
                        dailyLimitPercentage = dailyPercent,
                        weeklyLimit = weeklyLimitBytes,
                        monthlyLimit = monthlyLimitBytes
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun collectHourlyUsage() {
        viewModelScope.launch {
            try {
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                val hourly = dataUsageRepository.getHourlyUsage(today).first()
                _uiState.update { it.copy(hourlyUsage = hourly) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun collectCurrentSpeed() {
        viewModelScope.launch {
            dataUsageMonitor.currentSpeed.catch { e ->
                e.printStackTrace()
            }.collect { speed ->
                _uiState.update { it.copy(currentSpeed = speed.toFloat()) }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                val weekUsage = dataUsageRepository.getWeekUsage().first()
                val monthUsage = dataUsageRepository.getMonthUsage().first()
                val yearUsage = dataUsageRepository.getYearUsage().first()
                val changePercent = dataUsageRepository.getUsageChangePercent("daily").first()
                val predicted = dataUsageMonitor.predictMonthlyUsage()
                val anomalyScore = dataUsageMonitor.getAnomalyScore()
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val hourly = dataUsageRepository.getHourlyUsage(today).first()
                val daily = limitRepository.getDailyLimit().first()
                val weekly = limitRepository.getWeeklyLimit().first()
                val monthly = limitRepository.getMonthlyLimit().first()
                val todayUsage = _uiState.value.todayUsage
                val dailyLimitBytes = daily?.limitBytes ?: 0L
                val weeklyLimitBytes = weekly?.limitBytes ?: 0L
                val monthlyLimitBytes = monthly?.limitBytes ?: 0L
                val dailyPercent = if (dailyLimitBytes > 0) (todayUsage.toFloat() / dailyLimitBytes.toFloat()) * 100f else 0f

                _uiState.update {
                    it.copy(
                        weekUsage = weekUsage,
                        monthUsage = monthUsage,
                        yearUsage = yearUsage,
                        usageChangePercent = changePercent,
                        predictedMonthlyUsage = predicted,
                        anomalyScore = anomalyScore.toFloat(),
                        hourlyUsage = hourly,
                        dailyLimit = dailyLimitBytes,
                        dailyLimitPercentage = dailyPercent,
                        weeklyLimit = weeklyLimitBytes,
                        monthlyLimit = monthlyLimitBytes
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onAlertThresholdExceeded() {
        viewModelScope.launch {
            try {
                val todayUsage = _uiState.value.todayUsage
                val weekUsage = _uiState.value.weekUsage
                val monthUsage = _uiState.value.monthUsage
                val limits = limitRepository.checkLimits(todayUsage, weekUsage, monthUsage)
                limits.forEach { (limit, percentage) ->
                    if (percentage >= limit.alertThreshold) {
                        notificationHelper.showLimitExceededNotification(
                            used = todayUsage,
                            limit = limit.limitBytes,
                            type = limit.type
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            .coerceIn(0, units.size - 1)
        return String.format(
            Locale.US,
            "%.1f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun formatSpeed(mbps: Float): String {
        if (mbps <= 0f) return "0 Mbps"
        return String.format(Locale.US, "%.1f Mbps", mbps.toDouble())
    }
}
