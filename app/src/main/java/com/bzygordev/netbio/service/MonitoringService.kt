package com.bzygordev.netbio.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.bzygordev.netbio.data.local.entity.AlertEntity
import com.bzygordev.netbio.data.repository.AlertRepository
import com.bzygordev.netbio.data.repository.DataUsageRepository
import com.bzygordev.netbio.util.DataUsagePreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Keeps the network and data-usage monitors alive while the app is not in the
 * foreground, so alerts, usage totals, and connection-loss notifications keep
 * working even after the user leaves the app. Runs as a foreground service with
 * a persistent, low-priority notification (required by Android for long-running
 * background work) and is backed up by [UsageWorker] for periodic recording even
 * if the service itself gets stopped by the system.
 */
@AndroidEntryPoint
class MonitoringService : Service() {

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var dataUsageMonitor: DataUsageMonitor
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var dataUsageRepository: DataUsageRepository
    @Inject lateinit var alertRepository: AlertRepository
    @Inject lateinit var preferences: DataUsagePreferences

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var persistJob: Job? = null
    private var connectionWatchJob: Job? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate() {
        super.onCreate()
        startForeground(NotificationHelper.NOTIFICATION_SERVICE, buildNotification())

        networkMonitor.startMonitoring()
        dataUsageMonitor.startTracking()

        watchConnectionLoss()
        persistUsagePeriodically()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: if the system kills this service under memory pressure,
        // it will attempt to recreate it once resources are available again.
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        networkMonitor.stopMonitoring()
        dataUsageMonitor.stopTracking()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun watchConnectionLoss() {
        connectionWatchJob = serviceScope.launch {
            networkMonitor.connectionLost.collect { lost ->
                if (lost) {
                    notificationHelper.showConnectionAlert(
                        "Connection Lost",
                        "NetBio detected you're offline. Monitoring will resume automatically."
                    )
                }
            }
        }
    }

    private fun persistUsagePeriodically() {
        persistJob = serviceScope.launch {
            while (true) {
                try {
                    val rx = dataUsageMonitor.getCurrentRxBytes()
                    val tx = dataUsageMonitor.getCurrentTxBytes()
                    val today = dateFormat.format(Date())
                    dataUsageRepository.insertOrUpdate(
                        date = today,
                        networkType = networkMonitor.networkType.value,
                        bytesUsed = rx + tx,
                        rxBytes = rx,
                        txBytes = tx
                    )

                    val dailyLimit = preferences.dailyLimit.first()
                    val threshold = preferences.alertThreshold.first()
                    if (dailyLimit > 0) {
                        val percentage = (dataUsageMonitor.todayTotal.value.toFloat() / dailyLimit) * 100f
                        if (percentage >= threshold) {
                            notificationHelper.showLimitExceededNotification(
                                dataUsageMonitor.todayTotal.value, dailyLimit, "daily"
                            )
                            alertRepository.insert(
                                AlertEntity(
                                    timestamp = System.currentTimeMillis(),
                                    type = "usage_limit",
                                    title = "Daily Limit Exceeded",
                                    message = "You've used ${percentage.toInt()}% of your daily limit",
                                    value = percentage.toDouble(),
                                    threshold = threshold.toDouble()
                                )
                            )
                        }
                    }
                } catch (_: Exception) {
                    // Keep the service alive even if a single sampling cycle fails.
                }
                delay(60_000L)
            }
        }
    }

    private fun buildNotification() = notificationHelper.buildBackgroundServiceNotification()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, MonitoringService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitoringService::class.java))
        }
    }
}
