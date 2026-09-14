package com.bzygordev.netbio.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "data_usage",
    primaryKeys = ["date", "networkType"]
)
data class DataUsageEntity(
    val date: String,
    val networkType: String,
    val bytesUsed: Long,
    val rxBytes: Long,
    val txBytes: Long
)
