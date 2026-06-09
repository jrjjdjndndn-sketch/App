package com.aicompanion.pro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(p: GameProfileEntity)

    @Query("SELECT * FROM game_profiles WHERE packageName = :pkg")
    suspend fun get(pkg: String): GameProfileEntity?
}
