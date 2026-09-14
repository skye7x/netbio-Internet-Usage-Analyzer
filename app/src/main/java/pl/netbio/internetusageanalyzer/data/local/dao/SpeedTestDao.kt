package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.SpeedTestEntity

@Dao
interface SpeedTestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(test: SpeedTestEntity): Long

    @Delete
    suspend fun delete(test: SpeedTestEntity)

    @Query("DELETE FROM speed_tests")
    suspend fun deleteAll()

    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC")
    fun getAllTests(): Flow<List<SpeedTestEntity>>

    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTests(limit: Int): Flow<List<SpeedTestEntity>>

    @Query("SELECT AVG(downloadSpeed) FROM speed_tests WHERE timestamp >= :start AND timestamp <= :end")
    fun getAverageDownload(start: Long, end: Long): Flow<Double?>

    @Query("SELECT AVG(uploadSpeed) FROM speed_tests WHERE timestamp >= :start AND timestamp <= :end")
    fun getAverageUpload(start: Long, end: Long): Flow<Double?>

    @Query("SELECT AVG(ping) FROM speed_tests WHERE timestamp >= :start AND timestamp <= :end")
    fun getAveragePing(start: Long, end: Long): Flow<Double?>

    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC LIMIT 1")
    fun getLatestTest(): Flow<SpeedTestEntity?>

    @Query("SELECT MAX(downloadSpeed) FROM speed_tests")
    fun getMaxDownload(): Flow<Double?>

    @Query("SELECT MIN(downloadSpeed) FROM speed_tests WHERE downloadSpeed > 0")
    fun getMinDownload(): Flow<Double?>
}
