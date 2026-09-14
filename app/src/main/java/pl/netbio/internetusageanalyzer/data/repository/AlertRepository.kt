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
    fun getAllAlerts(): Flow<List<AlertEntity>> = alertDao.getAllAlerts()

    fun getUnreadAlerts(): Flow<List<AlertEntity>> = alertDao.getUnreadAlerts()

    fun getUnreadCount(): Flow<Int> = alertDao.getUnreadCount()

    fun getAlertsByType(type: String): Flow<List<AlertEntity>> = alertDao.getAlertsByType(type)

    suspend fun insertAlert(alert: AlertEntity): Long = alertDao.insert(alert)

    suspend fun markAsRead(id: Long) = alertDao.markAsRead(id)

    suspend fun markAllAsRead() = alertDao.markAllAsRead()

    suspend fun deleteAll() = alertDao.deleteAll()
}
