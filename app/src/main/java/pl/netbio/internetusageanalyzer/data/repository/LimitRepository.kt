package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.dao.UsageLimitDao
import pl.netbio.internetusageanalyzer.data.local.entity.UsageLimitEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LimitRepository @Inject constructor(
    private val usageLimitDao: UsageLimitDao
) {
    fun getAllLimits(): Flow<List<UsageLimitEntity>> = usageLimitDao.getAllLimits()

    fun getLimitByType(type: String): Flow<UsageLimitEntity?> = usageLimitDao.getLimitByType(type)

    fun getActiveLimits(): Flow<List<UsageLimitEntity>> = usageLimitDao.getActiveLimits()

    suspend fun insertLimit(limit: UsageLimitEntity): Long = usageLimitDao.insert(limit)

    suspend fun updateLimit(limit: UsageLimitEntity) = usageLimitDao.update(limit)

    suspend fun deleteLimit(limit: UsageLimitEntity) = usageLimitDao.delete(limit)
}
