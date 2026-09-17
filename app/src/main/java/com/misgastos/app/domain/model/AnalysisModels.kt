package com.misgastos.app.domain.model

import com.misgastos.app.data.local.relation.ExpenseFull
import java.time.LocalDate

data class CategoryGroup(
    val categoryName: String,
    val categoryIcon: String,
    val items: List<ExpenseFull>,
    val total: Double
)

data class DayAnalysis(
    val date: LocalDate,
    val groups: List<CategoryGroup>,
    val total: Double
)

data class MonthPoint(val monthLabel: String, val year: Int, val month: Int, val total: Double)
