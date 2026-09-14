package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.dao.AchievementDao
import pl.netbio.internetusageanalyzer.data.local.entity.AchievementEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {

    suspend fun insertOrUpdate(achievement: AchievementEntity) {
        achievementDao.insertOrUpdate(achievement)
    }

    fun getAll(): Flow<List<AchievementEntity>> = achievementDao.getAll()

    fun getById(id: String): Flow<AchievementEntity?> = achievementDao.getById(id)

    fun getUnlocked(): Flow<List<AchievementEntity>> = achievementDao.getUnlocked()
}
