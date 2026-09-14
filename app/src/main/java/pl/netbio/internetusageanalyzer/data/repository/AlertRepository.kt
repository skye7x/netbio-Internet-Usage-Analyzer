package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.dao.AlertDao
import pl.netbio.internetusageanalyzer.data.local.entity.AlertEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val alertDao: AlertDao
) {

    suspend fun insert(alert: AlertEntity) {
        alertDao.insert(alert)
    }

    fun getAll(): Flow<List<AlertEntity>> = alertDao.getAll()

    fun getUnreadCount(): Flow<Int> = alertDao.getUnreadCount()

    fun getUnread(): Flow<List<AlertEntity>> = alertDao.getUnread()

    suspend fun markAsRead(id: Long) {
        alertDao.markAsRead(id)
    }

    suspend fun markAllAsRead() {
        alertDao.markAllAsRead()
    }

    suspend fun deleteOld(cutoffDate: Long) {
        alertDao.deleteOld(cutoffDate)
    }

    suspend fun deleteById(id: Long) {
        alertDao.deleteById(id)
    }
}
