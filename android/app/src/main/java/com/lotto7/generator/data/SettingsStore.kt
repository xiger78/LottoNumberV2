package com.lotto7.generator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lotto7.generator.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val languageKey = stringPreferencesKey("language")

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        AppLanguage.fromCode(prefs[languageKey] ?: AppLanguage.JA.code)
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[languageKey] = language.code
        }
    }
}
