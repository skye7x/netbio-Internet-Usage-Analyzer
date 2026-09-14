package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.UsageLimitEntity

@Dao
interface UsageLimitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(limit: UsageLimitEntity): Long

    @Update
    suspend fun update(limit: UsageLimitEntity)

    @Delete
    suspend fun delete(limit: UsageLimitEntity)

    @Query("SELECT * FROM usage_limits")
    fun getAllLimits(): Flow<List<UsageLimitEntity>>

    @Query("SELECT * FROM usage_limits WHERE type = :type LIMIT 1")
    fun getLimitByType(type: String): Flow<UsageLimitEntity?>

    @Query("SELECT * FROM usage_limits WHERE isEnabled = 1")
    fun getActiveLimits(): Flow<List<UsageLimitEntity>>
}
