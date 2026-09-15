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
}

// Kept as a top-level property (not inside DateRangeFilter's own companion object) on purpose:
// referencing sealed-class singletons from their own companion object can hit a JVM class
// initialization race where the listed objects are still null. A top-level property has no such
// superclass-initialization dependency on DateRangeFilter, so it's always safe.
val quickDateRangeFilters: List<DateRangeFilter> = listOf(
    DateRangeFilter.Today,
    DateRangeFilter.ThisWeek,
    DateRangeFilter.ThisMonth,
    DateRangeFilter.ThisYear,
    DateRangeFilter.PreviousMonth
)

data class DateRange(val start: LocalDate, val end: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
}
