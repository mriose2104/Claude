package com.misgastos.app.domain.util

import com.misgastos.app.domain.model.DateRange
import com.misgastos.app.domain.model.DateRangeFilter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object DateRangeUtils {

    /** Resolves a [DateRangeFilter] into a concrete [DateRange], anchored on [today]. */
    fun resolve(filter: DateRangeFilter, today: LocalDate = LocalDate.now()): DateRange = when (filter) {
        DateRangeFilter.Today -> DateRange(today, today)
        DateRangeFilter.ThisWeek -> weekRange(today)
        DateRangeFilter.ThisMonth -> monthRange(today)
        DateRangeFilter.ThisYear -> yearRange(today)
        DateRangeFilter.PreviousMonth -> monthRange(today.minusMonths(1))
        is DateRangeFilter.Custom -> if (filter.start.isAfter(filter.end)) {
            DateRange(filter.end, filter.start)
        } else {
            DateRange(filter.start, filter.end)
        }
    }

    fun weekRange(date: LocalDate): DateRange {
        val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        return DateRange(start, end)
    }

    fun monthRange(date: LocalDate): DateRange {
        val start = date.with(TemporalAdjusters.firstDayOfMonth())
        val end = date.with(TemporalAdjusters.lastDayOfMonth())
        return DateRange(start, end)
    }

    fun yearRange(date: LocalDate): DateRange {
        val start = date.withDayOfYear(1)
        val end = date.withMonth(12).withDayOfMonth(31)
        return DateRange(start, end)
    }

    /** Number of calendar days spanned by the range, inclusive on both ends. */
    fun DateRange.dayCount(): Long = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1

    /** The previous period of equal length immediately preceding [range] (used for period-over-period comparisons). */
    fun previousEquivalentRange(range: DateRange): DateRange {
        val length = range.dayCount()
        val newEnd = range.start.minusDays(1)
        val newStart = newEnd.minusDays(length - 1)
        return DateRange(newStart, newEnd)
    }
}
