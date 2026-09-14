package com.bzygordev.netbio.data.repository

import kotlinx.coroutines.flow.Flow
import com.bzygordev.netbio.data.local.dao.DiagnosticDao
import com.bzygordev.netbio.data.local.entity.DiagnosticEntity
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
