package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_tests")
data class SpeedTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val ping: Double = 0.0,
    val jitter: Double = 0.0,
    val packetLoss: Double = 0.0,
    val serverName: String = "",
    val serverPing: Double = 0.0,
    val ipAddress: String = "",
    val isp: String = ""
)
