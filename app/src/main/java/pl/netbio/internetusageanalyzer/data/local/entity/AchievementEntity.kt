package pl.netbio.internetusageanalyzer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val unlockedAt: Long = 0L,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f,
    val maxProgress: Float = 100f
)
