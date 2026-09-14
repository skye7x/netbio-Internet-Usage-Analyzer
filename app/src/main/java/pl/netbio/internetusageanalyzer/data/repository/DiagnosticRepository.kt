package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.dao.DiagnosticDao
import pl.netbio.internetusageanalyzer.data.local.entity.DiagnosticEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticRepository @Inject constructor(
    private val diagnosticDao: DiagnosticDao
) {
    fun getAllDiagnostics(): Flow<List<DiagnosticEntity>> = diagnosticDao.getAllDiagnostics()

    fun getDiagnosticsByType(type: String): Flow<List<DiagnosticEntity>> = diagnosticDao.getDiagnosticsByType(type)

    fun getRecentDiagnostics(limit: Int): Flow<List<DiagnosticEntity>> = diagnosticDao.getRecentDiagnostics(limit)

    suspend fun insertDiagnostic(diagnostic: DiagnosticEntity): Long = diagnosticDao.insert(diagnostic)
}
