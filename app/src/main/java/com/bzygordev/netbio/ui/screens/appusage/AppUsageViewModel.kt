package com.bzygordev.netbio.ui.screens.appusage

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val usageBytes: Long,
    val usagePercent: Float,
    val icon: Drawable?
)

data class AppUsageUiState(
    val appUsageList: List<AppUsageItem> = emptyList(),
    val totalUsage: Long = 0L,
    val isLoading: Boolean = false
)

@HiltViewModel
class AppUsageViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUsageUiState())
    val uiState: StateFlow<AppUsageUiState> = _uiState.asStateFlow()

    private val networkStatsManager: NetworkStatsManager by lazy {
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    }

    private val packageManager: PackageManager by lazy {
        context.packageManager
    }

    init {
        loadAppUsage()
    }

    fun loadAppUsage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val startTime = calendar.timeInMillis
                    val endTime = System.currentTimeMillis()

                    val appUsageMap = mutableMapOf<String, Long>()

                    try {
                        for (networkType in listOf(
                            android.net.ConnectivityManager.TYPE_WIFI,
                            android.net.ConnectivityManager.TYPE_MOBILE
                        )) {
                            @Suppress("MissingPermission")
                            val stats = networkStatsManager.querySummary(
                                networkType,
                                null,
                                startTime,
                                endTime
                            )
                            val bucket = NetworkStats.Bucket()
                            while (stats.hasNextBucket()) {
                                stats.getNextBucket(bucket)
                                val uid = bucket.uid
                                val rxBytes = bucket.rxBytes
                                val txBytes = bucket.txBytes
                                val totalBytes = rxBytes + txBytes

                                if (totalBytes > 0) {
                                    val packageName = getPackageNameForUid(uid)
                                    if (packageName != null) {
                                        appUsageMap[packageName] = (appUsageMap[packageName] ?: 0L) + totalBytes
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val totalUsage = appUsageMap.values.sum()
                    val appUsageItems = appUsageMap.map { (packageName, usageBytes) ->
                        val appName = getAppName(packageName)
                        val icon = getAppIcon(packageName)
                        val usagePercent = if (totalUsage > 0) (usageBytes.toFloat() / totalUsage.toFloat()) * 100f else 0f
                        AppUsageItem(
                            packageName = packageName,
                            appName = appName,
                            usageBytes = usageBytes,
                            usagePercent = usagePercent,
                            icon = icon
                        )
                    }.sortedByDescending { it.usageBytes }

                    Pair(appUsageItems, totalUsage)
                }

                _uiState.update {
                    it.copy(
                        appUsageList = result.first,
                        totalUsage = result.second,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun getPackageNameForUid(uid: Int): String? {
        return try {
            packageManager.getNameForUid(uid)
        } catch (e: Exception) {
            null
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun getAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            .coerceIn(0, units.size - 1)
        return String.format(
            java.util.Locale.US,
            "%.1f %s",
            bytes / Math.pow(1024.0, digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun getUsagePercent(usageBytes: Long): Float {
        val totalUsage = _uiState.value.totalUsage
        return if (totalUsage > 0) (usageBytes.toFloat() / totalUsage.toFloat()) * 100f else 0f
    }
}
