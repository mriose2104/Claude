package com.misgastos.app.ui.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misgastos.app.domain.util.CurrencyFormatter
import com.misgastos.app.ui.components.KpiCard
import com.misgastos.app.ui.components.SectionTitle
import com.misgastos.app.ui.components.charts.DonutChart
import com.misgastos.app.ui.components.charts.DonutEntry
import com.misgastos.app.ui.components.charts.LineChart
import com.misgastos.app.ui.components.charts.LinePoint
import com.misgastos.app.ui.dashboard.colorFromHex

@Composable
fun YearTab(viewModel: AnalysisViewModel) {
    val year by viewModel.yearAnchor.collectAsStateWithLifecycle()
    val evolution by viewModel.yearEvolution.collectAsStateWithLifecycle()
    val stats by viewModel.yearStats.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousYear) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Año anterior")
                }
                Text(year.toString(), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = viewModel::nextYear) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Año siguiente")
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(title = "TOTAL DEL AÑO", value = CurrencyFormatter.format(stats.total), modifier = Modifier.weight(1f))
                KpiCard(title = "PROMEDIO MENSUAL", value = CurrencyFormatter.format(stats.averageDaily * 30), modifier = Modifier.weight(1f))
            }
        }

        if (evolution.isNotEmpty()) {
            item { SectionTitle("Evolución mensual") }
            item { LineChart(data = evolution.map { LinePoint(it.monthLabel, it.total) }) }
        }

        if (stats.byCategory.isNotEmpty()) {
            item { SectionTitle("Gastos por categoría") }
            item {
                DonutChart(
                    data = stats.byCategory.map { DonutEntry(it.name, it.total, colorFromHex(it.colorHex), it.icon) },
                    centerLabel = CurrencyFormatter.format(stats.total)
                )
            }
        }
    }
}
