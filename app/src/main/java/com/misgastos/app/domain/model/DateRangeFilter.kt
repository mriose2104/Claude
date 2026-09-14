package com.misgastos.app.domain.model

import java.time.LocalDate

/** Quick date filters used across Dashboard, Analysis and History. */
sealed class DateRangeFilter(val label: String) {
    data object Today : DateRangeFilter("Hoy")
    data object ThisWeek : DateRangeFilter("Esta semana")
    data object ThisMonth : DateRangeFilter("Este mes")
    data object ThisYear : DateRangeFilter("Este año")
    data object PreviousMonth : DateRangeFilter("Mes anterior")
    data class Custom(val start: LocalDate, val end: LocalDate) : DateRangeFilter("Rango personalizado")

    companion object {
        val quickFilters: List<DateRangeFilter> = listOf(Today, ThisWeek, ThisMonth, ThisYear, PreviousMonth)
    }
}

data class DateRange(val start: LocalDate, val end: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
}
