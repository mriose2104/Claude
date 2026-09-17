package com.misgastos.app.ui.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import com.misgastos.app.ui.components.SectionTitle
import com.misgastos.app.ui.components.charts.BarChart
import com.misgastos.app.ui.components.charts.BarEntry
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DayTab(viewModel: AnalysisViewModel) {
    val filter by viewModel.dayFilter.collectAsStateWithLifecycle()
    val stats by viewModel.dayStats.collectAsStateWithLifecycle()
    val days by viewModel.dayList.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
    ) {
        item { DateFilterRow(selected = filter, onSelected = viewModel::setDayFilter) }

        if (stats.byDay.isNotEmpty()) {
            item { SectionTitle("Gasto por día") }
            item {
                BarChart(data = stats.byDay.map { BarEntry(it.date.dayOfMonth.toString(), it.total) })
            }
        }

        if (days.isEmpty()) {
            item {
                EmptyState(
                    icon = "🗓️",
                    title = "Sin gastos en este periodo",
                    subtitle = "Los gastos que registres aparecerán agrupados por día."
                )
            }
        }

        items(days) { day ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val dayName = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "MX"))
                    val monthName = day.date.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                    Text(
                        "${dayName.replaceFirstChar { it.uppercase() }} ${day.date.dayOfMonth} $monthName",
                        style = MaterialTheme.typography.titleMedium
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
                    day.groups.forEach { group ->
                        Text(
                            "${group.categoryIcon} ${group.categoryName}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        group.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, top = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.establishmentName, style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyFormatter.format(item.expense.amount), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total del día", style = MaterialTheme.typography.titleSmall)
                        Text(CurrencyFormatter.format(day.total), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}
