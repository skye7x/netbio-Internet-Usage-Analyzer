package pl.netbio.internetusageanalyzer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import pl.netbio.internetusageanalyzer.service.NetworkMonitor
import pl.netbio.internetusageanalyzer.service.DataUsageMonitor
import pl.netbio.internetusageanalyzer.service.UsageWorker

class BootReceiver : BroadcastReceiver() {

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun networkMonitor(): NetworkMonitor
        fun dataUsageMonitor(): DataUsageMonitor
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            UsageWorker.scheduleUsageRecording(context)

            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    BootEntryPoint::class.java
                )
                entryPoint.networkMonitor().startMonitoring()
                entryPoint.dataUsageMonitor().startTracking()
            } catch (_: Exception) {
            }
        }
    }
}
