package com.bzygordev.netbio.data.repository

import kotlinx.coroutines.flow.Flow
import com.bzygordev.netbio.data.local.dao.NetworkInfoDao
import com.bzygordev.netbio.data.local.entity.NetworkInfoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkInfoRepository @Inject constructor(
    private val networkInfoDao: NetworkInfoDao
) {

    suspend fun insert(info: NetworkInfoEntity) {
        networkInfoDao.insert(info)
    }

    fun getAll(): Flow<List<NetworkInfoEntity>> = networkInfoDao.getAll()

    fun getBySsid(ssid: String): Flow<List<NetworkInfoEntity>> = networkInfoDao.getBySsid(ssid)

    fun getDistinctSsids(): Flow<List<String>> = networkInfoDao.getDistinctSsids()

    fun getLatest(): Flow<NetworkInfoEntity?> = networkInfoDao.getLatest()

    suspend fun deleteOld(cutoffDate: Long) {
        networkInfoDao.deleteOld(cutoffDate)
    }
}
