package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.NetworkInfoEntity

@Dao
interface NetworkInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: NetworkInfoEntity)

    @Query("SELECT * FROM network_info ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NetworkInfoEntity>>

    @Query("SELECT * FROM network_info WHERE ssid = :ssid ORDER BY timestamp DESC")
    fun getBySsid(ssid: String): Flow<List<NetworkInfoEntity>>

    @Query("SELECT DISTINCT ssid FROM network_info ORDER BY ssid ASC")
    fun getDistinctSsids(): Flow<List<String>>

    @Query("SELECT * FROM network_info ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<NetworkInfoEntity?>

    @Query("DELETE FROM network_info WHERE timestamp < :cutoffDate")
    suspend fun deleteOld(cutoffDate: Long)
}
