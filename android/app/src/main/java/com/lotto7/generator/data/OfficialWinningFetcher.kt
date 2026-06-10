package com.lotto7.generator.data

data class FetchedWinning(
    val roundLabel: String,
    val drawDate: String,
    val numbers: List<Int>
)

data class ImportResult(
    val added: Int,
    val skipped: Int,
    val message: String
)

object OfficialWinningFetcher {
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    private val mainPageUrl =
        "https://www.mizuhobank.co.jp/takarakuji/check/loto/loto7/index.html"

    fun backnumberUrl(round: Int): String =
        "https://www.mizuhobank.co.jp/takarakuji/check/loto/backnumber/detail.html?fromto=$round&loto=7"

    suspend fun fetchLatest(): List<FetchedWinning> {
        val html = fetchHtml(mainPageUrl) ?: throw IllegalStateException("fetch_failed")
        val parsed = parseFromHtml(html)
        if (parsed.isNotEmpty()) return parsed

        val latestRound = guessLatestRound(html)
        if (latestRound > 0) {
            val detailHtml = fetchHtml(backnumberUrl(latestRound))
                ?: throw IllegalStateException("fetch_failed")
            val detailParsed = parseFromHtml(detailHtml)
            if (detailParsed.isNotEmpty()) return detailParsed
        }
        throw IllegalStateException("parse_failed")
    }

    private fun fetchHtml(url: String): String? {
        return try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml")
            connection.setRequestProperty("Accept-Language", "ja-JP,ja;q=0.9,en;q=0.8")
            connection.instanceFollowRedirects = true
            val code = connection.responseCode
            if (code !in 200..299) return null
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (_: Exception) {
            null
        }
    }

    internal fun parseFromHtml(html: String): List<FetchedWinning> {
        if (html.contains("Access Denied", ignoreCase = true)) return emptyList()

        val results = mutableListOf<FetchedWinning>()
        val roundPattern = Regex("第(\\d+)回")
        val datePattern = Regex("(\\d{4})年(\\d{1,2})月(\\d{1,2})日")

        val roundMatches = roundPattern.findAll(html).map { it.value }.distinct().take(5)
        for (roundLabel in roundMatches) {
            val roundIndex = html.indexOf(roundLabel)
            if (roundIndex < 0) continue
            val chunk = html.substring(roundIndex, (roundIndex + 2500).coerceAtMost(html.length))
            val date = datePattern.find(chunk)?.value ?: continue
            val numbers = extractMainNumbers(chunk)
            if (numbers.size == 7) {
                results.add(FetchedWinning(roundLabel, date, numbers.sorted()))
            }
        }

        if (results.isNotEmpty()) return results.distinctBy { it.roundLabel }

        val date = datePattern.find(html)?.value ?: return emptyList()
        val round = roundPattern.find(html)?.value ?: return emptyList()
        val numbers = extractMainNumbers(html)
        return if (numbers.size == 7) {
            listOf(FetchedWinning(round, date, numbers.sorted()))
        } else {
            emptyList()
        }
    }

    private fun extractMainNumbers(text: String): List<Int> {
        val cleaned = text
            .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ")

        val candidates = Regex("\\b([0-9]{1,2})\\b").findAll(cleaned)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .filter { it in 1..37 }
            .toList()

        val mainSection = Regex("本数字[\\s\\S]{0,400}").find(cleaned)?.value
        if (mainSection != null) {
            val fromSection = Regex("\\b([0-9]{1,2})\\b").findAll(mainSection)
                .mapNotNull { it.groupValues[1].toIntOrNull() }
                .filter { it in 1..37 }
                .distinct()
                .take(7)
                .toList()
            if (fromSection.size == 7) return fromSection.sorted()
        }

        return candidates.distinct().take(7)
    }

    private fun guessLatestRound(html: String): Int {
        return Regex("第(\\d+)回").findAll(html)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .maxOrNull() ?: 0
    }
}
