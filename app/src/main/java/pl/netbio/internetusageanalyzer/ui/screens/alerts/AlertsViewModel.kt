package pl.netbio.internetusageanalyzer.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.data.local.entity.AlertEntity
import pl.netbio.internetusageanalyzer.data.local.entity.UsageLimitEntity
import pl.netbio.internetusageanalyzer.data.repository.AlertRepository
import pl.netbio.internetusageanalyzer.data.repository.LimitRepository
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import pl.netbio.internetusageanalyzer.util.DataUsagePreferences
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val limitRepository: LimitRepository,
    private val dataUsageMonitor: DataUsageMonitor,
    private val preferences: DataUsagePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            alertRepository.getAllAlerts().collect { alerts ->
                _uiState.value = _uiState.value.copy(alerts = alerts)
            }
        }
        viewModelScope.launch {
            alertRepository.getUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
        viewModelScope.launch {
            limitRepository.getAllLimits().collect { limits ->
                _uiState.value = _uiState.value.copy(limits = limits)
            }
        }
    }

    fun markAsRead(id: Long) { viewModelScope.launch { alertRepository.markAsRead(id) } }
    fun markAllAsRead() { viewModelScope.launch { alertRepository.markAllAsRead() } }

    fun addLimit(type: String, limitBytes: Long, warningPct: Float, alertPct: Float) {
        viewModelScope.launch {
            limitRepository.insertLimit(
                UsageLimitEntity(type = type, limitBytes = limitBytes,
                    warningPercentage = warningPct, alertPercentage = alertPct)
            )
        }
    }

    fun updateLimit(limit: UsageLimitEntity) { viewModelScope.launch { limitRepository.updateLimit(limit) } }
    fun deleteLimit(limit: UsageLimitEntity) { viewModelScope.launch { limitRepository.deleteLimit(limit) } }

    fun formatBytes(bytes: Long): String = dataUsageMonitor.formatBytes(bytes)
}

data class AlertsUiState(
    val alerts: List<AlertEntity> = emptyList(),
    val unreadCount: Int = 0,
    val limits: List<UsageLimitEntity> = emptyList()
)
