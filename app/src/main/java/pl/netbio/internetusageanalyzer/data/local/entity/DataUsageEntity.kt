package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_usage")
data class DataUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalBytes: Long = 0,
    val wifiBytes: Long = 0,
    val mobileBytes: Long = 0,
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    val packageName: String = "",
    val networkType: String = "wifi",
    val date: String = "",
    val hour: Int = 0,
    val dayOfWeek: Int = 0,
    val weekOfYear: Int = 0,
    val month: Int = 0,
    val year: Int = 0
)
