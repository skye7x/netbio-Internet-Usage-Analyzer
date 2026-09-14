package com.bzygordev.netbio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.bzygordev.netbio.service.DataUsageMonitor
import com.bzygordev.netbio.service.MonitoringService
import com.bzygordev.netbio.service.NetworkMonitor
import com.bzygordev.netbio.ui.theme.NetBioTheme
import com.bzygordev.netbio.util.DataUsagePreferences
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var dataUsageMonitor: DataUsageMonitor

    @Inject
    lateinit var preferences: DataUsagePreferences

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            launchBackgroundServiceIfEnabled()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // These give the UI live data while the app is open; the foreground
        // MonitoringService takes over once the app is backgrounded.
        networkMonitor.startMonitoring()
        dataUsageMonitor.startTracking()

        ensureNotificationPermissionThenStartService()

        setContent {
            NetBioTheme {
                NetBioApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.stopMonitoring()
        dataUsageMonitor.stopTracking()
    }

    private fun ensureNotificationPermissionThenStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                launchBackgroundServiceIfEnabled()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            launchBackgroundServiceIfEnabled()
        }
    }

    private fun launchBackgroundServiceIfEnabled() {
        lifecycleScope.launch {
            val enabled = try {
                preferences.isBackgroundServiceEnabled.first()
            } catch (_: Exception) {
                true
            }
            if (enabled) {
                MonitoringService.start(this@MainActivity)
            }
        }
    }
}
