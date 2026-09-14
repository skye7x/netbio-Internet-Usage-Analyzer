package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_info")
data class NetworkInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val networkName: String = "",
    val networkType: String = "wifi",
    val ssid: String = "",
    val bssid: String = "",
    val frequency: Int = 0,
    val linkSpeed: Int = 0,
    val rssi: Int = 0,
    val ipAddress: String = "",
    val macAddress: String = "",
    val channel: Int = 0,
    val isSecure: Boolean = true
)
