package com.bzygordev.netbio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.bzygordev.netbio.data.local.dao.DataUsageDao
import com.bzygordev.netbio.data.local.entity.DataUsageEntity
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataUsageRepository @Inject constructor(
    private val dataUsageDao: DataUsageDao
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun insertOrUpdate(
        date: String,
        networkType: String,
        bytesUsed: Long,
        rxBytes: Long,
        txBytes: Long
    ) {
        dataUsageDao.insertOrUpdate(
            DataUsageEntity(
                date = date,
                networkType = networkType,
                bytesUsed = bytesUsed,
                rxBytes = rxBytes,
                txBytes = txBytes
            )
        )
    }

    fun getTodayUsage(): Flow<Long> {
        val today = todayString()
        return dataUsageDao.getByDate(today).map { list ->
            list.sumOf { it.bytesUsed }
        }
    }

    fun getTodayWifiUsage(): Flow<Long> {
        val today = todayString()
        return dataUsageDao.getByDate(today).map { list ->
            list.filter { it.networkType == "wifi" }.sumOf { it.bytesUsed }
        }
    }

    fun getTodayMobileUsage(): Flow<Long> {
        val today = todayString()
        return dataUsageDao.getByDate(today).map { list ->
            list.filter { it.networkType == "mobile" }.sumOf { it.bytesUsed }
        }
    }

    fun getWeekUsage(): Flow<Long> {
        val (start, end) = weekRange()
        return dataUsageDao.getTotalByDateRange(start, end).map { it ?: 0L }
    }

    fun getMonthUsage(): Flow<Long> {
        val (start, end) = monthRange()
        return dataUsageDao.getTotalByDateRange(start, end).map { it ?: 0L }
    }

    fun getYearUsage(): Flow<Long> {
        val cal = Calendar.getInstance()
        val start = String.format("%d-01-01", cal.get(Calendar.YEAR))
        val end = String.format("%d-12-31", cal.get(Calendar.YEAR))
        return dataUsageDao.getTotalByDateRange(start, end).map { it ?: 0L }
    }

    fun getDailyUsageForMonth(yearMonth: String): Flow<List<Pair<String, Long>>> {
        return dataUsageDao.getDailyUsageForMonth(yearMonth).map { list ->
            list.groupBy { it.date }
                .map { (date, entities) -> date to entities.sumOf { it.bytesUsed } }
                .sortedBy { it.first }
        }
    }

    fun getHourlyUsage(date: String): Flow<List<Pair<Int, Long>>> {
        return dataUsageDao.getByDate(date).map { list ->
            list.map { entity ->
                entity.bytesUsed
            }.mapIndexed { index, bytes ->
                index to bytes
            }.groupBy { it.first }
                .map { (hour, pairs) -> hour to pairs.sumOf { it.second } }
                .sortedBy { it.first }
        }
    }

    fun getUsageByDateRange(start: String, end: String): Flow<Long> {
        return dataUsageDao.getTotalByDateRange(start, end).map { it ?: 0L }
    }

    fun getPreviousPeriodUsage(type: String): Flow<Long> {
        val cal = Calendar.getInstance()
        when (type) {
            "daily" -> cal.add(Calendar.DAY_OF_YEAR, -1)
            "weekly" -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            "monthly" -> cal.add(Calendar.MONTH, -1)
        }
        val end = dateFormat.format(cal.time)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = dateFormat.format(cal.time)
        return dataUsageDao.getTotalByDateRange(start, end).map { it ?: 0L }
    }

    fun getUsageChangePercent(type: String): Flow<Float> {
        return combine(
            getCurrentPeriodUsage(type),
            getPreviousPeriodUsage(type)
        ) { current, previous ->
            if (previous == 0L) {
                if (current > 0) 100f else 0f
            } else {
                ((current - previous).toFloat() / previous.toFloat()) * 100f
            }
        }
    }

    fun getAllDates(): Flow<List<String>> = dataUsageDao.getAllDates()

    private fun getCurrentPeriodUsage(type: String): Flow<Long> {
        return when (type) {
            "daily" -> getTodayUsage()
            "weekly" -> getWeekUsage()
            "monthly" -> getMonthUsage()
            else -> getTodayUsage()
        }
    }

    private fun todayString(): String = dateFormat.format(Date())

    private fun weekRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = dateFormat.format(cal.time)
        return start to end
    }

    private fun monthRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = dateFormat.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = dateFormat.format(cal.time)
        return start to end
    }
}
