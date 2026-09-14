package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_limits")
data class UsageLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val limitBytes: Long,
    val alertThreshold: Int = 80,
    val isEnabled: Boolean = true,
    val lastResetDate: String = ""
)
