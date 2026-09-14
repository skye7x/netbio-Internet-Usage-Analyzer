package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_test_results")
data class SpeedTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val ping: Double,
    val jitter: Double,
    val packetLoss: Double,
    val serverName: String = "",
    val networkType: String = ""
)
