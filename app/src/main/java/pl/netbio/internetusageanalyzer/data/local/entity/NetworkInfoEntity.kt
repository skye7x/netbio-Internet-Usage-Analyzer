package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_info")
data class NetworkInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val ssid: String,
    val bssid: String,
    val frequency: Int,
    val rssi: Int,
    val linkSpeed: Int,
    val networkType: String,
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val ping: Double = 0.0
)
