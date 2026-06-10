package com.lotto7.generator.data

import android.content.Context
import com.lotto7.generator.Draw
import com.lotto7.generator.DrawRepository
import com.lotto7.generator.Lotto7Engine
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val context: Context,
    private val winningDao: WinningNumberDao,
    private val historyDao: GenerationHistoryDao,
    private val settingsStore: SettingsStore
) {
    val languageFlow: Flow<com.lotto7.generator.i18n.AppLanguage> = settingsStore.languageFlow
    val winningNumbersFlow: Flow<List<WinningNumberEntity>> = winningDao.observeAll()

    suspend fun loadDraws(): List<Draw> = DrawRepository.loadDraws(context)

    suspend fun getWinningNumbers(): List<WinningNumberEntity> = winningDao.getAll()

    suspend fun insertWinning(entity: WinningNumberEntity): Long = winningDao.insert(entity)

    suspend fun updateWinning(entity: WinningNumberEntity) = winningDao.update(entity)

    suspend fun deleteWinning(entity: WinningNumberEntity) = winningDao.delete(entity)

    suspend fun saveGenerationHistory(numbers: List<Int>) {
        historyDao.insert(
            GenerationHistoryEntity(
                createdAt = System.currentTimeMillis(),
                numbers = numbers.joinToString(",")
            )
        )
    }

    suspend fun getHistoryPage(page: Int, pageSize: Int = 10): Pair<List<GenerationHistoryEntity>, Int> {
        val total = historyDao.count()
        val offset = page * pageSize
        val items = historyDao.getPage(pageSize, offset)
        return items to total
    }

    suspend fun setLanguage(language: com.lotto7.generator.i18n.AppLanguage) {
        settingsStore.setLanguage(language)
    }

    fun parseNumbers(raw: String): List<Int>? {
        val nums = raw.split(",", " ", "　", "\t", "\n")
            .mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
            .distinct()
            .sorted()
        return if (nums.size == 7 && nums.all { it in 1..37 }) nums else null
    }

    fun formatNumbers(nums: List<Int>): String = nums.joinToString(",") { "%02d".format(it) }

    fun winningNumberLists(entities: List<WinningNumberEntity>): List<List<Int>> {
        return entities.mapNotNull { parseNumbers(it.numbers) }
    }

    suspend fun importFromDraws(draws: List<Draw>): ImportResult {
        var added = 0
        var skipped = 0
        for (draw in draws) {
            if (winningDao.findByRound(draw.round) != null) {
                skipped++
                continue
            }
            val id = winningDao.insertIgnore(
                WinningNumberEntity(
                    roundLabel = draw.round,
                    drawDate = draw.date,
                    numbers = formatNumbers(draw.nums)
                )
            )
            if (id > 0) added++ else skipped++
        }
        return ImportResult(added, skipped, "")
    }

    suspend fun importFromOfficialSite(): ImportResult {
        val fetched = OfficialWinningFetcher.fetchLatest()
        var added = 0
        var skipped = 0
        for (item in fetched) {
            if (winningDao.findByRound(item.roundLabel) != null) {
                skipped++
                continue
            }
            val id = winningDao.insertIgnore(
                WinningNumberEntity(
                    roundLabel = item.roundLabel,
                    drawDate = item.drawDate,
                    numbers = formatNumbers(item.numbers)
                )
            )
            if (id > 0) added++ else skipped++
        }
        return ImportResult(added, skipped, "")
    }
}
