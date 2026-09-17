package com.misgastos.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.misgastos.app.di.LocalAppContainer
import com.misgastos.app.domain.util.CurrencyFormatter
import com.misgastos.app.ui.components.BudgetProgressCard
import com.misgastos.app.ui.components.DateFilterRow
import com.misgastos.app.ui.components.KpiCard
import com.misgastos.app.ui.components.SectionTitle
import com.misgastos.app.ui.components.TrendChip
import com.misgastos.app.ui.components.charts.BarChart
import com.misgastos.app.ui.components.charts.BarEntry
import com.misgastos.app.ui.components.charts.DonutChart
import com.misgastos.app.ui.components.charts.DonutEntry
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun DashboardScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToAnalysis: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DashboardViewModel(container.expenseRepository, container.budgetRepository) }
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            androidx.compose.foundation.layout.Column {
                Text("Mis Gastos", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Cuánto llevas gastado y en qué",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Button(
                onClick = onNavigateToAddExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(" Agregar gasto", style = MaterialTheme.typography.titleMedium)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "GASTO DEL DÍA",
                    value = CurrencyFormatter.format(state.todayTotal),
                    icon = "📅",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "GASTO DE LA SEMANA",
                    value = CurrencyFormatter.format(state.weekTotal),
                    icon = "🗓️",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "GASTO DEL MES",
                    value = CurrencyFormatter.format(state.monthTotal),
                    icon = "📆",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "GASTO DEL AÑO",
                    value = CurrencyFormatter.format(state.yearTotal),
                    icon = "📈",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            BudgetProgressCard(
                title = "Presupuesto del mes",
                spent = state.monthTotal,
                budget = state.monthlyBudget
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.OutlinedButton(onClick = onNavigateToAnalysis, modifier = Modifier.weight(1f)) {
                    Text("📊 Ver análisis")
                }
                androidx.compose.material3.OutlinedButton(onClick = onNavigateToHistory, modifier = Modifier.weight(1f)) {
                    Text("📋 Ver historial")
                }
            }
        }

        item { SectionTitle("Indicadores por periodo") }
        item {
            DateFilterRow(selected = state.selectedFilter, onSelected = viewModel::selectFilter)
        }
        item {
            TrendChip(percentChange = state.comparison?.percentChange)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "CATEGORÍA TOP",
                    value = state.selectedStats.topCategory?.let { "${it.icon} ${it.name}" } ?: "—",
                    subtitle = state.selectedStats.topCategory?.let { CurrencyFormatter.format(it.total) },
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "ESTABLECIMIENTO TOP",
                    value = state.selectedStats.topEstablishment?.name ?: "—",
                    subtitle = state.selectedStats.topEstablishment?.let { CurrencyFormatter.format(it.total) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.selectedStats.byCategory.isNotEmpty()) {
            item { SectionTitle("Distribución por categoría") }
            item {
                DonutChart(
                    data = state.selectedStats.byCategory.map {
                        DonutEntry(it.name, it.total, colorFromHex(it.colorHex), it.icon)
                    },
                    centerLabel = CurrencyFormatter.format(state.selectedStats.total)
                )
            }
        }

        if (state.selectedStats.byDay.isNotEmpty()) {
            item { SectionTitle("Gasto por día") }
            item {
                BarChart(
                    data = state.selectedStats.byDay.map {
                        BarEntry(it.date.dayOfMonth.toString(), it.total)
                    }
                )
            }
        }

        item { androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp)) }
    }
}

fun colorFromHex(hex: String): ComposeColor = runCatching {
    ComposeColor(android.graphics.Color.parseColor(hex))
}.getOrDefault(ComposeColor(0xFF78909C))
