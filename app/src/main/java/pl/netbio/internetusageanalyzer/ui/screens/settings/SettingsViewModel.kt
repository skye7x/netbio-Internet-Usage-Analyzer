package pl.netbio.internetusageanalyzer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.netbio.internetusageanalyzer.util.DataUsagePreferences
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: DataUsagePreferences
) : ViewModel() {

    val isDarkMode = preferences.isDarkMode
    val isPinEnabled = preferences.isPinEnabled
    val biometricEnabled = preferences.biometricEnabled
    val dailyLimit = preferences.dailyLimit
    val weeklyLimit = preferences.weeklyLimit
    val monthlyLimit = preferences.monthlyLimit
    val warningPercentage = preferences.warningPercentage
    val alertPercentage = preferences.alertPercentage
    val scheduledSpeedtestEnabled = preferences.scheduledSpeedtestEnabled
    val scheduledSpeedtestInterval = preferences.scheduledSpeedtestInterval

    fun setDarkMode(enabled: Boolean) { viewModelScope.launch { preferences.setDarkMode(enabled) } }
    fun setPinCode(pin: String) { viewModelScope.launch { preferences.setPinCode(pin) } }
    fun setPinEnabled(enabled: Boolean) { viewModelScope.launch { preferences.setPinEnabled(enabled) } }
    fun setBiometricEnabled(enabled: Boolean) { viewModelScope.launch { preferences.setBiometricEnabled(enabled) } }
    fun setDailyLimit(bytes: Long) { viewModelScope.launch { preferences.setDailyLimit(bytes) } }
    fun setWeeklyLimit(bytes: Long) { viewModelScope.launch { preferences.setWeeklyLimit(bytes) } }
    fun setMonthlyLimit(bytes: Long) { viewModelScope.launch { preferences.setMonthlyLimit(bytes) } }
    fun setWarningPercentage(percent: Float) { viewModelScope.launch { preferences.setWarningPercentage(percent) } }
    fun setAlertPercentage(percent: Float) { viewModelScope.launch { preferences.setAlertPercentage(percent) } }
    fun setScheduledSpeedtest(enabled: Boolean) { viewModelScope.launch { preferences.setScheduledSpeedtest(enabled) } }
    fun setScheduledSpeedtestInterval(minutes: Int) { viewModelScope.launch { preferences.setScheduledSpeedtestInterval(minutes) } }
}
