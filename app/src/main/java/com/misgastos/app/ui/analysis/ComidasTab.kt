package com.misgastos.app.ui.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgastos.app.domain.util.CurrencyFormatter
import com.misgastos.app.ui.components.DateFilterRow
import com.misgastos.app.ui.components.EmptyState
import com.misgastos.app.ui.components.KpiCard
import com.misgastos.app.ui.components.SectionTitle

@Composable
fun ComidasTab(viewModel: AnalysisViewModel) {
    val filter by viewModel.comidasFilter.collectAsStateWithLifecycle()
    val breakdown by viewModel.comidasBreakdown.collectAsStateWithLifecycle()
    val total = breakdown.sumOf { it.total }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item { DateFilterRow(selected = filter, onSelected = viewModel::setComidasFilter) }
        item { KpiCard(title = "TOTAL EN COMIDAS", value = CurrencyFormatter.format(total)) }

        if (breakdown.isEmpty()) {
            item {
                EmptyState(
                    icon = "🍔",
                    title = "Sin gastos en comidas",
                    subtitle = "Registra un gasto en la categoría Comidas para ver el desglose aquí."
                )
            }
        } else {
            item { SectionTitle("Por establecimiento") }
            items(breakdown) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.name, style = MaterialTheme.typography.titleSmall)
                            Text(CurrencyFormatter.format(entry.total), style = MaterialTheme.typography.titleSmall)
                        }
                        LinearProgressIndicator(
                            progress = { (entry.percentOfTotal / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            "${"%.1f".format(entry.percentOfTotal)}% del total · ${entry.count} movimientos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
