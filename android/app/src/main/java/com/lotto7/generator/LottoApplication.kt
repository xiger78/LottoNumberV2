package com.lotto7.generator

import android.app.Application
import com.lotto7.generator.data.AppDatabase
import com.lotto7.generator.data.AppRepository
import com.lotto7.generator.data.SettingsStore

class LottoApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val settingsStore by lazy { SettingsStore(this) }
    val repository by lazy {
        AppRepository(
            context = this,
            winningDao = database.winningNumberDao(),
            historyDao = database.generationHistoryDao(),
            settingsStore = settingsStore
        )
    }
}
