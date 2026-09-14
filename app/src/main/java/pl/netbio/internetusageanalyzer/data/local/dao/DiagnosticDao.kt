package pl.netbio.internetusageanalyzer.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.entity.DiagnosticEntity

@Dao
interface DiagnosticDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diagnostic: DiagnosticEntity): Long

    @Query("SELECT * FROM diagnostics ORDER BY timestamp DESC")
    fun getAllDiagnostics(): Flow<List<DiagnosticEntity>>

    @Query("SELECT * FROM diagnostics WHERE type = :type ORDER BY timestamp DESC")
    fun getDiagnosticsByType(type: String): Flow<List<DiagnosticEntity>>

    @Query("SELECT * FROM diagnostics ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDiagnostics(limit: Int): Flow<List<DiagnosticEntity>>
}
