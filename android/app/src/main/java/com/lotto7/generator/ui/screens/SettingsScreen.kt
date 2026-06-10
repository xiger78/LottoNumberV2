package com.lotto7.generator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lotto7.generator.SettingsUiState
import com.lotto7.generator.i18n.AppLanguage
import com.lotto7.generator.i18n.S

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    padding: PaddingValues,
    uiState: SettingsUiState,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onAutoRegisterExcel: () -> Unit,
    onFetchOfficial: () -> Unit
) {
    val s = S.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = s.settingsTitle, style = MaterialTheme.typography.titleMedium)

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

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = s.autoRegisterExcel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Text(
            text = s.autoRegisterExcelDesc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onAutoRegisterExcel,
            enabled = !uiState.isImporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                if (uiState.isImporting) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(s.autoRegisterExcel)
            }
        }

        Text(
            text = s.fetchOfficial,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
        )
        Text(
            text = s.fetchOfficialDesc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(
            onClick = onFetchOfficial,
            enabled = !uiState.isImporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(s.fetchOfficial)
            }
        }

        uiState.importMessage?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.importError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}
