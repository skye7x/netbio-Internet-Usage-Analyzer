package pl.netbio.internetusageanalyzer.data.repository

import kotlinx.coroutines.flow.Flow
import pl.netbio.internetusageanalyzer.data.local.dao.NetworkInfoDao
import pl.netbio.internetusageanalyzer.data.local.entity.NetworkInfoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(
    private val networkInfoDao: NetworkInfoDao
) {
    fun getLatestNetworkInfo(): Flow<NetworkInfoEntity?> = networkInfoDao.getLatestNetworkInfo()

    fun getNetworkInfoByType(type: String): Flow<List<NetworkInfoEntity>> = networkInfoDao.getNetworkInfoByType(type)

    fun getAllNetworkNames(): Flow<List<String>> = networkInfoDao.getAllNetworkNames()

    fun getNetworkHistory(name: String): Flow<List<NetworkInfoEntity>> = networkInfoDao.getNetworkHistory(name)

    suspend fun insertNetworkInfo(info: NetworkInfoEntity): Long = networkInfoDao.insert(info)
}
