package com.bzygordev.netbio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.bzygordev.netbio.data.local.entity.AchievementEntity

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    fun getById(id: String): Flow<AchievementEntity?>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAt DESC")
    fun getUnlocked(): Flow<List<AchievementEntity>>
}
