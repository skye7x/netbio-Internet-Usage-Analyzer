package com.bzygordev.netbio.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import com.bzygordev.netbio.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_USAGE = "usage_alerts"
        const val CHANNEL_CONNECTION = "connection_alerts"
        const val CHANNEL_ACHIEVEMENTS = "achievements"
        const val CHANNEL_BACKGROUND_SERVICE = "background_service"
        const val NOTIFICATION_USAGE = 1001
        const val NOTIFICATION_CONNECTION = 1002
        const val NOTIFICATION_ACHIEVEMENT = 1003
        const val NOTIFICATION_SERVICE = 1004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_USAGE,
                "Usage Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about data usage limits, speed, and signal"
            },
            NotificationChannel(
                CHANNEL_CONNECTION,
                "Connection Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Network disconnect and reconnect notifications"
            },
            NotificationChannel(
                CHANNEL_ACHIEVEMENTS,
                "Achievements",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Achievement unlock notifications"
            },
            NotificationChannel(
                CHANNEL_BACKGROUND_SERVICE,
                "Background Monitoring",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Persistent notification shown while NetBio monitors your connection and data usage in the background"
                setShowBadge(false)
            }
        )
        notificationManager.createNotificationChannels(channels)
    }

    private fun getMainIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getNotificationBuilder(channelId: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(getMainIntent())
    }

    fun showUsageAlert(title: String, message: String, type: String = "usage") {
        val priority = when (type) {
            "limit" -> NotificationCompat.PRIORITY_HIGH
            "speed" -> NotificationCompat.PRIORITY_DEFAULT
            "signal" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
        val notification = getNotificationBuilder(CHANNEL_USAGE)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .build()

        notificationManager.notify(
            NOTIFICATION_USAGE + System.currentTimeMillis().toInt(),
            notification
        )
    }

    fun showConnectionAlert(title: String, message: String) {
        val notification = getNotificationBuilder(CHANNEL_CONNECTION)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(
            NOTIFICATION_CONNECTION + System.currentTimeMillis().toInt(),
            notification
        )
    }

    fun showAchievement(title: String, message: String) {
        val notification = getNotificationBuilder(CHANNEL_ACHIEVEMENTS)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            NOTIFICATION_ACHIEVEMENT + System.currentTimeMillis().toInt(),
            notification
        )
    }

    fun showLimitExceededNotification(used: Long, limit: Long, type: String) {
        val percentage = if (limit > 0) (used.toFloat() / limit) * 100f else 0f
        val usedStr = formatBytes(used)
        val limitStr = formatBytes(limit)
        val title = "${type.replaceFirstChar { it.uppercase() }} Limit Exceeded"
        val message = "You've used $usedStr of $limitStr (${String.format("%.0f", percentage)}%)"
        showUsageAlert(title, message, type = "limit")
    }

    fun showLowSignalNotification(signalStrength: Int) {
        val title = "Low WiFi Signal"
        val message = "WiFi signal strength is $signalStrength% - consider moving closer to the router"
        showUsageAlert(title, message, type = "signal")
    }

    fun showHighPingNotification(ping: Double) {
        val title = "High Latency Detected"
        val message = "Current ping is ${String.format("%.0f", ping)}ms - connection may be unstable"
        showUsageAlert(title, message, type = "speed")
    }

    fun showLowSpeedNotification(speed: Double) {
        val title = "Low Connection Speed"
        val message = "Current speed is ${String.format("%.1f", speed)} Mbps - below expected threshold"
        showUsageAlert(title, message, type = "speed")
    }

    fun showLimitWarning(type: String, currentBytes: Long, limitBytes: Long, percentage: Float) {
        val currentStr = formatBytes(currentBytes)
        val limitStr = formatBytes(limitBytes)
        val message = "${type.replaceFirstChar { it.uppercase() }} limit: " +
                "$currentStr / $limitStr (${String.format("%.0f", percentage)}%)"
        showUsageAlert(
            title = "${type.replaceFirstChar { it.uppercase() }} Limit Warning",
            message = message,
            type = "limit"
        )
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }

    fun buildBackgroundServiceNotification(): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_BACKGROUND_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("NetBio is monitoring your connection")
            .setContentText("Tracking data usage and network alerts in the background")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(getMainIntent())
            .build()
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }
}
