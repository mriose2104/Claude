package com.misgastos.app.domain.util

import com.misgastos.app.data.local.relation.ExpenseFull
import com.misgastos.app.domain.model.CategoryGroup
import com.misgastos.app.domain.model.DateRange
import com.misgastos.app.domain.model.DayAnalysis
import com.misgastos.app.domain.model.DayTotal
import com.misgastos.app.domain.model.MonthPoint
import com.misgastos.app.domain.model.NamedStat
import com.misgastos.app.domain.model.PeriodComparison
import com.misgastos.app.domain.model.PeriodStats
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

object StatsCalculator {

    fun compute(expenses: List<ExpenseFull>, range: DateRange): PeriodStats {
        if (expenses.isEmpty()) return PeriodStats.empty(range)

        val total = expenses.sumOf { it.expense.amount }

        val byDay = expenses
            .groupBy { it.expense.date }
            .map { (date, list) -> DayTotal(date, list.sumOf { it.expense.amount }) }
            .sortedBy { it.date }

        val byCategory = expenses
            .groupBy { Triple(it.expense.categoryId, it.categoryName, it.categoryIcon to it.categoryColor) }
            .map { (key, list) ->
                val (id, name, iconColor) = key
                val sum = list.sumOf { it.expense.amount }
                NamedStat(
                    id = id,
                    name = name,
                    icon = iconColor.first,
                    colorHex = iconColor.second,
                    total = sum,
                    percentOfTotal = if (total > 0) sum / total * 100.0 else 0.0,
                    count = list.size
                )
            }
            .sortedByDescending { it.total }

        val byEstablishment = expenses
            .groupBy { Triple(it.expense.establishmentId, it.establishmentName, it.categoryIcon to it.categoryColor) }
            .map { (key, list) ->
                val (id, name, iconColor) = key
                val sum = list.sumOf { it.expense.amount }
                NamedStat(
                    id = id,
                    name = name,
                    icon = iconColor.first,
                    colorHex = iconColor.second,
                    total = sum,
                    percentOfTotal = if (total > 0) sum / total * 100.0 else 0.0,
                    count = list.size
                )
            }
            .sortedByDescending { it.total }

        val byPaymentMethod = expenses
            .groupBy { Triple(it.expense.paymentMethodId, it.paymentMethodName, it.paymentMethodIcon) }
            .map { (key, list) ->
                val (id, name, icon) = key
                val sum = list.sumOf { it.expense.amount }
                NamedStat(
                    id = id,
                    name = name,
                    icon = icon,
                    colorHex = "#78909C",
                    total = sum,
                    percentOfTotal = if (total > 0) sum / total * 100.0 else 0.0,
                    count = list.size
                )
            }
            .sortedByDescending { it.total }

        return PeriodStats(
            range = range,
            total = total,
            count = expenses.size,
            byDay = byDay,
            byCategory = byCategory,
            byEstablishment = byEstablishment,
            byPaymentMethod = byPaymentMethod
        )
    }

    /** Ranking of establishments within a single category (e.g. "Comidas"), with percent relative to that category's total. */
    fun establishmentBreakdownForCategory(expenses: List<ExpenseFull>, categoryId: Long): List<NamedStat> {
        val inCategory = expenses.filter { it.expense.categoryId == categoryId }
        val total = inCategory.sumOf { it.expense.amount }
        return inCategory
            .groupBy { Triple(it.expense.establishmentId, it.establishmentName, it.categoryIcon to it.categoryColor) }
            .map { (key, list) ->
                val (id, name, iconColor) = key
                val sum = list.sumOf { it.expense.amount }
                NamedStat(
                    id = id,
                    name = name,
                    icon = iconColor.first,
                    colorHex = iconColor.second,
                    total = sum,
                    percentOfTotal = if (total > 0) sum / total * 100.0 else 0.0,
                    count = list.size
                )
            }
            .sortedByDescending { it.total }
    }

    fun compare(current: PeriodStats, previous: PeriodStats): PeriodComparison =
        PeriodComparison(current.total, previous.total)

    /** Groups a single day's expenses by category, as shown in the daily analysis screen. */
    fun dayAnalysis(expenses: List<ExpenseFull>, date: LocalDate): DayAnalysis {
        val dayExpenses = expenses.filter { it.expense.date == date }
        val groups = dayExpenses
            .groupBy { it.categoryName to it.categoryIcon }
            .map { (key, items) ->
                CategoryGroup(
                    categoryName = key.first,
                    categoryIcon = key.second,
                    items = items.sortedByDescending { it.expense.amount },
                    total = items.sumOf { it.expense.amount }
                )
            }
            .sortedByDescending { it.total }
        return DayAnalysis(date, groups, dayExpenses.sumOf { it.expense.amount })
    }

    /** Rolls up expenses into one total per calendar month, ordered chronologically. */
    fun byMonth(expenses: List<ExpenseFull>): List<MonthPoint> = expenses
        .groupBy { it.expense.date.year to it.expense.date.monthValue }
        .map { (key, items) ->
            val (year, month) = key
            val label = LocalDate.of(year, month, 1)
                .month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                .replaceFirstChar { it.uppercase() }
            MonthPoint(label, year, month, items.sumOf { it.expense.amount })
        }
        .sortedWith(compareBy({ it.year }, { it.month }))

    /** Total for a category whose name matches [keyword] (case-insensitive, partial match). */
    fun totalForCategoryLike(stats: PeriodStats, keyword: String): Double =
        stats.byCategory.filter { it.name.contains(keyword, ignoreCase = true) }.sumOf { it.total }
}
