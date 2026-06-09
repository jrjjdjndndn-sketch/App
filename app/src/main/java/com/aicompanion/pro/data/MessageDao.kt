package com.aicompanion.pro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(m: MessageEntity)

    @Query("SELECT * FROM messages WHERE sessionId = :s ORDER BY id ASC")
    fun bySession(s: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY id DESC LIMIT :n")
    suspend fun latest(n: Int): List<MessageEntity>

    @Query("SELECT * FROM messages ORDER BY id ASC")
    suspend fun all(): List<MessageEntity>

    @Query("DELETE FROM messages")
    suspend fun clear()
}
