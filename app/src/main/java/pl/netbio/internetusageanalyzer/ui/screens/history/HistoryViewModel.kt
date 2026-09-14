package pl.netbio.internetusageanalyzer.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.local.entity.SpeedTestResultEntity
import pl.netbio.internetusageanalyzer.data.repository.DataUsageRepository
import pl.netbio.internetusageanalyzer.data.repository.SpeedTestRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HistoryUiState(
    val selectedTab: Int = 0,
    val dailyUsage: List<Pair<String, Long>> = emptyList(),
    val hourlyUsage: List<Pair<Int, Long>> = emptyList(),
    val allDates: List<String> = emptyList(),
    val selectedDate: String = "",
    val weeklyMonthlyYearlyUsage: Map<String, Long> = emptyMap(),
    val speedTestHistory: List<SpeedTestResultEntity> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dataUsageRepository: DataUsageRepository,
    private val speedTestRepository: SpeedTestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        val today = dateFormat.format(Date())
        _uiState.update { it.copy(selectedDate = today) }
        loadAllDates()
        loadSpeedTestHistory()
        loadUsageForDate(today)
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        getUsageForPeriod()
    }

    fun setDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadUsageForDate(date)
        getUsageForPeriod()
    }

    private fun loadAllDates() {
        viewModelScope.launch {
            try {
                val dates = dataUsageRepository.getAllDates().first()
                _uiState.update { it.copy(allDates = dates) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSpeedTestHistory() {
        viewModelScope.launch {
            try {
                speedTestRepository.getAll().catch { e ->
                    e.printStackTrace()
                }.collect { results ->
                    _uiState.update { it.copy(speedTestHistory = results) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadUsageForDate(date: String) {
        viewModelScope.launch {
            try {
                val dailyUsage = dataUsageRepository.getDailyUsageForMonth(date).first()
                _uiState.update { it.copy(dailyUsage = dailyUsage) }

                val hourlyUsage = dataUsageRepository.getHourlyUsage(date).first()
                _uiState.update { it.copy(hourlyUsage = hourlyUsage) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getUsageForPeriod() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                when (state.selectedTab) {
                    0 -> {
                        val dailyUsage = dataUsageRepository.getDailyUsageForMonth(state.selectedDate).first()
                        val hourlyUsage = dataUsageRepository.getHourlyUsage(state.selectedDate).first()
                        _uiState.update {
                            it.copy(
                                dailyUsage = dailyUsage,
                                hourlyUsage = hourlyUsage
                            )
                        }
                    }
                    1 -> {
                        val calendar = Calendar.getInstance()
                        val weeklyMap = mutableMapOf<String, Long>()
                        for (i in 6 downTo 0) {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -i)
                            val date = dateFormat.format(cal.time)
                            val dayUsage = dataUsageRepository.getDailyUsageForMonth(date).first()
                            val total = dayUsage.sumOf { it.second }
                            weeklyMap[date] = total
                        }
                        _uiState.update {
                            it.copy(weeklyMonthlyYearlyUsage = weeklyMap)
                        }
                    }
                    2 -> {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val monthlyMap = mutableMapOf<String, Long>()
                        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        for (day in 1..daysInMonth) {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_MONTH, day)
                            val date = dateFormat.format(cal.time)
                            val dayUsage = dataUsageRepository.getDailyUsageForMonth(date).first()
                            val total = dayUsage.sumOf { it.second }
                            monthlyMap["${month}/${day}"] = total
                        }
                        _uiState.update {
                            it.copy(weeklyMonthlyYearlyUsage = monthlyMap)
                        }
                    }
                    3 -> {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val yearlyMap = mutableMapOf<String, Long>()
                        for (month in 1..12) {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.MONTH, month - 1)
                            cal.set(Calendar.YEAR, year)
                            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                            var monthTotal = 0L
                            for (day in 1..daysInMonth) {
                                cal.set(Calendar.DAY_OF_MONTH, day)
                                val date = dateFormat.format(cal.time)
                                val dayUsage = dataUsageRepository.getDailyUsageForMonth(date).first()
                                monthTotal += dayUsage.sumOf { it.second }
                            }
                            yearlyMap["$year-$month"] = monthTotal
                        }
                        _uiState.update {
                            it.copy(weeklyMonthlyYearlyUsage = yearlyMap)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
