package com.lotto7.generator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lotto7.generator.i18n.AppLanguage
import com.lotto7.generator.i18n.S

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    padding: PaddingValues,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val s = S.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = s.settingsTitle,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = s.languageLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppLanguage.values().forEach { lang ->
                FilterChip(
                    selected = currentLanguage == lang,
                    onClick = { onLanguageSelected(lang) },
                    label = { Text(lang.label) }
                )
            }
        }
    }
}
