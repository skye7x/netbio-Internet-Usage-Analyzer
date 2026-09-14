package pl.netbio.internetusageanalyzer.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pl.netbio.internetusageanalyzer.data.local.entity.DataUsageEntity
import pl.netbio.internetusageanalyzer.data.local.entity.AlertEntity
import pl.netbio.internetusageanalyzer.data.repository.UsageRepository
import pl.netbio.internetusageanalyzer.data.repository.AlertRepository
import pl.netbio.internetusageanalyzer.data.repository.LimitRepository
import pl.netbio.internetusageanalyzer.util.DataUsagePreferences
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageRepository: UsageRepository,
    private val alertRepository: AlertRepository,
    private val limitRepository: LimitRepository,
    private val dataUsageMonitor: DataUsageMonitor,
    private val notificationHelper: NotificationHelper,
    private val preferences: DataUsagePreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            recordUsage()
            checkLimits()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun recordUsage() {
        val totalRx = dataUsageMonitor.getCurrentRxBytes()
        val totalTx = dataUsageMonitor.getCurrentTxBytes()
        val cal = Calendar.getInstance()
        val date = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val networkType = "wifi"

        val usage = DataUsageEntity(
            timestamp = System.currentTimeMillis(),
            totalBytes = totalRx + totalTx,
            wifiBytes = if (networkType == "wifi") totalRx + totalTx else 0,
            mobileBytes = if (networkType == "mobile") totalRx + totalTx else 0,
            rxBytes = totalRx,
            txBytes = totalTx,
            packageName = "system",
            networkType = networkType,
            date = date,
            hour = hour,
            dayOfWeek = dayOfWeek,
            weekOfYear = weekOfYear,
            month = month,
            year = year
        )
        usageRepository.insertUsage(usage)
    }

    private suspend fun checkLimits() {
        val usage = preferences.getCurrentDayUsage()
        val dailyLimit = preferences.getDailyLimit()

        if (dailyLimit > 0) {
            val percentage = (usage.toFloat() / dailyLimit) * 100f
            val warnPercent = preferences.getWarningPercentage()
            val alertPercent = preferences.getAlertPercentage()

            if (percentage >= alertPercent) {
                notificationHelper.showLimitWarning("daily", usage, dailyLimit, percentage)
                alertRepository.insertAlert(
                    AlertEntity(
                        type = "usage_limit",
                        title = "Daily Limit Exceeded",
                        message = "You've used ${String.format("%.0f", percentage)}% of your daily limit",
                        severity = "critical",
                        percentage = percentage,
                        limitBytes = dailyLimit,
                        currentBytes = usage
                    )
                )
            } else if (percentage >= warnPercent) {
                notificationHelper.showLimitWarning("daily", usage, dailyLimit, percentage)
            }
        }
    }

    companion object {
        private const val WORK_NAME_USAGE = "usage_recording"
        private const val WORK_NAME_SCHEDULED_SPEEDTEST = "scheduled_speedtest"

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