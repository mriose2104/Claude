package com.misgastos.app.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.data.local.entity.ExpenseEntity
import com.misgastos.app.data.local.entity.PaymentMethodEntity
import com.misgastos.app.data.notification.AlertNotifier
import com.misgastos.app.data.repository.BudgetRepository
import com.misgastos.app.data.repository.CatalogRepository
import com.misgastos.app.data.repository.ExpenseRepository
import com.misgastos.app.domain.model.DateRangeFilter
import com.misgastos.app.domain.util.DateRangeUtils
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddExpenseUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val establishments: List<EstablishmentEntity> = emptyList(),
    val paymentMethods: List<PaymentMethodEntity> = emptyList(),
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val amountText: String = "",
    val selectedCategoryId: Long? = null,
    val selectedEstablishmentId: Long? = null,
    val selectedPaymentMethodId: Long? = null,
    val note: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val saveError: String? = null,
    val saved: Boolean = false
) {
    val selectedCategory: CategoryEntity? get() = categories.find { it.id == selectedCategoryId }
    val selectedPaymentMethod: PaymentMethodEntity? get() = paymentMethods.find { it.id == selectedPaymentMethodId }
}

class AddExpenseViewModel(
    private val expenseId: Long?,
    private val expenseRepository: ExpenseRepository,
    private val catalogRepository: CatalogRepository,
    private val budgetRepository: BudgetRepository,
    private val alertNotifier: AlertNotifier
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseUiState(isEditing = expenseId != null))
    val state: StateFlow<AddExpenseUiState> = _state

    private var defaultsApplied = false

    init {
        viewModelScope.launch {
            combine(
                catalogRepository.observeCategories(),
                catalogRepository.observePaymentMethods()
            ) { categories, methods -> categories to methods }
                .collect { (categories, methods) ->
                    _state.update { it.copy(categories = categories, paymentMethods = methods, isLoading = false) }
                    applyDefaultsIfNeeded()
                }
        }

        viewModelScope.launch {
            _state.map { it.selectedCategoryId }
                .distinctUntilChanged()
                .flatMapLatest { categoryId ->
                    if (categoryId == null) flowOf(emptyList()) else catalogRepository.observeEstablishmentsForCategory(categoryId)
                }
                .collect { establishments ->
                    _state.update { current ->
                        val stillValid = establishments.any { it.id == current.selectedEstablishmentId }
                        current.copy(
                            establishments = establishments,
                            selectedEstablishmentId = if (stillValid) current.selectedEstablishmentId else establishments.firstOrNull()?.id
                        )
                    }
                }
        }

        if (expenseId != null) {
            viewModelScope.launch {
                val full = expenseRepository.getFullById(expenseId) ?: return@launch
                _state.update {
                    it.copy(
                        date = full.expense.date,
                        time = full.expense.time,
                        amountText = trimAmount(full.expense.amount),
                        selectedCategoryId = full.expense.categoryId,
                        selectedEstablishmentId = full.expense.establishmentId,
                        selectedPaymentMethodId = full.expense.paymentMethodId,
                        note = full.expense.note.orEmpty()
                    )
                }
                defaultsApplied = true
            }
        }
    }

    private fun trimAmount(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()

    private fun applyDefaultsIfNeeded() {
        if (defaultsApplied || expenseId != null) return
        val current = _state.value
        if (current.categories.isEmpty()) return
        defaultsApplied = true
        _state.update {
            it.copy(
                selectedCategoryId = it.selectedCategoryId ?: current.categories.first().id,
                selectedPaymentMethodId = it.selectedPaymentMethodId ?: current.paymentMethods.firstOrNull()?.id
            )
        }
    }

    fun setDate(date: LocalDate) = _state.update { it.copy(date = date) }
    fun setTime(time: LocalTime) = _state.update { it.copy(time = time) }
    fun setAmountText(text: String) {
        val sanitized = text.filter { it.isDigit() || it == '.' }
        _state.update { it.copy(amountText = sanitized, saveError = null) }
    }
    fun setCategory(categoryId: Long) = _state.update { it.copy(selectedCategoryId = categoryId) }
    fun setEstablishment(establishmentId: Long) = _state.update { it.copy(selectedEstablishmentId = establishmentId) }
    fun setPaymentMethod(paymentMethodId: Long) = _state.update { it.copy(selectedPaymentMethodId = paymentMethodId) }
    fun setNote(note: String) = _state.update { it.copy(note = note) }

    fun save() {
        val s = _state.value
        val amount = s.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _state.update { it.copy(saveError = "Ingresa un monto válido") }
            return
        }
        if (s.selectedCategoryId == null) {
            _state.update { it.copy(saveError = "Selecciona una categoría") }
            return
        }
        if (s.selectedEstablishmentId == null) {
            _state.update { it.copy(saveError = "Selecciona un establecimiento") }
            return
        }
        if (s.selectedPaymentMethodId == null) {
            _state.update { it.copy(saveError = "Selecciona una forma de pago") }
            return
        }

        viewModelScope.launch {
            if (expenseId != null) {
                val existing = expenseRepository.getFullById(expenseId)?.expense ?: return@launch
                expenseRepository.updateExpense(
                    existing.copy(
                        date = s.date,
                        time = s.time,
                        amount = amount,
                        categoryId = s.selectedCategoryId,
                        establishmentId = s.selectedEstablishmentId,
                        paymentMethodId = s.selectedPaymentMethodId,
                        note = s.note.ifBlank { null }
                    )
                )
            } else {
                expenseRepository.addExpense(
                    ExpenseEntity(
                        date = s.date,
                        time = s.time,
                        amount = amount,
                        categoryId = s.selectedCategoryId,
                        establishmentId = s.selectedEstablishmentId,
                        paymentMethodId = s.selectedPaymentMethodId,
                        note = s.note.ifBlank { null }
                    )
                )
                runCatching { checkAlerts(amount, s.selectedCategoryId, s.date) }
            }
            _state.update { it.copy(saved = true) }
        }
    }

    private suspend fun checkAlerts(amount: Double, categoryId: Long, date: LocalDate) {
        val settings = budgetRepository.observeAlertSettings().first()
        if (!settings.notificationsEnabled) return

        if (settings.dailyLimitEnabled && settings.dailyLimitAmount > 0 && date == LocalDate.now()) {
            val todayTotal = expenseRepository.observeStats(DateRangeUtils.resolve(DateRangeFilter.Today)).first().total
            val previousTotal = todayTotal - amount
            if (previousTotal < settings.dailyLimitAmount && todayTotal >= settings.dailyLimitAmount) {
                alertNotifier.notifyDailyLimitExceeded(todayTotal, settings.dailyLimitAmount)
            }
        }

        val monthRange = DateRangeUtils.resolve(DateRangeFilter.ThisMonth)
        val monthStats = expenseRepository.observeStats(monthRange).first()

        if (settings.categoryBudgetAlertEnabled) {
            val categoryBudget = budgetRepository.observeCategoryBudgets().first().find { it.categoryId == categoryId }
            if (categoryBudget != null && categoryBudget.amount > 0) {
                val categoryTotal = monthStats.byCategory.find { it.id == categoryId }?.total ?: 0.0
                val previousTotal = categoryTotal - amount
                val categoryName = monthStats.byCategory.find { it.id == categoryId }?.name ?: ""
                checkThresholdCrossing(previousTotal, categoryTotal, categoryBudget.amount, settings.budgetPercentThreshold) { exceeded ->
                    if (exceeded) alertNotifier.notifyCategoryBudgetExceeded(categoryName)
                    else alertNotifier.notifyCategoryBudgetWarning(categoryName, settings.budgetPercentThreshold)
                }
            }
        }

        val monthlyBudget = budgetRepository.observeMonthlyBudget().first()
        if (monthlyBudget > 0) {
            val previousTotal = monthStats.total - amount
            checkThresholdCrossing(previousTotal, monthStats.total, monthlyBudget, settings.budgetPercentThreshold) { exceeded ->
                if (exceeded) alertNotifier.notifyMonthlyBudgetExceeded()
                else alertNotifier.notifyMonthlyBudgetWarning(settings.budgetPercentThreshold)
            }
        }
    }

    private inline fun checkThresholdCrossing(
        previousTotal: Double,
        newTotal: Double,
        budget: Double,
        warningPercent: Int = 80,
        onCross: (exceeded: Boolean) -> Unit
    ) {
        if (budget <= 0) return
        val previousPercent = previousTotal / budget * 100.0
        val newPercent = newTotal / budget * 100.0
        if (previousPercent < 100.0 && newPercent >= 100.0) {
            onCross(true)
        } else if (previousPercent < warningPercent && newPercent >= warningPercent) {
            onCross(false)
        }
    }
}
