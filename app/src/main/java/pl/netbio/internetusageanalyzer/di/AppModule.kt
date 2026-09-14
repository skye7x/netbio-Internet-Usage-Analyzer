package pl.netbio.internetusageanalyzer.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.netbio.internetusageanalyzer.data.local.dao.AchievementDao
import pl.netbio.internetusageanalyzer.data.local.dao.AlertDao
import pl.netbio.internetusageanalyzer.data.local.dao.DataUsageDao
import pl.netbio.internetusageanalyzer.data.local.dao.DiagnosticDao
import pl.netbio.internetusageanalyzer.data.local.dao.NetworkInfoDao
import pl.netbio.internetusageanalyzer.data.local.dao.SpeedTestResultDao
import pl.netbio.internetusageanalyzer.data.local.dao.UsageLimitDao
import pl.netbio.internetusageanalyzer.data.local.database.NetBioDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NetBioDatabase {
        return Room.databaseBuilder(
            context,
            NetBioDatabase::class.java,
            NetBioDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDataUsageDao(db: NetBioDatabase): DataUsageDao = db.dataUsageDao()

    @Provides
    fun provideSpeedTestResultDao(db: NetBioDatabase): SpeedTestResultDao = db.speedTestResultDao()

    @Provides
    fun provideDiagnosticDao(db: NetBioDatabase): DiagnosticDao = db.diagnosticDao()

    @Provides
    fun provideAlertDao(db: NetBioDatabase): AlertDao = db.alertDao()

    @Provides
    fun provideUsageLimitDao(db: NetBioDatabase): UsageLimitDao = db.usageLimitDao()

    @Provides
    fun provideAchievementDao(db: NetBioDatabase): AchievementDao = db.achievementDao()

    @Provides
    fun provideNetworkInfoDao(db: NetBioDatabase): NetworkInfoDao = db.networkInfoDao()
}
