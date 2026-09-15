package com.misgastos.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.repository.BudgetRepository
import com.misgastos.app.data.repository.ExpenseRepository
import com.misgastos.app.domain.model.DateRange
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.model.PeriodComparison
import com.misgastos.app.domain.model.PeriodStats
import com.misgastos.app.domain.util.DateRangeUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val loading: Boolean = true,
    val todayTotal: Double = 0.0,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val yearTotal: Double = 0.0,
    val selectedFilter: DateRangeFilter = DateRangeFilter.ThisMonth,
    val selectedStats: PeriodStats = PeriodStats.empty(DateRangeUtils.resolve(DateRangeFilter.ThisMonth)),
    val comparison: PeriodComparison? = null,
    val monthlyBudget: Double = 0.0,
    val totalExpenseCount: Int = 0,
    // These reflect your whole spending history (not the period filter above), so switching
    // filters like "Hoy" doesn't distort them by projecting a single day's total forward.
    val weeklyAverage: Double = 0.0,
    val monthlyAverage: Double = 0.0
)

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val selectedFilter = MutableStateFlow<DateRangeFilter>(DateRangeFilter.ThisMonth)

    fun selectFilter(filter: DateRangeFilter) {
        selectedFilter.value = filter
    }

    private val todayStats = expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.Today, today))
    private val weekStats = expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.ThisWeek, today))
    private val monthStats = expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.ThisMonth, today))
    private val yearStats = expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.ThisYear, today))

    private val quickTotals = combine(todayStats, weekStats, monthStats, yearStats) { t, w, m, y ->
        QuickTotals(t.total, w.total, m.total, y.total)
    }

    private val selectedStatsFlow = selectedFilter.flatMapLatest { filter ->
        expenseRepository.observeStats(DateRangeUtils.resolve(filter, today))
    }

    private val comparisonFlow = selectedFilter.flatMapLatest { filter ->
        val range = DateRangeUtils.resolve(filter, today)
        val previousRange = DateRangeUtils.previousEquivalentRange(range)
        combine(
            expenseRepository.observeStats(range),
            expenseRepository.observeStats(previousRange)
        ) { current, previous -> PeriodComparison(current.total, previous.total) }
    }

    private val budgetFlow = budgetRepository.observeMonthlyBudget()
    private val countFlow = expenseRepository.observeCount()

    // All-time daily pace, used to derive the weekly/monthly average cards below —
    // independent of whichever quick filter is selected above.
    private val averagesFlow = expenseRepository.observeFirstExpenseDate()
        .flatMapLatest { firstDate ->
            expenseRepository.observeStats(DateRange(firstDate ?: today, today))
        }
        .map { allTimeStats -> Averages(allTimeStats.averageDaily * 7, allTimeStats.averageDaily * 30) }

    private val partialState = combine(
        quickTotals, selectedFilter, selectedStatsFlow, comparisonFlow, budgetFlow
    ) { quick, filter, stats, comparison, budget ->
        DashboardUiState(
            loading = false,
            todayTotal = quick.today,
            weekTotal = quick.week,
            monthTotal = quick.month,
            yearTotal = quick.year,
            selectedFilter = filter,
            selectedStats = stats,
            comparison = comparison,
            monthlyBudget = budget
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(partialState, countFlow, averagesFlow) { state, count, averages ->
        state.copy(totalExpenseCount = count, weeklyAverage = averages.weekly, monthlyAverage = averages.monthly)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private data class QuickTotals(val today: Double, val week: Double, val month: Double, val year: Double)
    private data class Averages(val weekly: Double, val monthly: Double)
}
