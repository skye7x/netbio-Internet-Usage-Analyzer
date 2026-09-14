package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.DataUsageEntity

@Dao
interface DataUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usage: DataUsageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usages: List<DataUsageEntity>)

    @Update
    suspend fun update(usage: DataUsageEntity)

    @Delete
    suspend fun delete(usage: DataUsageEntity)

    @Query("DELETE FROM data_usage")
    suspend fun deleteAll()

    @Query("SELECT * FROM data_usage ORDER BY timestamp DESC")
    fun getAllUsage(): Flow<List<DataUsageEntity>>

    @Query("SELECT * FROM data_usage WHERE date = :date ORDER BY timestamp DESC")
    fun getUsageByDate(date: String): Flow<List<DataUsageEntity>>

    @Query("SELECT * FROM data_usage WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp")
    fun getUsageBetween(start: Long, end: Long): Flow<List<DataUsageEntity>>

    @Query("SELECT SUM(totalBytes) FROM data_usage WHERE date = :date")
    fun getTotalUsageByDate(date: String): Flow<Long?>

    @Query("SELECT SUM(wifiBytes) FROM data_usage WHERE date = :date")
    fun getWifiUsageByDate(date: String): Flow<Long?>

    @Query("SELECT SUM(mobileBytes) FROM data_usage WHERE date = :date")
    fun getMobileUsageByDate(date: String): Flow<Long?>

    @Query("SELECT SUM(totalBytes) FROM data_usage WHERE timestamp >= :start AND timestamp <= :end")
    fun getTotalUsageBetween(start: Long, end: Long): Flow<Long?>

    @Query("SELECT SUM(wifiBytes) FROM data_usage WHERE timestamp >= :start AND timestamp <= :end")
    fun getWifiUsageBetween(start: Long, end: Long): Flow<Long?>

    @Query("SELECT SUM(mobileBytes) FROM data_usage WHERE timestamp >= :start AND timestamp <= :end")
    fun getMobileUsageBetween(start: Long, end: Long): Flow<Long?>

    @Query("SELECT * FROM data_usage WHERE date = :date AND hour = :hour ORDER BY timestamp")
    fun getUsageByHour(date: String, hour: Int): Flow<List<DataUsageEntity>>

    @Query("SELECT date, SUM(totalBytes) as total FROM data_usage GROUP BY date ORDER BY date DESC LIMIT :limit")
    fun getDailySummaries(limit: Int): Flow<List<DailySummary>>

    @Query("SELECT packageName, SUM(totalBytes) as total FROM data_usage WHERE timestamp >= :start AND timestamp <= :end GROUP BY packageName ORDER BY total DESC")
    fun getAppUsageBetween(start: Long, end: Long): Flow<List<AppUsageSummary>>

    @Query("SELECT SUM(totalBytes) FROM data_usage WHERE timestamp >= :start AND timestamp <= :end AND networkType = :networkType")
    fun getUsageByNetworkType(start: Long, end: Long, networkType: String): Flow<Long?>

    @Query("SELECT * FROM data_usage WHERE date = :date ORDER BY hour ASC")
    fun getHourlyUsage(date: String): Flow<List<DataUsageEntity>>
}

data class DailySummary(
    val date: String,
    val total: Long
)

data class AppUsageSummary(
    val packageName: String,
    val total: Long
)
