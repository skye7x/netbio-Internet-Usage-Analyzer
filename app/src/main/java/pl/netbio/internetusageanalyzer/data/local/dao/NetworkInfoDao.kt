package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.NetworkInfoEntity

@Dao
interface NetworkInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(info: NetworkInfoEntity): Long

    @Query("SELECT * FROM network_info ORDER BY timestamp DESC LIMIT 1")
    fun getLatestNetworkInfo(): Flow<NetworkInfoEntity?>

    @Query("SELECT * FROM network_info WHERE networkType = :type ORDER BY timestamp DESC")
    fun getNetworkInfoByType(type: String): Flow<List<NetworkInfoEntity>>

    @Query("SELECT DISTINCT networkName FROM network_info")
    fun getAllNetworkNames(): Flow<List<String>>

    @Query("SELECT * FROM network_info WHERE networkName = :name ORDER BY timestamp DESC")
    fun getNetworkHistory(name: String): Flow<List<NetworkInfoEntity>>
}
