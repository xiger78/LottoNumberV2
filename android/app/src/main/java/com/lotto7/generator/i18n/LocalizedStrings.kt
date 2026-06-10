package com.lotto7.generator.i18n

enum class AppLanguage(val code: String, val label: String) {
    JA("ja", "日本語"),
    KO("ko", "한국어"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code == code } ?: JA
        }
    }
}

data class Strings(
    val appName: String,
    val navLotto: String,
    val navWinning: String,
    val navHistory: String,
    val navSettings: String,
    val patternAnalysis: String,
    val analysisDraws: String,
    val latestRound: String,
    val latestWinning: String,
    val hotNumbers: String,
    val coldNumbers: String,
    val sumAverage: String,
    val oddDistribution: String,
    val monthPattern: String,
    val generateButton: String,
    val recommendedCombos: String,
    val disclaimer: String,
    val winningConsidered: String,
    val openOfficialSite: String,
    val winningTitle: String,
    val addWinning: String,
    val editWinning: String,
    val deleteWinning: String,
    val roundLabel: String,
    val drawDateLabel: String,
    val numbersLabel: String,
    val save: String,
    val cancel: String,
    val delete: String,
    val confirmDelete: String,
    val invalidNumbers: String,
    val emptyWinning: String,
    val historyTitle: String,
    val historyEmpty: String,
    val prevPage: String,
    val nextPage: String,
    val pageInfo: String,
    val settingsTitle: String,
    val languageLabel: String,
    val loadError: String,
    val times: String,
    val sumLabel: String,
    val oddLabel: String,
    val kouLabel: String,
    val monthSuffix: String
) {
    fun monthLabel(month: Int): String = "$month$monthSuffix"
}

object LocalizedStrings {
    private val ja = Strings(
        appName = "ロト番号",
        navLotto = "ロト番号",
        navWinning = "当選数字",
        navHistory = "生成履歴",
        navSettings = "設定",
        patternAnalysis = "パターン分析結果",
        analysisDraws = "分析回数",
        latestRound = "最新回",
        latestWinning = "最近当選",
        hotNumbers = "出現多い番号",
        coldNumbers = "出現少ない番号",
        sumAverage = "合計平均",
        oddDistribution = "奇数分布 TOP",
        monthPattern = "抽選月パターン",
        generateButton = "番号10組生成",
        recommendedCombos = "おすすめ本数字",
        disclaimer = "※ 過去パターン参考用。当選を保証しません。",
        winningConsidered = "※ 登録済み当選数字も分析に反映されます。",
        openOfficialSite = "公式当選発表サイト",
        winningTitle = "当選数字管理",
        addWinning = "当選数字追加",
        editWinning = "当選数字編集",
        deleteWinning = "削除",
        roundLabel = "回別",
        drawDateLabel = "抽せん日",
        numbersLabel = "本数字 (7個, 1-37)",
        save = "保存",
        cancel = "キャンセル",
        delete = "削除",
        confirmDelete = "この当選数字を削除しますか？",
        invalidNumbers = "本数字7個 (1-37) を正しく入力してください。",
        emptyWinning = "登録された当選数字がありません。",
        historyTitle = "生成履歴",
        historyEmpty = "生成履歴がありません。",
        prevPage = "前へ",
        nextPage = "次へ",
        pageInfo = "ページ %d / %d",
        settingsTitle = "設定",
        languageLabel = "表示言語",
        loadError = "データ読込失敗",
        times = "回",
        sumLabel = "合",
        oddLabel = "奇",
        kouLabel = "口",
        monthSuffix = "月"
    )

    private val ko = Strings(
        appName = "로또번호",
        navLotto = "로또번호",
        navWinning = "당첨숫자",
        navHistory = "생성이력",
        navSettings = "설정",
        patternAnalysis = "패턴 분석 결과",
        analysisDraws = "분석 회차",
        latestRound = "최신 회차",
        latestWinning = "최근 당첨",
        hotNumbers = "자주 나온 번호",
        coldNumbers = "적게 나온 번호",
        sumAverage = "합계 평균",
        oddDistribution = "홀수 분포 TOP",
        monthPattern = "추첨 월 패턴",
        generateButton = "번호 10조합 생성",
        recommendedCombos = "추천 본숫자",
        disclaimer = "※ 과거 패턴 기반 참고용이며 당첨을 보장하지 않습니다.",
        winningConsidered = "※ 저장된 당첨숫자도 분석에 반영됩니다.",
        openOfficialSite = "공식 당첨 발표 사이트",
        winningTitle = "당첨숫자 관리",
        addWinning = "당첨숫자 추가",
        editWinning = "당첨숫자 수정",
        deleteWinning = "삭제",
        roundLabel = "회차",
        drawDateLabel = "추첨일",
        numbersLabel = "본숫자 (7개, 1-37)",
        save = "저장",
        cancel = "취소",
        delete = "삭제",
        confirmDelete = "이 당첨숫자를 삭제하시겠습니까?",
        invalidNumbers = "본숫자 7개(1-37)를 올바르게 입력하세요.",
        emptyWinning = "등록된 당첨숫자가 없습니다.",
        historyTitle = "생성이력",
        historyEmpty = "생성 이력이 없습니다.",
        prevPage = "이전",
        nextPage = "다음",
        pageInfo = "페이지 %d / %d",
        settingsTitle = "설정",
        languageLabel = "표시 언어",
        loadError = "데이터 로드 실패",
        times = "회",
        sumLabel = "합",
        oddLabel = "홀",
        kouLabel = "口",
        monthSuffix = "月"
    )

    private val en = Strings(
        appName = "Loto Number",
        navLotto = "Loto No.",
        navWinning = "Winning No.",
        navHistory = "History",
        navSettings = "Settings",
        patternAnalysis = "Pattern Analysis",
        analysisDraws = "Draws analyzed",
        latestRound = "Latest round",
        latestWinning = "Recent winning",
        hotNumbers = "Hot numbers",
        coldNumbers = "Cold numbers",
        sumAverage = "Sum average",
        oddDistribution = "Odd count TOP",
        monthPattern = "Draw month pattern",
        generateButton = "Generate 10 sets",
        recommendedCombos = "Recommended numbers",
        disclaimer = "※ Based on past patterns. No guarantee of winning.",
        winningConsidered = "※ Saved winning numbers are included in analysis.",
        openOfficialSite = "Official results site",
        winningTitle = "Winning Numbers",
        addWinning = "Add winning numbers",
        editWinning = "Edit winning numbers",
        deleteWinning = "Delete",
        roundLabel = "Round",
        drawDateLabel = "Draw date",
        numbersLabel = "Main numbers (7, 1-37)",
        save = "Save",
        cancel = "Cancel",
        delete = "Delete",
        confirmDelete = "Delete this winning entry?",
        invalidNumbers = "Enter exactly 7 numbers (1-37).",
        emptyWinning = "No winning numbers registered.",
        historyTitle = "Generation History",
        historyEmpty = "No generation history.",
        prevPage = "Prev",
        nextPage = "Next",
        pageInfo = "Page %d / %d",
        settingsTitle = "Settings",
        languageLabel = "Display language",
        loadError = "Failed to load data",
        times = "",
        sumLabel = "Sum",
        oddLabel = "Odd",
        kouLabel = "Kou",
        monthSuffix = "M"
    )

    fun get(language: AppLanguage): Strings = when (language) {
        AppLanguage.JA -> ja
        AppLanguage.KO -> ko
        AppLanguage.EN -> en
    }

    fun formatHistoryLine(language: AppLanguage, createdAt: Long, numbers: List<Int>): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = createdAt }
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH) + 1
        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val min = cal.get(java.util.Calendar.MINUTE)
        val nums = numbers.joinToString(" ") { "%02d".format(it) }
        return when (language) {
            AppLanguage.JA -> "%04d年%02d月%02d日%02d時%02d分:%s".format(y, m, d, h, min, nums)
            AppLanguage.KO -> "%04d년%02d월%02d일%02d시%02d분:%s".format(y, m, d, h, min, nums)
            AppLanguage.EN -> "%04d/%02d/%02d %02d:%02d:%s".format(y, m, d, h, min, nums)
        }
    }
}
