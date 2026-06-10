package com.lotto7.generator.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WinningNumberDao {
    @Query("SELECT * FROM winning_numbers ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WinningNumberEntity>>

    @Query("SELECT * FROM winning_numbers ORDER BY updatedAt DESC")
    suspend fun getAll(): List<WinningNumberEntity>

    @Insert
    suspend fun insert(entity: WinningNumberEntity): Long

    @Update
    suspend fun update(entity: WinningNumberEntity)

    @Delete
    suspend fun delete(entity: WinningNumberEntity)
}

@Dao
interface GenerationHistoryDao {
    @Query("SELECT COUNT(*) FROM generation_history")
    suspend fun count(): Int

    @Query("SELECT * FROM generation_history ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<GenerationHistoryEntity>

    @Insert
    suspend fun insert(entity: GenerationHistoryEntity): Long

    @Query("DELETE FROM generation_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
