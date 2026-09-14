package pl.netbio.internetusageanalyzer.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netbio_prefs")

@Singleton
class DataUsagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val DAILY_LIMIT = longPreferencesKey("daily_limit")
        val WEEKLY_LIMIT = longPreferencesKey("weekly_limit")
        val MONTHLY_LIMIT = longPreferencesKey("monthly_limit")
        val WARNING_PERCENTAGE = floatPreferencesKey("warning_percentage")
        val ALERT_PERCENTAGE = floatPreferencesKey("alert_percentage")
        val CURRENT_DAY_USAGE = longPreferencesKey("current_day_usage")
        val CURRENT_WEEK_USAGE = longPreferencesKey("current_week_usage")
        val CURRENT_MONTH_USAGE = longPreferencesKey("current_month_usage")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val PIN_CODE = stringPreferencesKey("pin_code")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LAST_SPEEDTEST_DATE = stringPreferencesKey("last_speedtest_date")
        val SCHEDULED_SPEEDTEST_ENABLED = booleanPreferencesKey("scheduled_speedtest_enabled")
        val SCHEDULED_SPEEDTEST_INTERVAL = intPreferencesKey("scheduled_speedtest_interval")
        val NOTIFICATION_HISTORY = stringPreferencesKey("notification_history")
        val ACHIEVEMENTS_UNLOCKED = stringPreferencesKey("achievements_unlocked")
    }

    val dailyLimit: Flow<Long> = context.dataStore.data.map { it[DAILY_LIMIT] ?: 0L }
    val weeklyLimit: Flow<Long> = context.dataStore.data.map { it[WEEKLY_LIMIT] ?: 0L }
    val monthlyLimit: Flow<Long> = context.dataStore.data.map { it[MONTHLY_LIMIT] ?: 0L }
    val warningPercentage: Flow<Float> = context.dataStore.data.map { it[WARNING_PERCENTAGE] ?: 80f }
    val alertPercentage: Flow<Float> = context.dataStore.data.map { it[ALERT_PERCENTAGE] ?: 95f }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_MODE] ?: true }
    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_PIN_ENABLED] ?: false }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }
    val scheduledSpeedtestEnabled: Flow<Boolean> = context.dataStore.data.map { it[SCHEDULED_SPEEDTEST_ENABLED] ?: false }
    val scheduledSpeedtestInterval: Flow<Int> = context.dataStore.data.map { it[SCHEDULED_SPEEDTEST_INTERVAL] ?: 60 }

    suspend fun setDailyLimit(bytes: Long) { context.dataStore.edit { it[DAILY_LIMIT] = bytes } }
    suspend fun setWeeklyLimit(bytes: Long) { context.dataStore.edit { it[WEEKLY_LIMIT] = bytes } }
    suspend fun setMonthlyLimit(bytes: Long) { context.dataStore.edit { it[MONTHLY_LIMIT] = bytes } }
    suspend fun setWarningPercentage(percent: Float) { context.dataStore.edit { it[WARNING_PERCENTAGE] = percent } }
    suspend fun setAlertPercentage(percent: Float) { context.dataStore.edit { it[ALERT_PERCENTAGE] = percent } }
    suspend fun setDarkMode(enabled: Boolean) { context.dataStore.edit { it[IS_DARK_MODE] = enabled } }
    suspend fun setPinCode(pin: String) { context.dataStore.edit { it[PIN_CODE] = pin } }
    suspend fun setPinEnabled(enabled: Boolean) { context.dataStore.edit { it[IS_PIN_ENABLED] = enabled } }
    suspend fun setBiometricEnabled(enabled: Boolean) { context.dataStore.edit { it[BIOMETRIC_ENABLED] = enabled } }
    suspend fun setScheduledSpeedtest(enabled: Boolean) { context.dataStore.edit { it[SCHEDULED_SPEEDTEST_ENABLED] = enabled } }
    suspend fun setScheduledSpeedtestInterval(minutes: Int) { context.dataStore.edit { it[SCHEDULED_SPEEDTEST_INTERVAL] = minutes } }

    suspend fun getCurrentDayUsage(): Long = runBlocking {
        context.dataStore.data.first()[CURRENT_DAY_USAGE] ?: 0L
    }

    suspend fun setCurrentDayUsage(bytes: Long) { context.dataStore.edit { it[CURRENT_DAY_USAGE] = bytes } }

    suspend fun getDailyLimit(): Long = runBlocking {
        context.dataStore.data.first()[DAILY_LIMIT] ?: 0L
    }

    suspend fun getWarningPercentage(): Float = runBlocking {
        context.dataStore.data.first()[WARNING_PERCENTAGE] ?: 80f
    }

    suspend fun getAlertPercentage(): Float = runBlocking {
        context.dataStore.data.first()[ALERT_PERCENTAGE] ?: 95f
    }

    suspend fun verifyPin(pin: String): Boolean = runBlocking {
        context.dataStore.data.first()[PIN_CODE] == pin
    }
}