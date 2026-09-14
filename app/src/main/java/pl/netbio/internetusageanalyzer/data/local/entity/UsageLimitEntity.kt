package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_limits")
data class UsageLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "daily",
    val limitBytes: Long = 0,
    val warningPercentage: Float = 80f,
    val alertPercentage: Float = 95f,
    val isEnabled: Boolean = true,
    val resetDay: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
