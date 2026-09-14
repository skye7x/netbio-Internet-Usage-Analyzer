package pl.netbio.internetusageanalyzer.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.netbio.internetusageanalyzer.data.local.dao.*
import pl.netbio.internetusageanalyzer.data.local.entity.*

@Database(
    entities = [
        DataUsageEntity::class,
        SpeedTestResultEntity::class,
        DiagnosticEntity::class,
        AlertEntity::class,
        UsageLimitEntity::class,
        AchievementEntity::class,
        NetworkInfoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NetBioDatabase : RoomDatabase() {
    abstract fun dataUsageDao(): DataUsageDao
    abstract fun speedTestResultDao(): SpeedTestResultDao
    abstract fun diagnosticDao(): DiagnosticDao
    abstract fun alertDao(): AlertDao
    abstract fun usageLimitDao(): UsageLimitDao
    abstract fun achievementDao(): AchievementDao
    abstract fun networkInfoDao(): NetworkInfoDao

    companion object {
        const val DATABASE_NAME = "netbio_database"
    }
}
