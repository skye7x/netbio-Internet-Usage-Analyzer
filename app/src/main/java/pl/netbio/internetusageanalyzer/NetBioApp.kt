package pl.netbio.internetusageanalyzer

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import pl.netbio.internetusageanalyzer.service.UsageWorker
import javax.inject.Inject

@HiltAndroidApp
class NetBioApp : Application(), Configuration.Provider {

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
