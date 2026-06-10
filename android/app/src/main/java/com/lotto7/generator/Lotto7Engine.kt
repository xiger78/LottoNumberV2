package com.lotto7.generator

import kotlin.math.sqrt
import kotlin.random.Random

object Lotto7Engine {
    private const val MIN_NUM = 1
    private const val MAX_NUM = 37
    private const val MAIN_COUNT = 7

    fun kouOf(n: Int): Int = when {
        n <= 7 -> 1
        n <= 14 -> 2
        n <= 21 -> 3
        n <= 28 -> 4
        else -> 5
    }

    fun kouDistribution(nums: List<Int>): List<Int> {
        val counts = IntArray(6)
        nums.forEach { counts[kouOf(it)]++ }
        return (1..5).map { counts[it] }
    }

    class PatternAnalyzer(private val draws: List<Draw>) {
        val freq = mutableMapOf<Int, Int>()
        val monthFreq = mutableMapOf<Int, MutableMap<Int, Int>>()
        val kouPatterns = mutableMapOf<List<Int>, Int>()
        val oddPatterns = mutableMapOf<Int, Int>()
        val sums = mutableListOf<Int>()
        var sumAvg: Double = 0.0
        var sumStd: Double = 0.0
        lateinit var topKou: Set<List<Int>>
        lateinit var topOdd: Set<Int>

        init {
            analyze()
        }

        private fun analyze() {
            draws.forEach { draw ->
                draw.nums.forEach { n ->
                    freq[n] = (freq[n] ?: 0) + 1
                    draw.month?.let { month ->
                        val monthMap = monthFreq.getOrPut(month) { mutableMapOf() }
                        monthMap[n] = (monthMap[n] ?: 0) + 1
                    }
                }
                val kou = kouDistribution(draw.nums)
                kouPatterns[kou] = (kouPatterns[kou] ?: 0) + 1
                val odd = draw.nums.count { it % 2 == 1 }
                oddPatterns[odd] = (oddPatterns[odd] ?: 0) + 1
                sums.add(draw.nums.sum())
            }

            sumAvg = sums.average()
            sumStd = sqrt(sums.map { (it - sumAvg) * (it - sumAvg) }.average())
            topKou = kouPatterns.entries
                .sortedByDescending { it.value }
                .take(30)
                .map { it.key }
                .toSet()
            topOdd = oddPatterns.entries
                .sortedByDescending { it.value }
                .take(4)
                .map { it.key }
                .toSet()
        }

        fun numberWeights(
            targetMonth: Int? = null,
            savedWinningNumbers: List<List<Int>> = emptyList()
        ): Map<Int, Double> {
            val weights = mutableMapOf<Int, Double>()
            for (n in MIN_NUM..MAX_NUM) {
                weights[n] = (freq[n] ?: 1).toDouble()
            }
            targetMonth?.let { month ->
                monthFreq[month]?.forEach { (n, count) ->
                    weights[n] = (weights[n] ?: 0.0) + count * 0.5
                }
            }
            if (savedWinningNumbers.isNotEmpty()) {
                val recentFreq = mutableMapOf<Int, Int>()
                savedWinningNumbers.take(20).forEach { nums ->
                    nums.forEach { n -> recentFreq[n] = (recentFreq[n] ?: 0) + 1 }
                }
                recentFreq.forEach { (n, count) ->
                    weights[n] = (weights[n] ?: 1.0) * (1.0 - count * 0.12).coerceAtLeast(0.3)
                }
                val allWinning = savedWinningNumbers.flatten().toSet()
                for (n in MIN_NUM..MAX_NUM) {
                    if (n !in allWinning) {
                        weights[n] = (weights[n] ?: 1.0) * 1.08
                    }
                }
            }
            return weights
        }

        fun buildSummary(targetMonth: Int?): AnalysisSummary {
            val hot = freq.entries.sortedByDescending { it.value }.take(5).map { it.key }
            val cold = freq.entries.sortedBy { it.value }.take(5).map { it.key }
            val topOdd = oddPatterns.entries.sortedByDescending { it.value }.take(3)
                .map { it.key to it.value }
            val latest = draws.last()

            return AnalysisSummary(
                totalDraws = draws.size,
                latestRound = latest.round,
                latestDate = latest.date,
                latestNums = latest.nums,
                hotNumbers = hot,
                coldNumbers = cold,
                sumAverage = sumAvg,
                sumMin = sums.minOrNull() ?: 0,
                sumMax = sums.maxOrNull() ?: 0,
                topOddPatterns = topOdd,
                targetMonth = targetMonth
            )
        }
    }

    class Generator(
        private val analyzer: PatternAnalyzer,
        private val random: Random = Random.Default,
        private val savedWinningNumbers: List<List<Int>> = emptyList()
    ) {
        private fun overlapsWinning(nums: List<Int>): Boolean {
            if (savedWinningNumbers.isEmpty()) return false
            if (nums in savedWinningNumbers) return true
            return savedWinningNumbers.any { winning ->
                nums.intersect(winning.toSet()).size >= 5
            }
        }

        private fun fitsPattern(nums: List<Int>): Boolean {
            if (overlapsWinning(nums)) return false
            val kou = kouDistribution(nums)
            val odd = nums.count { it % 2 == 1 }
            val total = nums.sum()
            if (kou !in analyzer.topKou) return false
            if (odd !in analyzer.topOdd) return false
            if (total !in (analyzer.sumAvg - 1.8 * analyzer.sumStd).toInt()
                    ..(analyzer.sumAvg + 1.8 * analyzer.sumStd).toInt()) return false
            return true
        }

        private fun weightedPick(weights: Map<Int, Double>, exclude: Set<Int>): Int {
            val candidates = (MIN_NUM..MAX_NUM).filter { it !in exclude }
            val totalWeight = candidates.sumOf { weights[it] ?: 1.0 }
            var roll = random.nextDouble() * totalWeight
            for (n in candidates) {
                roll -= weights[n] ?: 1.0
                if (roll <= 0) return n
            }
            return candidates.last()
        }

        fun generateOne(targetMonth: Int? = null): List<Int> {
            val weights = analyzer.numberWeights(targetMonth, savedWinningNumbers)
            repeat(500) {
                val nums = mutableSetOf<Int>()
                while (nums.size < MAIN_COUNT) {
                    nums.add(weightedPick(weights, nums))
                }
                val result = nums.sorted()
                if (fitsPattern(result)) return result
            }
            val nums = mutableSetOf<Int>()
            while (nums.size < MAIN_COUNT) {
                nums.add(weightedPick(weights, nums))
            }
            return nums.sorted()
        }

        fun generate(count: Int = 10, targetMonth: Int? = null): List<List<Int>> {
            val seen = mutableSetOf<List<Int>>()
            val results = mutableListOf<List<Int>>()
            var attempts = 0
            while (results.size < count && attempts < count * 200) {
                attempts++
                val nums = generateOne(targetMonth)
                if (seen.add(nums)) {
                    results.add(nums)
                }
            }
            return results
        }
    }

    fun resolveTargetMonth(draws: List<Draw>, overrideMonth: Int? = null): Int? {
        if (overrideMonth != null) return overrideMonth.coerceIn(1, 12)
        val lastMonth = draws.lastOrNull()?.month ?: return null
        return lastMonth % 12 + 1
    }
}
