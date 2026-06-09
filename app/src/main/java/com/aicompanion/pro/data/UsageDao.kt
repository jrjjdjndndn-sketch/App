package com.aicompanion.pro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsageDao {
    @Insert
    suspend fun insert(u: UsageEntity)

    @Query("SELECT COALESCE(SUM(tokens),0) FROM usage WHERE at >= :since")
    suspend fun sumSince(since: Long): Int

    @Query("SELECT COALESCE(SUM(tokens),0) FROM usage")
    suspend fun total(): Int
}
