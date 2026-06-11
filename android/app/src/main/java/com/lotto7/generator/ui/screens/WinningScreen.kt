package com.lotto7.generator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lotto7.generator.WinningUiState
import com.lotto7.generator.data.WinningNumberEntity
import com.lotto7.generator.i18n.S
import com.lotto7.generator.ui.components.NumberBalls

@Composable
fun WinningScreen(
    padding: PaddingValues,
    uiState: WinningUiState,
    onAdd: () -> Unit,
    onEdit: (WinningNumberEntity) -> Unit,
    onDelete: (WinningNumberEntity) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onSave: () -> Unit,
    onDismissDialog: () -> Unit,
    onRoundChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onNumbersChange: (String) -> Unit
) {
    val s = S.current

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = s.addWinning)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (uiState.totalCount > 0) {
                Text(
                    text = String.format(s.lookupTotal, uiState.totalCount),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (uiState.items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.emptyWinning, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        WinningCard(
                            item = item,
                            onEdit = { onEdit(item) },
                            onDelete = { onDelete(item) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(if (uiState.editing == null) s.addWinning else s.editWinning) },
            text = {
                Column {
                    OutlinedTextField(
                        value = uiState.roundInput,
                        onValueChange = onRoundChange,
                        label = { Text(s.roundLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.dateInput,
                        onValueChange = onDateChange,
                        label = { Text(s.drawDateLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.numbersInput,
                        onValueChange = onNumbersChange,
                        label = { Text(s.numbersLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("01 05 12 17 23 28 34") }
                    )
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(s.invalidNumbers, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onSave) { Text(s.save) }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) { Text(s.cancel) }
            }
        )
    }

    uiState.confirmDelete?.let {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(s.deleteWinning) },
            text = { Text(s.confirmDelete) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) { Text(s.delete) }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) { Text(s.cancel) }
            }
        )
    }
}

@Composable
private fun WinningCard(
    item: WinningNumberEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val nums = item.numbers.split(",").mapNotNull { it.trim().toIntOrNull() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.roundLabel,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = item.drawDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            NumberBalls(nums)
        }
    }
}
