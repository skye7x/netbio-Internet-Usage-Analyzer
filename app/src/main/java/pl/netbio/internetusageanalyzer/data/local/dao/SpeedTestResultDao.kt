package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.SpeedTestResultEntity

@Dao
interface SpeedTestResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: SpeedTestResultEntity)

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SpeedTestResultEntity>>

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<SpeedTestResultEntity>>

    @Query("SELECT * FROM speed_test_results WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<SpeedTestResultEntity>>

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<SpeedTestResultEntity?>

    @Query("DELETE FROM speed_test_results WHERE timestamp < :cutoffDate")
    suspend fun deleteOld(cutoffDate: Long)
}
