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
import com.misgastos.app.domain.util.DateRangeUtils
import com.misgastos.app.domain.util.StatsCalculator
import com.misgastos.app.ui.components.KpiCard
import com.misgastos.app.ui.components.SectionTitle
import com.misgastos.app.ui.components.charts.BarChart
import com.misgastos.app.ui.components.charts.BarEntry
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekTab(viewModel: AnalysisViewModel) {
    val anchor by viewModel.weekAnchor.collectAsStateWithLifecycle()
    val stats by viewModel.weekStats.collectAsStateWithLifecycle()
    val range = DateRangeUtils.weekRange(anchor)
    val dayFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "MX"))

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
                IconButton(onClick = viewModel::previousWeek) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Semana anterior")
                }
                Text(
                    "${range.start.format(dayFormatter)} – ${range.end.format(dayFormatter)}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = viewModel::nextWeek) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Semana siguiente")
                }
            }
        }

        item {
            val weekDays = generateSequence(range.start) { it.plusDays(1) }.takeWhile { !it.isAfter(range.end) }.toList()
            val entries = weekDays.map { date ->
                val total = stats.byDay.find { it.date == date }?.total ?: 0.0
                val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "MX")).replaceFirstChar { it.uppercase() }
                BarEntry(label, total)
            }
            androidx.compose.foundation.layout.Column {
                SectionTitle("Gasto por día")
                BarChart(data = entries)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(title = "TOTAL DE LA SEMANA", value = CurrencyFormatter.format(stats.total), modifier = Modifier.weight(1f))
                KpiCard(title = "PROMEDIO DIARIO", value = CurrencyFormatter.format(stats.averageDaily), modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "DÍA CON MAYOR GASTO",
                    value = stats.maxDay?.let { CurrencyFormatter.format(it.total) } ?: "—",
                    subtitle = stats.maxDay?.date?.format(dayFormatter),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "DÍA CON MENOR GASTO",
                    value = stats.minDay?.let { CurrencyFormatter.format(it.total) } ?: "—",
                    subtitle = stats.minDay?.date?.format(dayFormatter),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "TOTAL COMIDAS",
                    value = CurrencyFormatter.format(StatsCalculator.totalForCategoryLike(stats, "comida")),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "TOTAL SERVICIOS",
                    value = CurrencyFormatter.format(StatsCalculator.totalForCategoryLike(stats, "servicio")),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            KpiCard(title = "NÚMERO DE MOVIMIENTOS", value = stats.count.toString())
        }
    }
}
