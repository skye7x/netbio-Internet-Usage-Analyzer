package com.bzygordev.netbio.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netbio_prefs")

@Singleton
class DataUsagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val DAILY_LIMIT = longPreferencesKey("daily_limit")
        val WEEKLY_LIMIT = longPreferencesKey("weekly_limit")
        val MONTHLY_LIMIT = longPreferencesKey("monthly_limit")
        val ALERT_THRESHOLD = intPreferencesKey("alert_threshold")
        val BILLING_CYCLE_DAY = intPreferencesKey("billing_cycle_day")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        val IS_REAL_TIME_TRACKING_ENABLED = booleanPreferencesKey("is_real_time_tracking_enabled")
        val AUTO_SPEED_TEST_ENABLED = booleanPreferencesKey("auto_speed_test_enabled")
        val AUTO_SPEED_TEST_INTERVAL_HOURS = intPreferencesKey("auto_speed_test_interval_hours")
        val PEAK_HOURS_ENABLED = booleanPreferencesKey("peak_hours_enabled")
        val ANOMALY_DETECTION_ENABLED = booleanPreferencesKey("anomaly_detection_enabled")
        val LAST_BACKUP_DATE = stringPreferencesKey("last_backup_date")
        val IS_BACKGROUND_SERVICE_ENABLED = booleanPreferencesKey("is_background_service_enabled")
    }

    val dailyLimit: Flow<Long> = context.dataStore.data.map { it[Keys.DAILY_LIMIT] ?: 0L }
    val weeklyLimit: Flow<Long> = context.dataStore.data.map { it[Keys.WEEKLY_LIMIT] ?: 0L }
    val monthlyLimit: Flow<Long> = context.dataStore.data.map { it[Keys.MONTHLY_LIMIT] ?: 0L }
    val alertThreshold: Flow<Int> = context.dataStore.data.map { it[Keys.ALERT_THRESHOLD] ?: 80 }
    val billingCycleDay: Flow<Int> = context.dataStore.data.map { it[Keys.BILLING_CYCLE_DAY] ?: 1 }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_DARK_MODE] ?: true }
    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_APP_LOCK_ENABLED] ?: false }
    val appLockPin: Flow<String> = context.dataStore.data.map { it[Keys.APP_LOCK_PIN] ?: "" }
    val isRealTimeTrackingEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_REAL_TIME_TRACKING_ENABLED] ?: true }
    val autoSpeedTestEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_SPEED_TEST_ENABLED] ?: false }
    val autoSpeedTestIntervalHours: Flow<Int> = context.dataStore.data.map { it[Keys.AUTO_SPEED_TEST_INTERVAL_HOURS] ?: 6 }
    val peakHoursEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.PEAK_HOURS_ENABLED] ?: false }
    val anomalyDetectionEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ANOMALY_DETECTION_ENABLED] ?: false }
    val lastBackupDate: Flow<String> = context.dataStore.data.map { it[Keys.LAST_BACKUP_DATE] ?: "" }
    val isBackgroundServiceEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_BACKGROUND_SERVICE_ENABLED] ?: true }

    suspend fun setDailyLimit(bytes: Long) {
        context.dataStore.edit { it[Keys.DAILY_LIMIT] = bytes }
    }

    suspend fun setWeeklyLimit(bytes: Long) {
        context.dataStore.edit { it[Keys.WEEKLY_LIMIT] = bytes }
    }

    suspend fun setMonthlyLimit(bytes: Long) {
        context.dataStore.edit { it[Keys.MONTHLY_LIMIT] = bytes }
    }

    suspend fun setAlertThreshold(percent: Int) {
        context.dataStore.edit { it[Keys.ALERT_THRESHOLD] = percent }
    }

    suspend fun setBillingCycleDay(day: Int) {
        context.dataStore.edit { it[Keys.BILLING_CYCLE_DAY] = day.coerceIn(1, 28) }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setAppLockPin(pin: String) {
        context.dataStore.edit { it[Keys.APP_LOCK_PIN] = pin }
    }

    suspend fun setRealTimeTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_REAL_TIME_TRACKING_ENABLED] = enabled }
    }

    suspend fun setAutoSpeedTestEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SPEED_TEST_ENABLED] = enabled }
    }

    suspend fun setAutoSpeedTestIntervalHours(hours: Int) {
        context.dataStore.edit { it[Keys.AUTO_SPEED_TEST_INTERVAL_HOURS] = hours }
    }

    suspend fun setPeakHoursEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PEAK_HOURS_ENABLED] = enabled }
    }

    suspend fun setAnomalyDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ANOMALY_DETECTION_ENABLED] = enabled }
    }

    suspend fun setLastBackupDate(date: String) {
        context.dataStore.edit { it[Keys.LAST_BACKUP_DATE] = date }
    }

    suspend fun setBackgroundServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_BACKGROUND_SERVICE_ENABLED] = enabled }
    }

    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
