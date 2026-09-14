package com.bzygordev.netbio.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.bzygordev.netbio.util.DataUsagePreferences

class BootReceiver : BroadcastReceiver() {

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun preferences(): DataUsagePreferences
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            UsageWorker.scheduleUsageRecording(context)

            val pendingResult = goAsync()
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    BootEntryPoint::class.java
                )
                val enabled = try {
                    runBlocking { entryPoint.preferences().isBackgroundServiceEnabled.first() }
                } catch (_: Exception) {
                    true
                }
                if (enabled) {
                    val serviceIntent = Intent(context, MonitoringService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
