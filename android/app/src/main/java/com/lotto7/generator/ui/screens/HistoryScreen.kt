package com.lotto7.generator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.lotto7.generator.HistoryUiState
import com.lotto7.generator.i18n.LocalAppLanguage
import com.lotto7.generator.i18n.LocalizedStrings
import com.lotto7.generator.i18n.S

@Composable
fun HistoryScreen(
    padding: PaddingValues,
    uiState: HistoryUiState,
    onLoadPage: (Int) -> Unit
) {
    val s = S.current
    val lang = LocalAppLanguage.current

    LaunchedEffect(Unit) {
        onLoadPage(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(
            text = s.historyTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.items.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.historyEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        val nums = item.numbers.split(",").mapNotNull { it.trim().toIntOrNull() }
                        val line = LocalizedStrings.formatHistoryLine(lang, item.createdAt, nums)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = line,
                                modifier = Modifier.padding(12.dp),
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onLoadPage(uiState.currentPage - 1) },
                        enabled = uiState.currentPage > 0
                    ) {
                        Text(s.prevPage)
                    }
                    Text(
                        text = String.format(s.pageInfo, uiState.currentPage + 1, uiState.totalPages),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { onLoadPage(uiState.currentPage + 1) },
                        enabled = uiState.currentPage < uiState.totalPages - 1
                    ) {
                        Text(s.nextPage)
                    }
                }
            }
        }
    }
}
