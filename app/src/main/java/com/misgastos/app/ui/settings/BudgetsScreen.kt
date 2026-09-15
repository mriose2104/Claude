package com.misgastos.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.misgastos.app.di.LocalAppContainer
import com.misgastos.app.ui.components.BudgetProgressCard
import com.misgastos.app.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: BudgetsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { BudgetsViewModel(container.budgetRepository, container.catalogRepository, container.expenseRepository) }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var monthlyBudgetText by remember(state.monthlyBudget) { mutableStateOf(if (state.monthlyBudget > 0) state.monthlyBudget.toInt().toString() else "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presupuestos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item { SectionTitle("Presupuesto mensual general") }
            item {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = monthlyBudgetText,
                        onValueChange = { monthlyBudgetText = it.filter { c -> c.isDigit() } },
                        label = { Text("Monto mensual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    TextButton(onClick = { viewModel.setMonthlyBudget(monthlyBudgetText.toDoubleOrNull() ?: 0.0) }) {
                        Text("Guardar")
                    }
                }
            }
            item {
                BudgetProgressCard(title = "Este mes", spent = state.monthSpent, budget = state.monthlyBudget)
            }

            item { SectionTitle("Presupuesto por categoría") }
            items(state.categoryRows, key = { it.category.id }) { row ->
                CategoryBudgetCard(row = row, onSave = { amount -> viewModel.setCategoryBudget(row.category.id, amount) })
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(row: CategoryBudgetRow, onSave: (Double) -> Unit) {
    var text by remember(row.budgetAmount) { mutableStateOf(if (row.budgetAmount > 0) row.budgetAmount.toInt().toString() else "") }

    Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Text("${row.category.icon} ${row.category.name}", style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() } },
                    label = { Text("Presupuesto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                TextButton(onClick = { onSave(text.toDoubleOrNull() ?: 0.0) }) { Text("Guardar") }
            }
            if (row.budgetAmount > 0) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                BudgetProgressCard(title = "Progreso", spent = row.spent, budget = row.budgetAmount)
            }
        }
    }
}
