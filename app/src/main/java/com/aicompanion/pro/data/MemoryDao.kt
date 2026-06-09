package com.aicompanion.pro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MemoryDao {
    @Insert
    suspend fun insert(m: MemoryEntity)

    @Query("SELECT * FROM memory ORDER BY id DESC")
    suspend fun all(): List<MemoryEntity>

    @Query("SELECT * FROM memory WHERE category = :cat ORDER BY id DESC")
    suspend fun byCategory(cat: String): List<MemoryEntity>

    @Delete
    suspend fun delete(m: MemoryEntity)

    @Query("DELETE FROM memory")
    suspend fun clear()
}
