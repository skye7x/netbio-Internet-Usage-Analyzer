package pl.netbio.internetusageanalyzer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class SpeedTestEngine @Inject constructor() {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _phase = MutableStateFlow(SpeedTestPhase.IDLE)
    val phase: StateFlow<SpeedTestPhase> = _phase.asStateFlow()

    private val _downloadSpeed = MutableStateFlow(0.0)
    val downloadSpeed: StateFlow<Double> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0.0)
    val uploadSpeed: StateFlow<Double> = _uploadSpeed.asStateFlow()

    private val _ping = MutableStateFlow(0.0)
    val ping: StateFlow<Double> = _ping.asStateFlow()

    private val _jitter = MutableStateFlow(0.0)
    val jitter: StateFlow<Double> = _jitter.asStateFlow()

    private val _packetLoss = MutableStateFlow(0.0)
    val packetLoss: StateFlow<Double> = _packetLoss.asStateFlow()

    private val _currentDownload = MutableStateFlow(0.0)
    val currentDownload: StateFlow<Double> = _currentDownload.asStateFlow()

    private val _currentUpload = MutableStateFlow(0.0)
    val currentUpload: StateFlow<Double> = _currentUpload.asStateFlow()

    private val isCancelled = AtomicBoolean(false)

    private val testUrls = listOf(
        "https://speed.cloudflare.com/__down?bytes=10000000",
        "https://proof.ovh.net/files/10Mb.dat",
        "https://speedtest.tele2.net/10MB.zip"
    )

    private val uploadUrls = listOf(
        "https://speed.cloudflare.com/__up",
        "https://proof.ovh.net/upload.php"
    )

    suspend fun startTest(): SpeedTestResult {
        _isRunning.value = true
        _progress.value = 0f
        _phase.value = SpeedTestPhase.PING
        isCancelled.set(false)

        try {
            // Ping test
            val pingResult = measurePing()
            if (isCancelled.get()) return createEmptyResult()
            _ping.value = pingResult
            _progress.value = 0.15f

            // Jitter test
            val jitterResult = measureJitter()
            if (isCancelled.get()) return createEmptyResult()
            _jitter.value = jitterResult
            _progress.value = 0.25f

            // Download test
            _phase.value = SpeedTestPhase.DOWNLOAD
            val downloadResult = measureDownload()
            if (isCancelled.get()) return createEmptyResult()
            _downloadSpeed.value = downloadResult
            _progress.value = 0.65f

            // Upload test
            _phase.value = SpeedTestPhase.UPLOAD
            val uploadResult = measureUpload()
            if (isCancelled.get()) return createEmptyResult()
            _uploadSpeed.value = uploadResult
            _progress.value = 0.85f

            // Packet loss
            val packetLossResult = measurePacketLoss()
            _packetLoss.value = packetLossResult
            _progress.value = 1.0f
            _phase.value = SpeedTestPhase.COMPLETE

            return SpeedTestResult(
                downloadSpeed = downloadResult,
                uploadSpeed = uploadResult,
                ping = pingResult,
                jitter = jitterResult,
                packetLoss = packetLossResult
            )
        } catch (e: Exception) {
            _phase.value = SpeedTestPhase.ERROR
            return createEmptyResult()
        } finally {
            _isRunning.value = false
        }
    }

    fun cancel() {
        isCancelled.set(true)
        _isRunning.value = false
        _phase.value = SpeedTestPhase.IDLE
    }

    private suspend fun measurePing(): Double = withContext(Dispatchers.IO) {
        val pings = mutableListOf<Double>()
        repeat(10) {
            if (isCancelled.get()) return@withContext 0.0
            try {
                val url = URL("https://speed.cloudflare.com/__down?bytes=0")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val start = System.nanoTime()
                conn.connect()
                val end = System.nanoTime()
                pings.add((end - start) / 1_000_000.0)
                conn.disconnect()
            } catch (e: Exception) {
                pings.add(0.0)
            }
            delay(100)
        }
        pings.filter { it > 0 }.average().let { if (it.isNaN()) 0.0 else it }
    }

    private suspend fun measureJitter(): Double = withContext(Dispatchers.IO) {
        val pings = mutableListOf<Double>()
        repeat(20) {
            if (isCancelled.get()) return@withContext 0.0
            try {
                val url = URL("https://speed.cloudflare.com/__down?bytes=0")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                val start = System.nanoTime()
                conn.connect()
                val end = System.nanoTime()
                pings.add((end - start) / 1_000_000.0)
                conn.disconnect()
            } catch (e: Exception) {
                pings.add(0.0)
            }
            delay(50)
        }
        val validPings = pings.filter { it > 0 }
        if (validPings.size < 2) return@withContext 0.0
        val avg = validPings.average()
        validPings.map { kotlin.math.abs(it - avg) }.average()
    }

    private suspend fun measureDownload(): Double = withContext(Dispatchers.IO) {
        var totalBytes = 0L
        val duration = 10_000L
        val startTime = System.currentTimeMillis()
        val speeds = mutableListOf<Double>()

        for (testUrl in testUrls) {
            if (isCancelled.get()) return@withContext 0.0
            try {
                val url = URL(testUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.connect()

                val buffer = ByteArray(8192)
                var bytesRead: Int
                val streamStartTime = System.currentTimeMillis()

                conn.inputStream.use { stream ->
                    while (stream.read(buffer).also { bytesRead = it } != -1) {
                        if (isCancelled.get()) {
                            conn.disconnect()
                            return@withContext 0.0
                        }
                        totalBytes += bytesRead
                        val elapsed = System.currentTimeMillis() - streamStartTime
                        if (elapsed > 0) {
                            val currentSpeed = (totalBytes * 8.0) / (elapsed / 1000.0) / 1_000_000.0
                            _currentDownload.value = currentSpeed
                            speeds.add(currentSpeed)
                        }
                        if (System.currentTimeMillis() - startTime >= duration) break
                    }
                }
                conn.disconnect()
                if (System.currentTimeMillis() - startTime >= duration) break
            } catch (e: Exception) {
                continue
            }
        }

        val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
        if (elapsed <= 0) return@withContext 0.0
        val avgSpeedMbps = (totalBytes * 8.0) / elapsed / 1_000_000.0
        _currentDownload.value = avgSpeedMbps
        avgSpeedMbps
    }

    private suspend fun measureUpload(): Double = withContext(Dispatchers.IO) {
        val dataSize = 5 * 1024 * 1024
        val data = ByteArray(dataSize) { (it % 256).toByte() }
        var totalBytes = 0L
        val duration = 8_000L
        val startTime = System.currentTimeMillis()

        for (testUrl in uploadUrls) {
            if (isCancelled.get()) return@withContext 0.0
            try {
                val url = URL(testUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.connect()

                val os = conn.outputStream
                val chunkSize = 8192
                var offset = 0

                while (offset < data.size) {
                    if (isCancelled.get()) {
                        os.close()
                        conn.disconnect()
                        return@withContext 0.0
                    }
                    val remaining = minOf(chunkSize, data.size - offset)
                    os.write(data, offset, remaining)
                    offset += remaining
                    totalBytes += remaining

                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed > 0) {
                        _currentUpload.value = (totalBytes * 8.0) / (elapsed / 1000.0) / 1_000_000.0
                    }
                    if (System.currentTimeMillis() - startTime >= duration) break
                }

                os.flush()
                conn.responseCode
                os.close()
                conn.disconnect()

                if (System.currentTimeMillis() - startTime >= duration) break
            } catch (e: Exception) {
                continue
            }
        }

        val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
        if (elapsed <= 0) return@withContext 0.0
        (totalBytes * 8.0) / elapsed / 1_000_000.0
    }

    private suspend fun measurePacketLoss(): Double = withContext(Dispatchers.IO) {
        var sent = 0
        var received = 0
        repeat(20) {
            if (isCancelled.get()) return@withContext 0.0
            try {
                val url = URL("https://speed.cloudflare.com/__down?bytes=0")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                sent++
                conn.connect()
                if (conn.responseCode in 200..299) received++
                conn.disconnect()
            } catch (e: Exception) {
                sent++
            }
            delay(200)
        }
        if (sent == 0) return@withContext 0.0
        ((sent - received).toDouble() / sent) * 100.0
    }

    private fun createEmptyResult(): SpeedTestResult {
        return SpeedTestResult(0.0, 0.0, 0.0, 0.0, 0.0)
    }

    fun reset() {
        _downloadSpeed.value = 0.0
        _uploadSpeed.value = 0.0
        _ping.value = 0.0
        _jitter.value = 0.0
        _packetLoss.value = 0.0
        _currentDownload.value = 0.0
        _currentUpload.value = 0.0
        _progress.value = 0f
        _phase.value = SpeedTestPhase.IDLE
    }
}

enum class SpeedTestPhase {
    IDLE, PING, DOWNLOAD, UPLOAD, COMPLETE, ERROR
}

data class SpeedTestResult(
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val ping: Double,
    val jitter: Double,
    val packetLoss: Double
)