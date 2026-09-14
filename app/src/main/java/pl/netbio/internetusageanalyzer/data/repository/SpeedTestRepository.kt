package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.netbio.internetusageanalyzer.data.local.dao.SpeedTestResultDao
import pl.netbio.internetusageanalyzer.data.local.entity.SpeedTestResultEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedTestRepository @Inject constructor(
    private val speedTestResultDao: SpeedTestResultDao
) {

    suspend fun insert(result: SpeedTestResultEntity) {
        speedTestResultDao.insert(result)
    }

    fun getAll(): Flow<List<SpeedTestResultEntity>> = speedTestResultDao.getAll()

    fun getRecent(limit: Int): Flow<List<SpeedTestResultEntity>> =
        speedTestResultDao.getRecent(limit)

    fun getLatest(): Flow<SpeedTestResultEntity?> = speedTestResultDao.getLatest()

    fun getAverageSpeeds(): Flow<Triple<Double, Double, Double>> {
        return speedTestResultDao.getAll().map { list ->
            if (list.isEmpty()) {
                Triple(0.0, 0.0, 0.0)
            } else {
                val avgDownload = list.map { it.downloadSpeed }.average()
                val avgUpload = list.map { it.uploadSpeed }.average()
                val avgPing = list.map { it.ping }.average()
                Triple(avgDownload, avgUpload, avgPing)
            }
        }
    }

    suspend fun deleteOld(cutoffDate: Long) {
        speedTestResultDao.deleteOld(cutoffDate)
    }
}
