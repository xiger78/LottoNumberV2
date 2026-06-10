package com.lotto7.generator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lotto7.generator.data.GenerationHistoryEntity
import com.lotto7.generator.data.WinningNumberEntity
import com.lotto7.generator.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val OFFICIAL_LOTO7_URL = "https://www.mizuhobank.co.jp/takarakuji/check/loto/loto7/index.html"

data class LottoUiState(
    val isLoading: Boolean = true,
    val summary: AnalysisSummary? = null,
    val combos: List<GeneratedCombo> = emptyList(),
    val selectedMonth: Int? = null,
    val errorMessage: String? = null,
    val savedWinningCount: Int = 0
)

data class WinningUiState(
    val items: List<WinningNumberEntity> = emptyList(),
    val showDialog: Boolean = false,
    val editing: WinningNumberEntity? = null,
    val roundInput: String = "",
    val dateInput: String = "",
    val numbersInput: String = "",
    val errorMessage: String? = null,
    val confirmDelete: WinningNumberEntity? = null
)

data class HistoryUiState(
    val items: List<GenerationHistoryEntity> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LottoApplication
    private val repo = app.repository

    val language: StateFlow<AppLanguage> = repo.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppLanguage.JA)

    private val _lottoState = MutableStateFlow(LottoUiState())
    val lottoState: StateFlow<LottoUiState> = _lottoState.asStateFlow()

    private val _winningState = MutableStateFlow(WinningUiState())
    val winningState: StateFlow<WinningUiState> = _winningState.asStateFlow()

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    private lateinit var draws: List<Draw>
    private lateinit var analyzer: Lotto7Engine.PatternAnalyzer
    private var savedWinning: List<List<Int>> = emptyList()

    init {
        loadInitialData()
        observeWinningNumbers()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val loaded = repo.loadDraws()
                draws = loaded
                analyzer = Lotto7Engine.PatternAnalyzer(draws)
                refreshSavedWinning()
                val month = Lotto7Engine.resolveTargetMonth(draws)
                _lottoState.update {
                    it.copy(
                        isLoading = false,
                        selectedMonth = month,
                        summary = analyzer.buildSummary(month),
                        combos = emptyList(),
                        savedWinningCount = savedWinning.size
                    )
                }
            } catch (e: Exception) {
                _lottoState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
        }
    }

    private fun observeWinningNumbers() {
        viewModelScope.launch {
            repo.winningNumbersFlow.collect { items ->
                _winningState.update { it.copy(items = items) }
                refreshSavedWinningFromEntities(items)
            }
        }
    }

    private suspend fun refreshSavedWinning() {
        savedWinning = repo.winningNumberLists(repo.getWinningNumbers())
    }

    private fun refreshSavedWinningFromEntities(items: List<WinningNumberEntity>) {
        savedWinning = repo.winningNumberLists(items)
        if (::analyzer.isInitialized) {
            _lottoState.update { it.copy(savedWinningCount = savedWinning.size) }
        }
    }

    private fun generator(): Lotto7Engine.Generator {
        return Lotto7Engine.Generator(analyzer, savedWinningNumbers = savedWinning)
    }

    fun regenerate() {
        if (!::analyzer.isInitialized || _lottoState.value.isLoading) return
        viewModelScope.launch {
            val month = _lottoState.value.selectedMonth
            val combos = generateCombos(month)
            combos.forEach { combo ->
                repo.saveGenerationHistory(combo.numbers)
            }
            _lottoState.update {
                it.copy(
                    combos = combos,
                    summary = analyzer.buildSummary(month)
                )
            }
            loadHistoryPage(0)
        }
    }

    fun setMonth(month: Int) {
        if (!::analyzer.isInitialized) return
        val safeMonth = month.coerceIn(1, 12)
        _lottoState.update {
            it.copy(
                selectedMonth = safeMonth,
                summary = analyzer.buildSummary(safeMonth)
            )
        }
    }

    private fun generateCombos(month: Int?): List<GeneratedCombo> {
        return generator().generate(count = 10, targetMonth = month).mapIndexed { index, nums ->
            GeneratedCombo(
                index = index + 1,
                numbers = nums,
                sum = nums.sum(),
                oddCount = nums.count { it % 2 == 1 },
                kouDistribution = Lotto7Engine.kouDistribution(nums)
            )
        }
    }

    fun openWinningDialog(entity: WinningNumberEntity? = null) {
        _winningState.update {
            it.copy(
                showDialog = true,
                editing = entity,
                roundInput = entity?.roundLabel ?: "",
                dateInput = entity?.drawDate ?: "",
                numbersInput = entity?.numbers?.replace(",", " ") ?: "",
                errorMessage = null
            )
        }
    }

    fun closeWinningDialog() {
        _winningState.update {
            it.copy(showDialog = false, editing = null, errorMessage = null)
        }
    }

    fun updateRoundInput(v: String) = _winningState.update { it.copy(roundInput = v) }
    fun updateDateInput(v: String) = _winningState.update { it.copy(dateInput = v) }
    fun updateNumbersInput(v: String) = _winningState.update { it.copy(numbersInput = v) }

    fun saveWinning() {
        val state = _winningState.value
        val nums = repo.parseNumbers(state.numbersInput)
        if (nums == null) {
            _winningState.update { it.copy(errorMessage = "invalid") }
            return
        }
        viewModelScope.launch {
            val formatted = repo.formatNumbers(nums)
            if (state.editing == null) {
                repo.insertWinning(
                    WinningNumberEntity(
                        roundLabel = state.roundInput.trim(),
                        drawDate = state.dateInput.trim(),
                        numbers = formatted
                    )
                )
            } else {
                repo.updateWinning(
                    state.editing.copy(
                        roundLabel = state.roundInput.trim(),
                        drawDate = state.dateInput.trim(),
                        numbers = formatted,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            closeWinningDialog()
        }
    }

    fun requestDeleteWinning(entity: WinningNumberEntity) {
        _winningState.update { it.copy(confirmDelete = entity) }
    }

    fun cancelDeleteWinning() {
        _winningState.update { it.copy(confirmDelete = null) }
    }

    fun confirmDeleteWinning() {
        val entity = _winningState.value.confirmDelete ?: return
        viewModelScope.launch {
            repo.deleteWinning(entity)
            _winningState.update { it.copy(confirmDelete = null) }
        }
    }

    fun loadHistoryPage(page: Int) {
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true) }
            val (items, total) = repo.getHistoryPage(page)
            val totalPages = if (total == 0) 1 else ((total - 1) / 10) + 1
            _historyState.update {
                HistoryUiState(
                    items = items,
                    currentPage = page.coerceIn(0, totalPages - 1),
                    totalPages = totalPages,
                    totalCount = total,
                    isLoading = false
                )
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { repo.setLanguage(lang) }
    }
}
