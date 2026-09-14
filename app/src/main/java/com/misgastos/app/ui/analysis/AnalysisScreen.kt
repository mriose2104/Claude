package com.misgastos.app.ui.analysis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.misgastos.app.di.LocalAppContainer

private val tabTitles = listOf("Día", "Semana", "Mes", "Año", "Comidas")

@Composable
fun AnalysisScreen() {
    val container = LocalAppContainer.current
    val viewModel: AnalysisViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AnalysisViewModel(container.expenseRepository, container.catalogRepository) }
        }
    )

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Análisis",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DayTab(viewModel)
                1 -> WeekTab(viewModel)
                2 -> MonthTab(viewModel)
                3 -> YearTab(viewModel)
                4 -> ComidasTab(viewModel)
            }
        }
    }
}
