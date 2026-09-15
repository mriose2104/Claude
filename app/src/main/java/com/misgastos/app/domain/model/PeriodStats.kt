package com.misgastos.app.domain.model

import java.time.LocalDate

data class NamedStat(
    val id: Long,
    val name: String,
    val icon: String,
    val colorHex: String,
    val total: Double,
    val percentOfTotal: Double,
    val count: Int
)

data class DayTotal(val date: LocalDate, val total: Double)

data class PeriodStats(
    val range: DateRange,
    val total: Double,
    val count: Int,
    val byDay: List<DayTotal>,
    val byCategory: List<NamedStat>,
    val byEstablishment: List<NamedStat>,
    val byPaymentMethod: List<NamedStat>
) {
    val averageDaily: Double
        get() {
            val days = com.misgastos.app.domain.util.DateRangeUtils.run { range.dayCount() }
            return if (days > 0) total / days else 0.0
        }

    val topCategory: NamedStat? get() = byCategory.maxByOrNull { it.total }
    val topEstablishment: NamedStat? get() = byEstablishment.maxByOrNull { it.total }
    val maxDay: DayTotal? get() = byDay.maxByOrNull { it.total }
    val minDay: DayTotal? get() = byDay.filter { it.total > 0 }.minByOrNull { it.total }

    companion object {
        fun empty(range: DateRange) = PeriodStats(range, 0.0, 0, emptyList(), emptyList(), emptyList(), emptyList())
    }
}

/** Comparison of a period's total against the equivalent previous period. */
data class PeriodComparison(
    val currentTotal: Double,
    val previousTotal: Double
) {
    val delta: Double get() = currentTotal - previousTotal
    val percentChange: Double?
        get() = if (previousTotal == 0.0) null else (delta / previousTotal) * 100.0
    val increased: Boolean get() = delta > 0
}
