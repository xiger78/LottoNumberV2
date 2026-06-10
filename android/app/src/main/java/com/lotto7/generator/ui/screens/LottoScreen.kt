package com.lotto7.generator.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lotto7.generator.GeneratedCombo
import com.lotto7.generator.LottoUiState
import com.lotto7.generator.OFFICIAL_LOTO7_URL
import com.lotto7.generator.i18n.S
import com.lotto7.generator.ui.components.NumberBalls
import com.lotto7.generator.ui.components.SummaryRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LottoScreen(
    padding: PaddingValues,
    uiState: LottoUiState,
    onRegenerate: () -> Unit,
    onMonthSelected: (Int) -> Unit
) {
    val s = S.current
    val summary = uiState.summary ?: return
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_LOTO7_URL))
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(s.openOfficialSite)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = s.patternAnalysis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow(s.analysisDraws, "${summary.totalDraws}${s.times}")
                    SummaryRow(s.latestRound, "${summary.latestRound} / ${summary.latestDate}")
                    SummaryRow(
                        s.latestWinning,
                        summary.latestNums.joinToString(" ") { "%02d".format(it) }
                    )
                    SummaryRow(s.hotNumbers, summary.hotNumbers.joinToString(", "))
                    SummaryRow(s.coldNumbers, summary.coldNumbers.joinToString(", "))
                    SummaryRow(
                        s.sumAverage,
                        "%.1f (${summary.sumMin}~${summary.sumMax})".format(summary.sumAverage)
                    )
                    SummaryRow(
                        s.oddDistribution,
                        summary.topOddPatterns.joinToString(", ") { "${it.first}(${it.second})" }
                    )
                    if (uiState.savedWinningCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${s.winningConsidered} (${uiState.savedWinningCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = s.monthPattern,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..12).forEach { month ->
                        FilterChip(
                            selected = uiState.selectedMonth == month,
                            onClick = { onMonthSelected(month) },
                            label = { Text(s.monthLabel(month)) }
                        )
                    }
                }
            }
        }

        item {
            Button(onClick = onRegenerate, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(s.generateButton)
            }
        }

        if (uiState.combos.isNotEmpty()) {
            item {
                Text(
                    text = "${s.recommendedCombos} ${uiState.combos.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(uiState.combos) { combo ->
                ComboCard(combo)
            }
        }

        item {
            Text(
                text = s.disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun ComboCard(combo: GeneratedCombo) {
    val s = S.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${combo.index}.  ${s.sumLabel}:${combo.sum}  ${s.oddLabel}:${combo.oddCount}  ${s.kouLabel}:${combo.kouDistribution}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            NumberBalls(combo.numbers)
        }
    }
}
