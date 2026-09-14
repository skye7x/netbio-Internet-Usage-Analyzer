package com.bzygordev.netbio.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.bzygordev.netbio.util.DataUsagePreferences
import com.bzygordev.netbio.data.local.database.NetBioDatabase
import com.bzygordev.netbio.service.MonitoringService
import javax.inject.Inject

data class SettingsUiState(
    val dailyLimit: Long = 0L,
    val weeklyLimit: Long = 0L,
    val monthlyLimit: Long = 0L,
    val alertThreshold: Float = 80f,
    val billingCycleDay: Int = 1,
    val isAppLockEnabled: Boolean = false,
    val autoSpeedTestEnabled: Boolean = false,
    val autoSpeedTestInterval: Int = 60,
    val peakHoursEnabled: Boolean = false,
    val anomalyDetectionEnabled: Boolean = true,
    val isBackgroundServiceEnabled: Boolean = true,
    val appVersion: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: DataUsagePreferences,
    private val database: NetBioDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadAppVersion()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferences.dailyLimit.catch { e ->
                e.printStackTrace()
            }.collect { limit ->
                _uiState.update { it.copy(dailyLimit = limit) }
            }
        }
        viewModelScope.launch {
            preferences.weeklyLimit.catch { e ->
                e.printStackTrace()
            }.collect { limit ->
                _uiState.update { it.copy(weeklyLimit = limit) }
            }
        }
        viewModelScope.launch {
            preferences.monthlyLimit.catch { e ->
                e.printStackTrace()
            }.collect { limit ->
                _uiState.update { it.copy(monthlyLimit = limit) }
            }
        }
        viewModelScope.launch {
            preferences.alertThreshold.catch { e ->
                e.printStackTrace()
            }.collect { threshold ->
                _uiState.update { it.copy(alertThreshold = threshold.toFloat()) }
            }
        }
        viewModelScope.launch {
            preferences.billingCycleDay.catch { e ->
                e.printStackTrace()
            }.collect { day ->
                _uiState.update { it.copy(billingCycleDay = day) }
            }
        }
        viewModelScope.launch {
            preferences.isAppLockEnabled.catch { e ->
                e.printStackTrace()
            }.collect { enabled ->
                _uiState.update { it.copy(isAppLockEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferences.autoSpeedTestEnabled.catch { e ->
                e.printStackTrace()
            }.collect { enabled ->
                _uiState.update { it.copy(autoSpeedTestEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferences.autoSpeedTestIntervalHours.catch { e ->
                e.printStackTrace()
            }.collect { interval ->
                _uiState.update { it.copy(autoSpeedTestInterval = interval) }
            }
        }
        viewModelScope.launch {
            preferences.peakHoursEnabled.catch { e ->
                e.printStackTrace()
            }.collect { enabled ->
                _uiState.update { it.copy(peakHoursEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferences.anomalyDetectionEnabled.catch { e ->
                e.printStackTrace()
            }.collect { enabled ->
                _uiState.update { it.copy(anomalyDetectionEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferences.isBackgroundServiceEnabled.catch { e ->
                e.printStackTrace()
            }.collect { enabled ->
                _uiState.update { it.copy(isBackgroundServiceEnabled = enabled) }
            }
        }
    }

    private fun loadAppVersion() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: "Unknown"
            _uiState.update { it.copy(appVersion = versionName) }
        } catch (e: Exception) {
            _uiState.update { it.copy(appVersion = "Unknown") }
        }
    }

    fun updateSetting(key: String, value: Any) {
        viewModelScope.launch {
            try {
                when (key) {
                    "dailyLimit" -> preferences.setDailyLimit(value as Long)
                    "weeklyLimit" -> preferences.setWeeklyLimit(value as Long)
                    "monthlyLimit" -> preferences.setMonthlyLimit(value as Long)
                    "alertThreshold" -> preferences.setAlertThreshold((value as Float).toInt())
                    "billingCycleDay" -> preferences.setBillingCycleDay(value as Int)
                    "isAppLockEnabled" -> preferences.setAppLockEnabled(value as Boolean)
                    "autoSpeedTestEnabled" -> preferences.setAutoSpeedTestEnabled(value as Boolean)
                    "autoSpeedTestInterval" -> preferences.setAutoSpeedTestIntervalHours(value as Int)
                    "peakHoursEnabled" -> preferences.setPeakHoursEnabled(value as Boolean)
                    "anomalyDetectionEnabled" -> preferences.setAnomalyDetectionEnabled(value as Boolean)
                    "isBackgroundServiceEnabled" -> {
                        val enabled = value as Boolean
                        preferences.setBackgroundServiceEnabled(enabled)
                        if (enabled) {
                            MonitoringService.start(context)
                        } else {
                            MonitoringService.stop(context)
                        }
                    }
                }
                loadSettings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            try {
                database.clearAllTables()
                preferences.resetAll()
                loadSettings()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPrivacyInfo(): String {
        return """
            NetBio Internet Usage Analyzer Privacy Information:

            Data Collection:
            - Network usage statistics (download/upload per app)
            - WiFi connection information (SSID, signal strength)
            - Speed test results
            - Usage alerts and limits

            Data Storage:
            - All data is stored locally on your device
            - No data is transmitted to external servers
            - Data can be exported and deleted at any time

            Data Sharing:
            - No data is shared with third parties
            - Exported files are controlled by you

            Data Retention:
            - Data is retained until you choose to delete it
            - You can reset all data in Settings

            Your privacy is important to us. All processing happens on-device.
        """.trimIndent()
    }
}
