package pl.netbio.internetusageanalyzer.ui.screens.appusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.local.dao.AppUsageSummary
import pl.netbio.internetusageanalyzer.data.repository.UsageRepository
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AppUsageViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val dataUsageMonitor: DataUsageMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUsageUiState())
    val uiState: StateFlow<AppUsageUiState> = _uiState.asStateFlow()

    init { loadAppUsage() }

    private fun loadAppUsage() {
        val cal = Calendar.getInstance()
        val todayStart = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            usageRepository.getAppUsageBetween(todayStart, now).collect { apps ->
                val total = apps.sumOf { it.total }
                val ranked = apps.map { AppUsageItem(it.packageName, it.total,
                    if (total > 0) (it.total.toFloat() / total) * 100f else 0f) }
                _uiState.value = _uiState.value.copy(appUsages = ranked, totalUsage = total)
            }
        }
    }

    fun formatBytes(bytes: Long): String = dataUsageMonitor.formatBytes(bytes)
}

data class AppUsageUiState(
    val appUsages: List<AppUsageItem> = emptyList(),
    val totalUsage: Long = 0L
)

data class AppUsageItem(val packageName: String, val bytes: Long, val percentage: Float)
