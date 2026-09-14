package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "usage_limit",
    val title: String = "",
    val message: String = "",
    val severity: String = "info",
    val isRead: Boolean = false,
    val percentage: Float = 0f,
    val limitBytes: Long = 0,
    val currentBytes: Long = 0
)
