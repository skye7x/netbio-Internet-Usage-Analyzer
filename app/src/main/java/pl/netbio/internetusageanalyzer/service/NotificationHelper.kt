package pl.netbio.internetusageanalyzer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.netbio.internetusageanalyzer.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_USAGE = "usage_alerts"
        const val CHANNEL_SPEEDTEST = "speedtest_results"
        const val CHANNEL_NETWORK = "network_status"
        const val CHANNEL_WIFI = "wifi_alerts"
        const val NOTIFICATION_USAGE = 1001
        const val NOTIFICATION_SPEEDTEST = 1002
        const val NOTIFICATION_NETWORK = 1003
        const val NOTIFICATION_WIFI = 1004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(CHANNEL_USAGE, "Usage Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications about data usage limits"
            },
            NotificationChannel(CHANNEL_SPEEDTEST, "Speedtest Results", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Speed test completion notifications"
            },
            NotificationChannel(CHANNEL_NETWORK, "Network Status", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Network connection status changes"
            },
            NotificationChannel(CHANNEL_WIFI, "WiFi Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "WiFi signal and connection alerts"
            }
        )
        notificationManager.createNotificationChannels(channels)
    }

    fun showUsageAlert(title: String, message: String, percentage: Float) {
        val channelId = if (percentage >= 95f) CHANNEL_USAGE else CHANNEL_USAGE
        val priority = if (percentage >= 95f) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        notificationManager.notify(NOTIFICATION_USAGE + System.currentTimeMillis().toInt(), notification)
    }

    fun showSpeedTestResult(download: String, upload: String, ping: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SPEEDTEST)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Speed Test Complete")
            .setContentText("↓ $download | ↑ $upload | Ping: $ping")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_SPEEDTEST, notification)
    }

    fun showNetworkAlert(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_NETWORK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_NETWORK, notification)
    }

    fun showWifiAlert(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_WIFI)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_WIFI + System.currentTimeMillis().toInt(), notification)
    }

    fun showLimitWarning(type: String, currentBytes: Long, limitBytes: Long, percentage: Float) {
        val usageFormatter = DataUsageMonitor(context)
        val message = "${type.replaceFirstChar { it.uppercase() }} limit: ${usageFormatter.formatBytes(currentBytes)} / ${usageFormatter.formatBytes(limitBytes)} (${String.format("%.0f", percentage)}%)"
        showUsageAlert(
            title = "⚠️ ${type.replaceFirstChar { it.uppercase() }} Limit Warning",
            message = message,
            percentage = percentage
        )
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }
}