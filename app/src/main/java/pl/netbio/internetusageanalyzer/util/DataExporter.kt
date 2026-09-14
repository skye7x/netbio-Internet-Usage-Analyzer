package pl.netbio.internetusageanalyzer.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
    private val dateFormatReadable = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun exportUsageToCsv(
        data: List<ExportUsageData>,
        filename: String = "usage_${dateFormat.format(Date())}.csv"
    ): File {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, filename)

        FileWriter(file).use { writer ->
            writer.append("Date,Time,Total Bytes,WiFi Bytes,Mobile Bytes,RX Bytes,TX Bytes,Package,Network Type\n")
            data.forEach { entry ->
                writer.append("${entry.date},${dateFormatReadable.format(Date(entry.timestamp))},${entry.totalBytes},${entry.wifiBytes},${entry.mobileBytes},${entry.rxBytes},${entry.txBytes},${entry.packageName},${entry.networkType}\n")
            }
        }
        return file
    }

    fun exportSpeedTestsToCsv(
        data: List<ExportSpeedTestData>,
        filename: String = "speedtests_${dateFormat.format(Date())}.csv"
    ): File {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, filename)

        FileWriter(file).use { writer ->
            writer.append("Date,Time,Download (Mbps),Upload (Mbps),Ping (ms),Jitter (ms),Packet Loss (%),Server\n")
            data.forEach { entry ->
                writer.append("${entry.date},${dateFormatReadable.format(Date(entry.timestamp))},${entry.downloadSpeed},${entry.uploadSpeed},${entry.ping},${entry.jitter},${entry.packetLoss},${entry.serverName}\n")
            }
        }
        return file
    }

    fun exportUsageToJson(
        data: List<ExportUsageData>,
        filename: String = "usage_${dateFormat.format(Date())}.json"
    ): File {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, filename)

        FileWriter(file).use { writer ->
            writer.append("[\n")
            data.forEachIndexed { index, entry ->
                writer.append("  {\n")
                writer.append("    \"date\": \"${entry.date}\",\n")
                writer.append("    \"timestamp\": ${entry.timestamp},\n")
                writer.append("    \"totalBytes\": ${entry.totalBytes},\n")
                writer.append("    \"wifiBytes\": ${entry.wifiBytes},\n")
                writer.append("    \"mobileBytes\": ${entry.mobileBytes},\n")
                writer.append("    \"rxBytes\": ${entry.rxBytes},\n")
                writer.append("    \"txBytes\": ${entry.txBytes},\n")
                writer.append("    \"packageName\": \"${entry.packageName}\",\n")
                writer.append("    \"networkType\": \"${entry.networkType}\"\n")
                writer.append("  }")
                if (index < data.size - 1) writer.append(",")
                writer.append("\n")
            }
            writer.append("]")
        }
        return file
    }

    fun exportSpeedTestsToJson(
        data: List<ExportSpeedTestData>,
        filename: String = "speedtests_${dateFormat.format(Date())}.json"
    ): File {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, filename)

        FileWriter(file).use { writer ->
            writer.append("[\n")
            data.forEachIndexed { index, entry ->
                writer.append("  {\n")
                writer.append("    \"date\": \"${entry.date}\",\n")
                writer.append("    \"timestamp\": ${entry.timestamp},\n")
                writer.append("    \"downloadSpeed\": ${entry.downloadSpeed},\n")
                writer.append("    \"uploadSpeed\": ${entry.uploadSpeed},\n")
                writer.append("    \"ping\": ${entry.ping},\n")
                writer.append("    \"jitter\": ${entry.jitter},\n")
                writer.append("    \"packetLoss\": ${entry.packetLoss},\n")
                writer.append("    \"serverName\": \"${entry.serverName}\"\n")
                writer.append("  }")
                if (index < data.size - 1) writer.append(",")
                writer.append("\n")
            }
            writer.append("]")
        }
        return file
    }

    fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when {
                file.name.endsWith(".csv") -> "text/csv"
                file.name.endsWith(".json") -> "application/json"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    data class ExportUsageData(
        val date: String,
        val timestamp: Long,
        val totalBytes: Long,
        val wifiBytes: Long,
        val mobileBytes: Long,
        val rxBytes: Long,
        val txBytes: Long,
        val packageName: String,
        val networkType: String
    )

    data class ExportSpeedTestData(
        val date: String,
        val timestamp: Long,
        val downloadSpeed: Double,
        val uploadSpeed: Double,
        val ping: Double,
        val jitter: Double,
        val packetLoss: Double,
        val serverName: String
    )
}