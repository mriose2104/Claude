package com.misgastos.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.data.local.entity.PaymentMethodEntity
import com.misgastos.app.data.local.relation.ExpenseFull
import com.misgastos.app.data.repository.CatalogRepository
import com.misgastos.app.data.repository.ExpenseRepository
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.util.DateRangeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    DATE_DESC("Más reciente primero"),
    DATE_ASC("Más antiguo primero"),
    AMOUNT_DESC("Monto: mayor a menor"),
    AMOUNT_ASC("Monto: menor a mayor")
}

data class HistoryFilters(
    val dateFilter: DateRangeFilter? = null,
    val categoryId: Long? = null,
    val establishmentId: Long? = null,
    val paymentMethodId: Long? = null,
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.DATE_DESC
)

data class HistoryUiState(
    val filters: HistoryFilters = HistoryFilters(),
    val expenses: List<ExpenseFull> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val establishments: List<EstablishmentEntity> = emptyList(),
    val paymentMethods: List<PaymentMethodEntity> = emptyList(),
    val totalAmount: Double = 0.0,
    val pendingDelete: ExpenseFull? = null
)

class HistoryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val filters = MutableStateFlow(HistoryFilters())

    private val rawExpenses = filters
        .map { it.dateFilter }
        .flatMapLatest { dateFilter ->
            if (dateFilter == null) expenseRepository.observeAllExpenses()
            else expenseRepository.observeExpensesBetween(DateRangeUtils.resolve(dateFilter))
        }

    private val filteredExpenses = combine(rawExpenses, filters) { expenses, f ->
        expenses
            .filter { f.categoryId == null || it.expense.categoryId == f.categoryId }
            .filter { f.establishmentId == null || it.expense.establishmentId == f.establishmentId }
            .filter { f.paymentMethodId == null || it.expense.paymentMethodId == f.paymentMethodId }
            .filter {
                f.searchQuery.isBlank() ||
                    it.establishmentName.contains(f.searchQuery, ignoreCase = true) ||
                    it.categoryName.contains(f.searchQuery, ignoreCase = true) ||
                    it.expense.note?.contains(f.searchQuery, ignoreCase = true) == true
            }
            .let { list ->
                when (f.sortOption) {
                    SortOption.DATE_DESC -> list.sortedWith(compareByDescending<ExpenseFull> { it.expense.date }.thenByDescending { it.expense.time })
                    SortOption.DATE_ASC -> list.sortedWith(compareBy<ExpenseFull> { it.expense.date }.thenBy { it.expense.time })
                    SortOption.AMOUNT_DESC -> list.sortedByDescending { it.expense.amount }
                    SortOption.AMOUNT_ASC -> list.sortedBy { it.expense.amount }
                }
            }
    }

    private val pendingDelete = MutableStateFlow<ExpenseFull?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        filteredExpenses,
        filters,
        catalogRepository.observeCategories(),
        catalogRepository.observeEstablishments(),
        catalogRepository.observePaymentMethods()
    ) { expenses, f, categories, establishments, paymentMethods ->
        HistoryUiState(
            filters = f,
            expenses = expenses,
            categories = categories,
            establishments = establishments,
            paymentMethods = paymentMethods,
            totalAmount = expenses.sumOf { it.expense.amount }
        )
    }.combine(pendingDelete) { state, pending -> state.copy(pendingDelete = pending) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun setDateFilter(filter: DateRangeFilter?) = filters.update { it.copy(dateFilter = filter) }
    fun setCategoryFilter(categoryId: Long?) = filters.update { it.copy(categoryId = categoryId, establishmentId = null) }
    fun setEstablishmentFilter(establishmentId: Long?) = filters.update { it.copy(establishmentId = establishmentId) }
    fun setPaymentMethodFilter(paymentMethodId: Long?) = filters.update { it.copy(paymentMethodId = paymentMethodId) }
    fun setSearchQuery(query: String) = filters.update { it.copy(searchQuery = query) }
    fun setSortOption(option: SortOption) = filters.update { it.copy(sortOption = option) }
    fun clearFilters() = filters.update { HistoryFilters() }

    fun requestDelete(expense: ExpenseFull) { pendingDelete.value = expense }
    fun cancelDelete() { pendingDelete.value = null }
    fun confirmDelete() {
        val expense = pendingDelete.value ?: return
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense.expense)
            pendingDelete.value = null
        }
    }
}
