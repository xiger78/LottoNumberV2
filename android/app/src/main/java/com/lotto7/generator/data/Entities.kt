package com.lotto7.generator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "winning_numbers")
data class WinningNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundLabel: String,
    val drawDate: String,
    val numbers: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generation_history")
data class GenerationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val numbers: String
)
