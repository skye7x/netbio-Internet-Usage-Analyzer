package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.dao.AppUsageSummary
import pl.netbio.internetusageanalyzer.data.local.dao.DailySummary
import pl.netbio.internetusageanalyzer.data.local.dao.DataUsageDao
import pl.netbio.internetusageanalyzer.data.local.entity.DataUsageEntity
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    private val dataUsageDao: DataUsageDao
) {
    fun getAllUsage(): Flow<List<DataUsageEntity>> = dataUsageDao.getAllUsage()

    fun getUsageByDate(date: String): Flow<List<DataUsageEntity>> = dataUsageDao.getUsageByDate(date)

    fun getUsageBetween(start: Long, end: Long): Flow<List<DataUsageEntity>> = dataUsageDao.getUsageBetween(start, end)

    fun getTotalUsageByDate(date: String): Flow<Long?> = dataUsageDao.getTotalUsageByDate(date)

    fun getWifiUsageByDate(date: String): Flow<Long?> = dataUsageDao.getWifiUsageByDate(date)

    fun getMobileUsageByDate(date: String): Flow<Long?> = dataUsageDao.getMobileUsageByDate(date)

    fun getTotalUsageBetween(start: Long, end: Long): Flow<Long?> = dataUsageDao.getTotalUsageBetween(start, end)

    fun getWifiUsageBetween(start: Long, end: Long): Flow<Long?> = dataUsageDao.getWifiUsageBetween(start, end)

    fun getMobileUsageBetween(start: Long, end: Long): Flow<Long?> = dataUsageDao.getMobileUsageBetween(start, end)

    fun getDailySummaries(limit: Int): Flow<List<DailySummary>> = dataUsageDao.getDailySummaries(limit)

    fun getAppUsageBetween(start: Long, end: Long): Flow<List<AppUsageSummary>> = dataUsageDao.getAppUsageBetween(start, end)

    fun getUsageByNetworkType(start: Long, end: Long, networkType: String): Flow<Long?> = dataUsageDao.getUsageByNetworkType(start, end, networkType)

    fun getHourlyUsage(date: String): Flow<List<DataUsageEntity>> = dataUsageDao.getHourlyUsage(date)

    suspend fun insertUsage(usage: DataUsageEntity): Long = dataUsageDao.insert(usage)

    suspend fun insertAll(usages: List<DataUsageEntity>) = dataUsageDao.insertAll(usages)

    suspend fun deleteAll() = dataUsageDao.deleteAll()

    fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    fun getTodayStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getTodayEnd(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun getWeekStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getMonthStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getMonthEnd(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    fun getYearStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun Calendar.getActualMaximum(field: Int): Int = this.getActualMaximum(field)
}
