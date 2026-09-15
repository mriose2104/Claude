package com.misgastos.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.repository.BudgetRepository
import com.misgastos.app.data.repository.CatalogRepository
import com.misgastos.app.data.repository.ExpenseRepository
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.util.DateRangeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryBudgetRow(val category: CategoryEntity, val budgetAmount: Double, val spent: Double)

data class BudgetsUiState(
    val monthlyBudget: Double = 0.0,
    val monthSpent: Double = 0.0,
    val categoryRows: List<CategoryBudgetRow> = emptyList()
)

class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val catalogRepository: CatalogRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val monthStats = expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.ThisMonth))

    val uiState: StateFlow<BudgetsUiState> = combine(
        budgetRepository.observeMonthlyBudget(),
        budgetRepository.observeCategoryBudgets(),
        catalogRepository.observeCategories(),
        monthStats
    ) { monthlyBudget, categoryBudgets, categories, stats ->
        val rows = categories.map { category ->
            val budgetAmount = categoryBudgets.find { it.categoryId == category.id }?.amount ?: 0.0
            val spent = stats.byCategory.find { it.id == category.id }?.total ?: 0.0
            CategoryBudgetRow(category, budgetAmount, spent)
        }
        BudgetsUiState(monthlyBudget = monthlyBudget, monthSpent = stats.total, categoryRows = rows)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetsUiState())

    fun setMonthlyBudget(amount: Double) {
        viewModelScope.launch { budgetRepository.setMonthlyBudget(amount) }
    }

    fun setCategoryBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch { budgetRepository.setCategoryBudget(categoryId, amount) }
    }
}
