package com.bzygordev.netbio.ui.screens.alerts

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
import com.bzygordev.netbio.data.local.entity.AlertEntity
import com.bzygordev.netbio.data.local.entity.UsageLimitEntity
import com.bzygordev.netbio.data.repository.AlertRepository
import com.bzygordev.netbio.data.repository.DataUsageRepository
import com.bzygordev.netbio.data.repository.LimitRepository
import com.bzygordev.netbio.service.NotificationHelper
import java.util.Date
import javax.inject.Inject

data class AlertsUiState(
    val alerts: List<AlertEntity> = emptyList(),
    val unreadCount: Int = 0,
    val limits: List<UsageLimitEntity> = emptyList(),
    val dailyLimit: Long = 0L,
    val weeklyLimit: Long = 0L,
    val monthlyLimit: Long = 0L
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val limitRepository: LimitRepository,
    private val dataUsageRepository: DataUsageRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
        loadLimits()
        collectUnreadCount()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            try {
                alertRepository.getAll().catch { e ->
                    e.printStackTrace()
                }.collect { alerts ->
                    _uiState.update { it.copy(alerts = alerts) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun collectUnreadCount() {
        viewModelScope.launch {
            alertRepository.getUnreadCount().catch { e ->
                e.printStackTrace()
            }.collect { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
    }

    private fun loadLimits() {
        viewModelScope.launch {
            try {
                val limits = limitRepository.getAll().first()
                val daily = limitRepository.getDailyLimit().first()
                val weekly = limitRepository.getWeeklyLimit().first()
                val monthly = limitRepository.getMonthlyLimit().first()
                _uiState.update {
                    it.copy(
                        limits = limits,
                        dailyLimit = daily?.limitBytes ?: 0L,
                        weeklyLimit = weekly?.limitBytes ?: 0L,
                        monthlyLimit = monthly?.limitBytes ?: 0L
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAsRead(alertId: Long) {
        viewModelScope.launch {
            try {
                alertRepository.markAsRead(alertId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                alertRepository.markAllAsRead()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAlert(alertId: Long) {
        viewModelScope.launch {
            try {
                alertRepository.deleteById(alertId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addLimit(type: String, bytes: Long, threshold: Float) {
        viewModelScope.launch {
            try {
                val limit = UsageLimitEntity(
                    type = type,
                    limitBytes = bytes,
                    alertThreshold = threshold.toInt(),
                    isEnabled = true
                )
                limitRepository.insertOrUpdate(limit)
                loadLimits()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateLimit(limit: UsageLimitEntity) {
        viewModelScope.launch {
            try {
                limitRepository.insertOrUpdate(limit)
                loadLimits()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteLimit(limitId: Long) {
        viewModelScope.launch {
            try {
                limitRepository.deleteById(limitId)
                loadLimits()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkLimits() {
        viewModelScope.launch {
            try {
                val todayUsage = dataUsageRepository.getTodayUsage().first()
                val weekUsage = dataUsageRepository.getWeekUsage().first()
                val monthUsage = dataUsageRepository.getMonthUsage().first()
                val triggeredLimits = limitRepository.checkLimits(todayUsage, weekUsage, monthUsage)

                triggeredLimits.forEach { (limit, percentage) ->
                    if (percentage >= limit.alertThreshold) {
                        val existingAlert = _uiState.value.alerts.find {
                            it.type == "LIMIT_EXCEEDED" && it.title.contains(limit.type, ignoreCase = true) && !it.isRead
                        }
                        if (existingAlert == null) {
                            val alert = AlertEntity(
                                title = "Usage Limit Exceeded",
                                message = "Your ${limit.type} limit has been exceeded. Current usage: ${formatBytes(todayUsage)}, Limit: ${formatBytes(limit.limitBytes)}",
                                type = "LIMIT_EXCEEDED",
                                timestamp = System.currentTimeMillis(),
                                isRead = false
                            )
                            alertRepository.insert(alert)
                            notificationHelper.showLimitExceededNotification(
                                used = todayUsage,
                                limit = limit.limitBytes,
                                type = limit.type
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            .coerceIn(0, units.size - 1)
        return String.format(
            java.util.Locale.US,
            "%.1f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }
}
