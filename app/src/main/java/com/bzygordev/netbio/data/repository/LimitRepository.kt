package com.bzygordev.netbio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.bzygordev.netbio.data.local.dao.UsageLimitDao
import com.bzygordev.netbio.data.local.entity.UsageLimitEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LimitRepository @Inject constructor(
    private val usageLimitDao: UsageLimitDao
) {

    suspend fun insertOrUpdate(limit: UsageLimitEntity) {
        usageLimitDao.insertOrUpdate(limit)
    }

    fun getAll(): Flow<List<UsageLimitEntity>> = usageLimitDao.getAll()

    fun getDailyLimit(): Flow<UsageLimitEntity?> = usageLimitDao.getByType("daily")

    fun getWeeklyLimit(): Flow<UsageLimitEntity?> = usageLimitDao.getByType("weekly")

    fun getMonthlyLimit(): Flow<UsageLimitEntity?> = usageLimitDao.getByType("monthly")

    suspend fun checkLimits(
        todayUsage: Long,
        weekUsage: Long,
        monthUsage: Long
    ): List<Pair<UsageLimitEntity, Float>> {
        val limits = usageLimitDao.getAll().first().filter { it.isEnabled }
        val results = mutableListOf<Pair<UsageLimitEntity, Float>>()

        for (limit in limits) {
            val usage = when (limit.type) {
                "daily" -> todayUsage
                "weekly" -> weekUsage
                "monthly" -> monthUsage
                else -> 0L
            }
            val percentage = if (limit.limitBytes > 0) {
                (usage.toFloat() / limit.limitBytes.toFloat()) * 100f
            } else {
                0f
            }
            results.add(limit to percentage)
        }

        return results
    }

    suspend fun deleteById(id: Long) {
        usageLimitDao.deleteById(id)
    }
}
