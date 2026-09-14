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
    fun getAllAchievements(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    fun getUnlockedAchievements(): Flow<List<AchievementEntity>> = achievementDao.getUnlockedAchievements()

    fun getUnlockedCount(): Flow<Int> = achievementDao.getUnlockedCount()

    suspend fun insertAchievement(achievement: AchievementEntity): Long = achievementDao.insert(achievement)

    suspend fun updateAchievement(achievement: AchievementEntity) = achievementDao.update(achievement)
}
