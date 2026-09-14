package pl.netbio.internetusageanalyzer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pl.netbio.internetusageanalyzer.data.local.dao.*
import pl.netbio.internetusageanalyzer.data.local.entity.*

@Database(
    entities = [
        DataUsageEntity::class,
        SpeedTestEntity::class,
        AlertEntity::class,
        UsageLimitEntity::class,
        NetworkInfoEntity::class,
        DiagnosticEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NetBioDatabase : RoomDatabase() {
    abstract fun dataUsageDao(): DataUsageDao
    abstract fun speedTestDao(): SpeedTestDao
    abstract fun alertDao(): AlertDao
    abstract fun usageLimitDao(): UsageLimitDao
    abstract fun networkInfoDao(): NetworkInfoDao
    abstract fun diagnosticDao(): DiagnosticDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "netbio_database"
    }
}
