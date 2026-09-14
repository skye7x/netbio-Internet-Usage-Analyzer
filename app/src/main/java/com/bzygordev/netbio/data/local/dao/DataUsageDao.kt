package com.bzygordev.netbio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.bzygordev.netbio.data.local.entity.DataUsageEntity

@Dao
interface DataUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dataUsage: DataUsageEntity)

    @Query("SELECT * FROM data_usage ORDER BY date DESC")
    fun getAll(): Flow<List<DataUsageEntity>>

    @Query("SELECT * FROM data_usage WHERE date = :date")
    fun getByDate(date: String): Flow<List<DataUsageEntity>>

    @Query("SELECT * FROM data_usage WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<DataUsageEntity>>

    @Query("SELECT SUM(bytesUsed) FROM data_usage WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalByDateRange(startDate: String, endDate: String): Flow<Long?>

    @Query(
        "SELECT SUM(bytesUsed) FROM data_usage WHERE date BETWEEN :startDate AND :endDate AND networkType = :networkType"
    )
    fun getTotalByNetworkType(
        startDate: String,
        endDate: String,
        networkType: String
    ): Flow<Long?>

    @Query("SELECT * FROM data_usage WHERE date LIKE :yearMonth || '%' ORDER BY date ASC")
    fun getDailyUsageForMonth(yearMonth: String): Flow<List<DataUsageEntity>>

    @Query("DELETE FROM data_usage WHERE date < :cutoffDate")
    suspend fun deleteOldData(cutoffDate: String)

    @Query("SELECT DISTINCT date FROM data_usage ORDER BY date ASC")
    fun getAllDates(): Flow<List<String>>
}
