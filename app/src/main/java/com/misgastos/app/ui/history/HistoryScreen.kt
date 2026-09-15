package com.misgastos.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.misgastos.app.di.LocalAppContainer
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.util.CurrencyFormatter
import com.misgastos.app.ui.components.DateFilterRow
import com.misgastos.app.ui.components.EmptyState
import com.misgastos.app.ui.components.ExpenseRow

@Composable
fun HistoryScreen(onEditExpense: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: HistoryViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HistoryViewModel(container.expenseRepository, container.catalogRepository) }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showSortMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showPaymentMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Text("Historial", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            OutlinedTextField(
                value = state.filters.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Buscar por establecimiento, categoría o nota") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.filters.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.filters.dateFilter == null,
                    onClick = { viewModel.setDateFilter(null) },
                    label = { Text("Todos") }
                )
                DateFilterRow(
                    selected = state.filters.dateFilter ?: DateRangeFilter.ThisMonth,
                    onSelected = { viewModel.setDateFilter(it) }
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    FilterChip(
                        selected = state.filters.categoryId != null,
                        onClick = { showCategoryMenu = true },
                        label = {
                            Text(
                                state.categories.find { it.id == state.filters.categoryId }?.name ?: "Categoría"
                            )
                        }
                    )
                    DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                        DropdownMenuItem(text = { Text("Todas") }, onClick = {
                            viewModel.setCategoryFilter(null)
                            showCategoryMenu = false
                        })
                        state.categories.forEach { category ->
                            DropdownMenuItem(text = { Text("${category.icon} ${category.name}") }, onClick = {
                                viewModel.setCategoryFilter(category.id)
                                showCategoryMenu = false
                            })
                        }
                    }
                }

                Box {
                    FilterChip(
                        selected = state.filters.paymentMethodId != null,
                        onClick = { showPaymentMenu = true },
                        label = {
                            Text(
                                state.paymentMethods.find { it.id == state.filters.paymentMethodId }?.name ?: "Forma de pago"
                            )
                        }
                    )
                    DropdownMenu(expanded = showPaymentMenu, onDismissRequest = { showPaymentMenu = false }) {
                        DropdownMenuItem(text = { Text("Todas") }, onClick = {
                            viewModel.setPaymentMethodFilter(null)
                            showPaymentMenu = false
                        })
                        state.paymentMethods.forEach { method ->
                            DropdownMenuItem(text = { Text("${method.icon} ${method.name}") }, onClick = {
                                viewModel.setPaymentMethodFilter(method.id)
                                showPaymentMenu = false
                            })
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = "Ordenar")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(text = { Text(option.label) }, onClick = {
                                viewModel.setSortOption(option)
                                showSortMenu = false
                            })
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${state.expenses.size} movimientos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(CurrencyFormatter.format(state.totalAmount), style = MaterialTheme.typography.titleSmall)
            }
        }
        item { HorizontalDivider() }

        if (state.expenses.isEmpty()) {
            item {
                EmptyState(
                    icon = "📋",
                    title = "No hay movimientos",
                    subtitle = "Ajusta los filtros o registra un nuevo gasto."
                )
            }
        }

        items(state.expenses, key = { it.expense.id }) { expense ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                ExpenseRow(
                    expense = expense,
                    modifier = Modifier.weight(1f),
                    onClick = { onEditExpense(expense.expense.id) },
                    showDate = true
                )
                IconButton(onClick = { viewModel.requestDelete(expense) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
            HorizontalDivider()
        }
    }

    state.pendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("¿Eliminar este gasto?") },
            text = { Text("${expense.establishmentName} · ${CurrencyFormatter.format(expense.expense.amount)}. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Cancelar") }
            }
        )
    }
}
