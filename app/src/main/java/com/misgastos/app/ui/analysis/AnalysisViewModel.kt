package com.misgastos.app.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.repository.CatalogRepository
import com.misgastos.app.data.repository.ExpenseRepository
import com.misgastos.app.domain.model.DateRange
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.model.DayAnalysis
import com.misgastos.app.domain.model.MonthPoint
import com.misgastos.app.domain.model.NamedStat
import com.misgastos.app.domain.model.PeriodStats
import com.misgastos.app.domain.util.DateRangeUtils
import com.misgastos.app.domain.util.StatsCalculator
import java.time.LocalDate
import java.time.Year
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AnalysisViewModel(
    private val expenseRepository: ExpenseRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private fun <T> flowState(initial: T, block: () -> kotlinx.coroutines.flow.Flow<T>) =
        block().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)

    // ---------- Día ----------
    private val _dayFilter = MutableStateFlow<DateRangeFilter>(DateRangeFilter.ThisWeek)
    val dayFilter: StateFlow<DateRangeFilter> = _dayFilter
    fun setDayFilter(filter: DateRangeFilter) { _dayFilter.value = filter }

    val dayStats: StateFlow<PeriodStats> = flowState(PeriodStats.empty(DateRangeUtils.resolve(DateRangeFilter.ThisWeek))) {
        _dayFilter.flatMapLatest { filter -> expenseRepository.observeStats(DateRangeUtils.resolve(filter)) }
    }

    val dayList: StateFlow<List<DayAnalysis>> = flowState(emptyList()) {
        _dayFilter.flatMapLatest { filter ->
            val range = DateRangeUtils.resolve(filter)
            expenseRepository.observeExpensesBetween(range).map { expenses ->
                expenses.map { it.expense.date }.distinct().sortedDescending()
                    .map { date -> StatsCalculator.dayAnalysis(expenses, date) }
            }
        }
    }

    // ---------- Semana ----------
    private val _weekAnchor = MutableStateFlow(LocalDate.now())
    val weekAnchor: StateFlow<LocalDate> = _weekAnchor
    fun previousWeek() { _weekAnchor.value = _weekAnchor.value.minusWeeks(1) }
    fun nextWeek() { _weekAnchor.value = _weekAnchor.value.plusWeeks(1) }
    fun currentWeek() { _weekAnchor.value = LocalDate.now() }

    val weekStats: StateFlow<PeriodStats> = flowState(PeriodStats.empty(DateRangeUtils.weekRange(LocalDate.now()))) {
        _weekAnchor.flatMapLatest { anchor ->
            val range = DateRangeUtils.weekRange(anchor)
            expenseRepository.observeStats(range)
        }
    }

    // ---------- Mes ----------
    private val _monthAnchor = MutableStateFlow(LocalDate.now())
    val monthAnchor: StateFlow<LocalDate> = _monthAnchor
    fun previousMonth() { _monthAnchor.value = _monthAnchor.value.minusMonths(1) }
    fun nextMonth() { _monthAnchor.value = _monthAnchor.value.plusMonths(1) }
    fun currentMonth() { _monthAnchor.value = LocalDate.now() }

    val monthStats: StateFlow<PeriodStats> = flowState(PeriodStats.empty(DateRangeUtils.monthRange(LocalDate.now()))) {
        _monthAnchor.flatMapLatest { anchor ->
            expenseRepository.observeStats(DateRangeUtils.monthRange(anchor))
        }
    }

    val monthEvolution: StateFlow<List<MonthPoint>> = flowState(emptyList()) {
        _monthAnchor.flatMapLatest { anchor ->
            val start = anchor.minusMonths(5).withDayOfMonth(1)
            val end = anchor.withDayOfMonth(anchor.lengthOfMonth())
            expenseRepository.observeExpensesBetween(DateRange(start, end)).map { StatsCalculator.byMonth(it) }
        }
    }

    // ---------- Año ----------
    private val _yearAnchor = MutableStateFlow(Year.now().value)
    val yearAnchor: StateFlow<Int> = _yearAnchor
    fun previousYear() { _yearAnchor.value -= 1 }
    fun nextYear() { _yearAnchor.value += 1 }
    fun currentYear() { _yearAnchor.value = Year.now().value }

    val yearEvolution: StateFlow<List<MonthPoint>> = flowState(emptyList()) {
        _yearAnchor.flatMapLatest { year ->
            val range = DateRangeUtils.yearRange(LocalDate.of(year, 6, 15))
            expenseRepository.observeExpensesBetween(range).map { StatsCalculator.byMonth(it) }
        }
    }

    val yearStats: StateFlow<PeriodStats> = flowState(PeriodStats.empty(DateRangeUtils.yearRange(LocalDate.now()))) {
        _yearAnchor.flatMapLatest { year ->
            expenseRepository.observeStats(DateRangeUtils.yearRange(LocalDate.of(year, 6, 15)))
        }
    }

    // ---------- Comidas ----------
    private val _comidasFilter = MutableStateFlow<DateRangeFilter>(DateRangeFilter.ThisMonth)
    val comidasFilter: StateFlow<DateRangeFilter> = _comidasFilter
    fun setComidasFilter(filter: DateRangeFilter) { _comidasFilter.value = filter }

    private val comidasCategoryId = catalogRepository.observeCategories().map { categories ->
        categories.firstOrNull { it.name.contains("comida", ignoreCase = true) }?.id ?: categories.firstOrNull()?.id
    }

    val comidasBreakdown: StateFlow<List<NamedStat>> = flowState(emptyList()) {
        combine(_comidasFilter, comidasCategoryId) { filter, categoryId -> filter to categoryId }
            .flatMapLatest { (filter, categoryId) ->
                if (categoryId == null) {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                } else {
                    val range = DateRangeUtils.resolve(filter)
                    expenseRepository.observeExpensesBetween(range)
                        .map { StatsCalculator.establishmentBreakdownForCategory(it, categoryId) }
                }
            }
    }
}
