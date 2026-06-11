package com.lotto7.generator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lotto7.generator.i18n.AppLanguage
import com.lotto7.generator.i18n.ProvideStrings
import com.lotto7.generator.i18n.S
import com.lotto7.generator.ui.components.LottoBanner
import com.lotto7.generator.ui.navigation.AppScreen
import com.lotto7.generator.ui.screens.HistoryScreen
import com.lotto7.generator.ui.screens.LookupScreen
import com.lotto7.generator.ui.screens.LottoScreen
import com.lotto7.generator.ui.screens.SettingsScreen
import com.lotto7.generator.ui.screens.WinningScreen
import com.lotto7.generator.ui.theme.Lotto7GeneratorTheme
import com.lotto7.generator.ui.theme.LottoPrimary
import com.lotto7.generator.ui.theme.LottoSurface

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra(EXTRA_SET_LANG)?.let { code ->
            viewModel.setLanguage(AppLanguage.fromCode(code))
        }
        setContent {
            val language by viewModel.language.collectAsState()
            Lotto7GeneratorTheme {
                ProvideStrings(language = language) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SET_LANG = "set_lang"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: AppViewModel) {
    val s = S.current
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.LOTTO) }

    val lottoState by viewModel.lottoState.collectAsState()
    val winningState by viewModel.winningState.collectAsState()
    val lookupState by viewModel.lookupState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val language by viewModel.language.collectAsState()

    val title = when (currentScreen) {
        AppScreen.LOTTO -> s.navLotto
        AppScreen.WINNING -> s.navWinning
        AppScreen.LOOKUP -> s.navLookup
        AppScreen.HISTORY -> s.navHistory
        AppScreen.SETTINGS -> s.navSettings
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LottoPrimary,
                    titleContentColor = LottoSurface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.LOTTO,
                    onClick = { currentScreen = AppScreen.LOTTO },
                    icon = { Icon(Icons.Default.Casino, contentDescription = s.navLotto) },
                    label = { Text(s.navLotto) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.WINNING,
                    onClick = {
                        currentScreen = AppScreen.WINNING
                        viewModel.refreshWinningNumbers()
                    },
                    icon = { Icon(Icons.Default.Star, contentDescription = s.navWinning) },
                    label = { Text(s.navWinning) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.LOOKUP,
                    onClick = {
                        currentScreen = AppScreen.LOOKUP
                        viewModel.loadLookupPage(lookupState.currentPage)
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = s.navLookup) },
                    label = { Text(s.navLookup) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.HISTORY,
                    onClick = {
                        currentScreen = AppScreen.HISTORY
                        viewModel.loadHistoryPage(historyState.currentPage)
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = s.navHistory) },
                    label = { Text(s.navHistory) }
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { currentScreen = AppScreen.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = s.navSettings) },
                    label = { Text(s.navSettings) }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LottoBanner()
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    AppScreen.LOTTO -> {
                        if (lottoState.isLoading) {
                            LoadingBox(error = lottoState.errorMessage)
                        } else {
                            LottoScreen(
                                padding = androidx.compose.foundation.layout.PaddingValues(),
                                uiState = lottoState,
                                onRegenerate = viewModel::regenerate,
                                onMonthSelected = viewModel::setMonth
                            )
                        }
                    }
                    AppScreen.WINNING -> WinningScreen(
                        padding = androidx.compose.foundation.layout.PaddingValues(),
                        uiState = winningState,
                        onAdd = { viewModel.openWinningDialog() },
                        onEdit = viewModel::openWinningDialog,
                        onDelete = viewModel::requestDeleteWinning,
                        onConfirmDelete = viewModel::confirmDeleteWinning,
                        onCancelDelete = viewModel::cancelDeleteWinning,
                        onSave = viewModel::saveWinning,
                        onDismissDialog = viewModel::closeWinningDialog,
                        onRoundChange = viewModel::updateRoundInput,
                        onDateChange = viewModel::updateDateInput,
                        onNumbersChange = viewModel::updateNumbersInput
                    )
                    AppScreen.LOOKUP -> LookupScreen(
                        padding = androidx.compose.foundation.layout.PaddingValues(),
                        uiState = lookupState,
                        onSearchChange = viewModel::setLookupSearch,
                        onLoadPage = viewModel::loadLookupPage
                    )
                    AppScreen.HISTORY -> HistoryScreen(
                        padding = androidx.compose.foundation.layout.PaddingValues(),
                        uiState = historyState,
                        onLoadPage = viewModel::loadHistoryPage
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        padding = androidx.compose.foundation.layout.PaddingValues(),
                        uiState = settingsState,
                        currentLanguage = language,
                        onLanguageSelected = viewModel::setLanguage,
                        onAutoRegisterExcel = viewModel::autoRegisterFromExcel,
                        onFetchOfficial = viewModel::fetchFromOfficialSite
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBox(error: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (error != null) {
            Text(
                text = error,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            CircularProgressIndicator(color = LottoPrimary)
        }
    }
}
