package com.lotto7.generator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WinningNumberEntity::class, GenerationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun winningNumberDao(): WinningNumberDao
    abstract fun generationHistoryDao(): GenerationHistoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lotto7.db"
                ).build().also { instance = it }
            }
        }
    }
}
