package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.UsageLimitEntity

@Dao
interface UsageLimitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(limit: UsageLimitEntity)

    @Query("SELECT * FROM usage_limits")
    fun getAll(): Flow<List<UsageLimitEntity>>

    @Query("SELECT * FROM usage_limits WHERE type = :type LIMIT 1")
    fun getByType(type: String): Flow<UsageLimitEntity?>

    @Query("DELETE FROM usage_limits WHERE id = :id")
    suspend fun deleteById(id: Long)
}
