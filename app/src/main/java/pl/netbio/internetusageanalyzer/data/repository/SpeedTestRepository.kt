package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.dao.SpeedTestDao
import pl.netbio.internetusageanalyzer.data.local.entity.SpeedTestEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedTestRepository @Inject constructor(
    private val speedTestDao: SpeedTestDao
) {
    fun getAllTests(): Flow<List<SpeedTestEntity>> = speedTestDao.getAllTests()

    fun getRecentTests(limit: Int): Flow<List<SpeedTestEntity>> = speedTestDao.getRecentTests(limit)

    fun getAverageDownload(start: Long, end: Long): Flow<Double?> = speedTestDao.getAverageDownload(start, end)

    fun getAverageUpload(start: Long, end: Long): Flow<Double?> = speedTestDao.getAverageUpload(start, end)

    fun getAveragePing(start: Long, end: Long): Flow<Double?> = speedTestDao.getAveragePing(start, end)

    fun getLatestTest(): Flow<SpeedTestEntity?> = speedTestDao.getLatestTest()

    fun getMaxDownload(): Flow<Double?> = speedTestDao.getMaxDownload()

    fun getMinDownload(): Flow<Double?> = speedTestDao.getMinDownload()

    suspend fun insertTest(test: SpeedTestEntity): Long = speedTestDao.insert(test)

    suspend fun deleteAll() = speedTestDao.deleteAll()
}
