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
import com.misgastos.app.ui.components.charts.BarChart
import com.misgastos.app.ui.components.charts.BarEntry
import com.misgastos.app.ui.components.charts.DonutChart
import com.misgastos.app.ui.components.charts.DonutEntry
import com.misgastos.app.ui.components.charts.LineChart
import com.misgastos.app.ui.components.charts.LinePoint
import com.misgastos.app.ui.dashboard.colorFromHex
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthTab(viewModel: AnalysisViewModel) {
    val anchor by viewModel.monthAnchor.collectAsStateWithLifecycle()
    val stats by viewModel.monthStats.collectAsStateWithLifecycle()
    val evolution by viewModel.monthEvolution.collectAsStateWithLifecycle()

    val monthLabel = anchor.month.getDisplayName(TextStyle.FULL, Locale("es", "MX")).replaceFirstChar { it.uppercase() } + " ${anchor.year}"

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
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                }
                Text(monthLabel, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Mes siguiente")
                }
            }
        }

        item {
            KpiCard(title = "TOTAL GASTADO", value = CurrencyFormatter.format(stats.total))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(title = "PROMEDIO DIARIO", value = CurrencyFormatter.format(stats.averageDaily), modifier = Modifier.weight(1f))
                KpiCard(title = "MOVIMIENTOS", value = stats.count.toString(), modifier = Modifier.weight(1f))
            }
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

        if (stats.byEstablishment.isNotEmpty()) {
            item { SectionTitle("Gastos por establecimiento") }
            item {
                BarChart(data = stats.byEstablishment.take(10).map { BarEntry(it.name, it.total) })
            }
        }

        if (stats.byDay.isNotEmpty()) {
            item { SectionTitle("Gastos por día") }
            item {
                BarChart(data = stats.byDay.map { BarEntry(it.date.dayOfMonth.toString(), it.total) })
            }
        }

        if (evolution.size > 1) {
            item { SectionTitle("Comparación con meses anteriores") }
            item {
                LineChart(data = evolution.map { LinePoint(it.monthLabel, it.total) })
            }
        }
    }
}
