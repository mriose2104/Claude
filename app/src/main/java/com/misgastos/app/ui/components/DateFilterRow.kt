package com.misgastos.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.model.quickDateRangeFilters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterRow(
    selected: DateRangeFilter,
    onSelected: (DateRangeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomPicker by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
    ) {
        items(quickDateRangeFilters) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter.label) }
            )
        }
        item {
            FilterChip(
                selected = selected is DateRangeFilter.Custom,
                onClick = { showCustomPicker = true },
                label = { Text(if (selected is DateRangeFilter.Custom) "Rango elegido" else "Rango personalizado") }
            )
        }
    }

    if (showCustomPicker) {
        val state = rememberDateRangePickerState()
        Dialog(onDismissRequest = { showCustomPicker = false }) {
            androidx.compose.material3.Surface(shape = MaterialTheme.shapes.large) {
                androidx.compose.foundation.layout.Column {
                    DateRangePicker(
                        state = state,
                        modifier = Modifier
                            .padding(8.dp)
                            .height(500.dp)
                    )
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomPicker = false }) { Text("Cancelar") }
                        Button(onClick = {
                            val startMillis = state.selectedStartDateMillis
                            val endMillis = state.selectedEndDateMillis
                            if (startMillis != null) {
                                val start = Instant.ofEpochMilli(startMillis).atZone(ZoneOffset.UTC).toLocalDate()
                                val end = endMillis?.let {
                                    Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                                } ?: start
                                onSelected(DateRangeFilter.Custom(start, end))
                            }
                            showCustomPicker = false
                        }) { Text("Aplicar") }
                    }
                }
            }
        }
    }
}

fun LocalDate.toEpochMillisUtc(): Long = this.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
