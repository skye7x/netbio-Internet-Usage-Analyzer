package com.bzygordev.netbio

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import com.bzygordev.netbio.service.UsageWorker
import javax.inject.Inject

@HiltAndroidApp
class NetBioApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        UsageWorker.scheduleUsageRecording(this)
    }
}
