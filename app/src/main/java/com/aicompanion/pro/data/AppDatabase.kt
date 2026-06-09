package com.aicompanion.pro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val text: String,
    val sessionId: String = "default",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "usage")
data class UsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tokens: Int,
    val at: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey val packageName: String,
    val summary: String = "",
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val category: String = "general",
    val createdAt: Long = System.currentTimeMillis()
)

@Database(
    entities = [
        MessageEntity::class,
        UsageEntity::class,
        GameProfileEntity::class,
        MemoryEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messages(): MessageDao
    abstract fun usage(): UsageDao
    abstract fun gameProfiles(): GameProfileDao
    abstract fun memory(): MemoryDao

    companion object {
        @Volatile private var I: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = I ?: synchronized(this) {
            I ?: Room.databaseBuilder(
                ctx.applicationContext,
                AppDatabase::class.java,
                "companion.db"
            ).fallbackToDestructiveMigration().build().also { I = it }
        }
    }
}
