package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val value: Double = 0.0,
    val threshold: Double = 0.0
)
