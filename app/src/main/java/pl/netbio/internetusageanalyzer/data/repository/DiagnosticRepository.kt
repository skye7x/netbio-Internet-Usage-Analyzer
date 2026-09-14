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

    suspend fun insert(entity: DiagnosticEntity) {
        diagnosticDao.insert(entity)
    }

    fun getAll(): Flow<List<DiagnosticEntity>> = diagnosticDao.getAll()

    fun getByType(type: String): Flow<List<DiagnosticEntity>> = diagnosticDao.getByType(type)

    fun getLatest(): Flow<DiagnosticEntity?> = diagnosticDao.getLatest()
}
