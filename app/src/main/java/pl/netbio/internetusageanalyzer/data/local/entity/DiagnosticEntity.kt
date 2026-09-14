package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostics")
data class DiagnosticEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "dns",
    val result: String = "",
    val latency: Double = 0.0,
    val isStable: Boolean = true,
    val packetLoss: Double = 0.0,
    val details: String = ""
)
