package com.bzygordev.netbio.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import com.bzygordev.netbio.data.local.entity.AlertEntity
import com.bzygordev.netbio.data.local.entity.DataUsageEntity
import com.bzygordev.netbio.data.repository.DataUsageRepository
import com.bzygordev.netbio.data.repository.AlertRepository
import com.bzygordev.netbio.util.DataUsagePreferences
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataUsageRepository: DataUsageRepository,
    private val alertRepository: AlertRepository,
    private val dataUsageMonitor: DataUsageMonitor,
    private val notificationHelper: NotificationHelper,
    private val preferences: DataUsagePreferences
) : CoroutineWorker(appContext, workerParams) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun doWork(): Result {
        return try {
            recordUsage()
            checkLimits()
            checkAnomalies()
            autoResetLimits()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun recordUsage() {
        val totalRx = dataUsageMonitor.getCurrentRxBytes()
        val totalTx = dataUsageMonitor.getCurrentTxBytes()
        val today = dateFormat.format(Date())
        val networkType = getNetworkType()

        dataUsageRepository.insertOrUpdate(
            date = today,
            networkType = networkType,
            bytesUsed = totalRx + totalTx,
            rxBytes = totalRx,
            txBytes = totalTx
        )
    }

    private fun getNetworkType(): String {
        return try {
            val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return "other"
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return "other"
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "mobile"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "other"
            }
        } catch (_: Exception) {
            "wifi"
        }
    }

    private suspend fun checkLimits() {
        val todayUsage = dataUsageMonitor.todayTotal.value

        val dailyLimit = try { preferences.dailyLimit.first() } catch (_: Exception) { 0L }
        val alertThreshold = try { preferences.alertThreshold.first() } catch (_: Exception) { 80 }

        if (dailyLimit > 0) {
            val percentage = (todayUsage.toFloat() / dailyLimit) * 100f
            if (percentage >= alertThreshold) {
                notificationHelper.showLimitExceededNotification(todayUsage, dailyLimit, "daily")
                alertRepository.insert(
                    AlertEntity(
                        timestamp = System.currentTimeMillis(),
                        type = "usage_limit",
                        title = "Daily Limit Exceeded",
                        message = "You've used ${String.format("%.0f", percentage)}% of your daily limit",
                        value = percentage.toDouble(),
                        threshold = alertThreshold.toDouble()
                    )
                )
            }
        }

        val weeklyLimit = try { preferences.weeklyLimit.first() } catch (_: Exception) { 0L }
        if (weeklyLimit > 0) {
            val weekUsage = dataUsageRepository.getWeekUsage().first()
            val percentage = (weekUsage.toFloat() / weeklyLimit) * 100f
            if (percentage >= alertThreshold) {
                notificationHelper.showLimitExceededNotification(weekUsage, weeklyLimit, "weekly")
                alertRepository.insert(
                    AlertEntity(
                        timestamp = System.currentTimeMillis(),
                        type = "usage_limit",
                        title = "Weekly Limit Exceeded",
                        message = "You've used ${String.format("%.0f", percentage)}% of your weekly limit",
                        value = percentage.toDouble(),
                        threshold = alertThreshold.toDouble()
                    )
                )
            }
        }

        val monthlyLimit = try { preferences.monthlyLimit.first() } catch (_: Exception) { 0L }
        if (monthlyLimit > 0) {
            val monthUsage = dataUsageRepository.getMonthUsage().first()
            val percentage = (monthUsage.toFloat() / monthlyLimit) * 100f
            if (percentage >= alertThreshold) {
                notificationHelper.showLimitExceededNotification(monthUsage, monthlyLimit, "monthly")
                alertRepository.insert(
                    AlertEntity(
                        timestamp = System.currentTimeMillis(),
                        type = "usage_limit",
                        title = "Monthly Limit Exceeded",
                        message = "You've used ${String.format("%.0f", percentage)}% of your monthly limit",
                        value = percentage.toDouble(),
                        threshold = alertThreshold.toDouble()
                    )
                )
            }
        }
    }

    private suspend fun checkAnomalies() {
        val anomalyEnabled = try { preferences.anomalyDetectionEnabled.first() } catch (_: Exception) { false }
        if (!anomalyEnabled) return

        val anomalyScore = dataUsageMonitor.getAnomalyScore()
        if (anomalyScore > 0.5) {
            alertRepository.insert(
                AlertEntity(
                    timestamp = System.currentTimeMillis(),
                    type = "anomaly",
                    title = "Unusual Data Usage Detected",
                    message = "Current usage is ${String.format("%.0f", anomalyScore * 100)}% above normal",
                    value = anomalyScore,
                    threshold = 0.5
                )
            )
        }
    }

    private suspend fun autoResetLimits() {
        val billingDay = try { preferences.billingCycleDay.first() } catch (_: Exception) { 1 }
        val cal = Calendar.getInstance()
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        if (dayOfMonth == billingDay && hour == 0) {
            dataUsageMonitor.resetDaily()
            alertRepository.insert(
                AlertEntity(
                    timestamp = System.currentTimeMillis(),
                    type = "billing_cycle",
                    title = "Billing Cycle Reset",
                    message = "New billing cycle started. Counters have been reset.",
                    value = 0.0,
                    threshold = 0.0
                )
            )
        }
    }

    companion object {
        private const val WORK_NAME_USAGE = "usage_recording"

        fun scheduleUsageRecording(context: Context) {
            val request = PeriodicWorkRequestBuilder<UsageWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_USAGE,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelUsageRecording(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_USAGE)
        }
    }
}
