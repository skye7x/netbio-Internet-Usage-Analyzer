package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.DiagnosticEntity

@Dao
interface DiagnosticDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DiagnosticEntity)

    @Query("SELECT * FROM diagnostics ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DiagnosticEntity>>

    @Query("SELECT * FROM diagnostics WHERE testType = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<DiagnosticEntity>>

    @Query("SELECT * FROM diagnostics ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<DiagnosticEntity?>
}
