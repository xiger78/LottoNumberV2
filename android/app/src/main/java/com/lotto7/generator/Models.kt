package com.lotto7.generator

data class Draw(
    val round: String,
    val date: String,
    val month: Int?,
    val nums: List<Int>
)

data class AnalysisSummary(
    val totalDraws: Int,
    val latestRound: String,
    val latestDate: String,
    val latestNums: List<Int>,
    val hotNumbers: List<Int>,
    val coldNumbers: List<Int>,
    val sumAverage: Double,
    val sumMin: Int,
    val sumMax: Int,
    val topOddPatterns: List<Pair<Int, Int>>,
    val targetMonth: Int?
)

data class GeneratedCombo(
    val index: Int,
    val numbers: List<Int>,
    val sum: Int,
    val oddCount: Int,
    val kouDistribution: List<Int>
)
