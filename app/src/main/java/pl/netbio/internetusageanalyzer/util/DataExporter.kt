package pl.netbio.internetusageanalyzer.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
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

    fun exportToCsv(data: List<ExportUsageData>, filename: String = "usage_${dateFormat.format(Date())}.csv"): File {
        val file = File(getExportDirectory(), filename)
        FileWriter(file).use { writer ->
            writer.append("Date,Time,Total Bytes,WiFi Bytes,Mobile Bytes,RX Bytes,TX Bytes,Package,Network Type\n")
            data.forEach { entry ->
                writer.append("${entry.date},${dateFormatReadable.format(Date(entry.timestamp))},${entry.totalBytes},${entry.wifiBytes},${entry.mobileBytes},${entry.rxBytes},${entry.txBytes},${entry.packageName},${entry.networkType}\n")
            }
        }
        return file
    }

    fun exportToJson(data: List<ExportUsageData>, filename: String = "usage_${dateFormat.format(Date())}.json"): File {
        val file = File(getExportDirectory(), filename)
        val jsonArray = JSONArray()
        data.forEach { entry ->
            val obj = JSONObject().apply {
                put("date", entry.date)
                put("timestamp", entry.timestamp)
                put("totalBytes", entry.totalBytes)
                put("wifiBytes", entry.wifiBytes)
                put("mobileBytes", entry.mobileBytes)
                put("rxBytes", entry.rxBytes)
                put("txBytes", entry.txBytes)
                put("packageName", entry.packageName)
                put("networkType", entry.networkType)
            }
            jsonArray.put(obj)
        }
        FileWriter(file).use { writer ->
            writer.write(jsonArray.toString(2))
        }
        return file
    }

    fun exportSpeedTestHistory(context: Context, results: List<ExportSpeedTestData>, filename: String = "speedtests_${dateFormat.format(Date())}.csv"): File {
        val file = File(getExportDirectory(context), filename)
        FileWriter(file).use { writer ->
            writer.append("Date,Time,Download (Mbps),Upload (Mbps),Ping (ms),Jitter (ms),Packet Loss (%),Server\n")
            results.forEach { entry ->
                writer.append("${entry.date},${dateFormatReadable.format(Date(entry.timestamp))},${entry.downloadSpeed},${entry.uploadSpeed},${entry.ping},${entry.jitter},${entry.packetLoss},${entry.serverName}\n")
            }
        }
        return file
    }

    fun exportUsageHistory(context: Context, usage: List<ExportUsageData>, filename: String = "usage_history_${dateFormat.format(Date())}.csv"): File {
        val file = File(getExportDirectory(context), filename)
        FileWriter(file).use { writer ->
            writer.append("Date,Time,Total Bytes,WiFi Bytes,Mobile Bytes,RX Bytes,TX Bytes,Package,Network Type\n")
            usage.forEach { entry ->
                writer.append("${entry.date},${dateFormatReadable.format(Date(entry.timestamp))},${entry.totalBytes},${entry.wifiBytes},${entry.mobileBytes},${entry.rxBytes},${entry.txBytes},${entry.packageName},${entry.networkType}\n")
            }
        }
        return file
    }

    fun exportSpeedTestsToJson(data: List<ExportSpeedTestData>, filename: String = "speedtests_${dateFormat.format(Date())}.json"): File {
        val file = File(getExportDirectory(), filename)
        val jsonArray = JSONArray()
        data.forEach { entry ->
            val obj = JSONObject().apply {
                put("date", entry.date)
                put("timestamp", entry.timestamp)
                put("downloadSpeed", entry.downloadSpeed)
                put("uploadSpeed", entry.uploadSpeed)
                put("ping", entry.ping)
                put("jitter", entry.jitter)
                put("packetLoss", entry.packetLoss)
                put("serverName", entry.serverName)
            }
            jsonArray.put(obj)
        }
        FileWriter(file).use { writer ->
            writer.write(jsonArray.toString(2))
        }
        return file
    }

    fun getExportDirectory(context: Context? = null): File {
        val ctx = context ?: this.context
        val dir = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "NetBio/exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun shareFile(context: Context? = null, file: File) {
        val ctx = context ?: this.context
        val uri = FileProvider.getUriForFile(
            ctx,
            "${ctx.packageName}.fileprovider",
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
        ctx.startActivity(
            Intent.createChooser(intent, "Share via")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
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
