package pl.netbio.internetusageanalyzer.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.local.dao.DailySummary
import pl.netbio.internetusageanalyzer.data.local.entity.DataUsageEntity
import pl.netbio.internetusageanalyzer.data.repository.UsageRepository
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val dataUsageMonitor: DataUsageMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            usageRepository.getDailySummaries(30).collect { summaries ->
                _uiState.value = _uiState.value.copy(dailySummaries = summaries)
            }
        }
        val today = getTodayDate()
        viewModelScope.launch {
            usageRepository.getHourlyUsage(today).collect { hourly ->
                _uiState.value = _uiState.value.copy(hourlyUsage = hourly)
            }
        }
    }

    fun selectDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        viewModelScope.launch {
            usageRepository.getUsageByDate(date).collect { usages ->
                _uiState.value = _uiState.value.copy(selectedDateUsages = usages)
            }
        }
    }

    fun formatBytes(bytes: Long): String = dataUsageMonitor.formatBytes(bytes)

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}

data class HistoryUiState(
    val dailySummaries: List<DailySummary> = emptyList(),
    val hourlyUsage: List<DataUsageEntity> = emptyList(),
    val selectedDate: String = "",
    val selectedDateUsages: List<DataUsageEntity> = emptyList()
)
