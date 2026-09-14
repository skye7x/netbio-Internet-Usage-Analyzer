package pl.netbio.internetusageanalyzer.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.local.entity.DataUsageEntity
import pl.netbio.internetusageanalyzer.data.local.entity.SpeedTestEntity
import pl.netbio.internetusageanalyzer.data.repository.*
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import pl.netbio.internetusageanalyzer.util.DataUsagePreferences
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val speedTestRepository: SpeedTestRepository,
    private val alertRepository: AlertRepository,
    private val limitRepository: LimitRepository,
    private val dataUsageMonitor: DataUsageMonitor,
    private val preferences: DataUsagePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val today = getTodayDate()
        val todayStart = getTodayStart()
        val todayEnd = getTodayEnd()
        val weekStart = getWeekStart()
        val monthStart = getMonthStart()
        val yesterdayStart = getYesterdayStart()
        val yesterdayEnd = getYesterdayEnd()

        viewModelScope.launch {
            combine(
                usageRepository.getTotalUsageByDate(today),
                usageRepository.getWifiUsageByDate(today),
                usageRepository.getMobileUsageByDate(today),
                usageRepository.getTotalUsageBetween(yesterdayStart, yesterdayEnd),
                usageRepository.getTotalUsageBetween(weekStart, System.currentTimeMillis()),
                usageRepository.getTotalUsageBetween(monthStart, System.currentTimeMillis()),
                speedTestRepository.getLatestTest(),
                alertRepository.getUnreadCount(),
                preferences.dailyLimit
            ) { results ->
                val todayTotal = results[0] as? Long ?: 0L
                val todayWifi = results[1] as? Long ?: 0L
                val todayMobile = results[2] as? Long ?: 0L
                val yesterdayTotal = results[3] as? Long ?: 0L
                val weekTotal = results[4] as? Long ?: 0L
                val monthTotal = results[5] as? Long ?: 0L
                val latestSpeedTest = results[6] as? SpeedTestEntity?
                val unreadAlerts = results[7] as? Int ?: 0
                val dailyLimit = results[8] as? Long ?: 0L

                val usageChange = if (yesterdayTotal > 0) {
                    ((todayTotal - yesterdayTotal).toFloat() / yesterdayTotal) * 100f
                } else 0f

                val limitPercentage = if (dailyLimit > 0) {
                    (todayTotal.toFloat() / dailyLimit) * 100f
                } else 0f

                val predictedMonthly = if (todayTotal > 0) {
                    val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                    (todayTotal.toFloat() / dayOfMonth) * daysInMonth
                } else 0f

                DashboardUiState(
                    todayUsage = todayTotal,
                    todayWifiUsage = todayWifi,
                    todayMobileUsage = todayMobile,
                    weekUsage = weekTotal,
                    monthUsage = monthTotal,
                    usageChangePercent = usageChange,
                    dailyLimit = dailyLimit,
                    dailyLimitPercentage = limitPercentage,
                    predictedMonthlyUsage = predictedMonthly,
                    latestSpeedTest = latestSpeedTest,
                    unreadAlerts = unreadAlerts,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        viewModelScope.launch {
            dataUsageMonitor.downloadSpeed.collect { speed ->
                _uiState.value = _uiState.value.copy(currentDownloadSpeed = speed)
            }
        }
        viewModelScope.launch {
            dataUsageMonitor.uploadSpeed.collect { speed ->
                _uiState.value = _uiState.value.copy(currentUploadSpeed = speed)
            }
        }
    }

    fun formatBytes(bytes: Long): String = dataUsageMonitor.formatBytes(bytes)
    fun formatSpeed(bytesPerSecond: Double): String = dataUsageMonitor.formatSpeedMbps(bytesPerSecond)

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun getTodayStart(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun getTodayEnd(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private fun getWeekStart(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun getMonthStart(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun getYesterdayStart(): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun getYesterdayEnd(): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

data class DashboardUiState(
    val todayUsage: Long = 0L,
    val todayWifiUsage: Long = 0L,
    val todayMobileUsage: Long = 0L,
    val weekUsage: Long = 0L,
    val monthUsage: Long = 0L,
    val usageChangePercent: Float = 0f,
    val dailyLimit: Long = 0L,
    val dailyLimitPercentage: Float = 0f,
    val predictedMonthlyUsage: Float = 0f,
    val latestSpeedTest: SpeedTestEntity? = null,
    val unreadAlerts: Int = 0,
    val currentDownloadSpeed: Double = 0.0,
    val currentUploadSpeed: Double = 0.0,
    val isLoading: Boolean = true
)
