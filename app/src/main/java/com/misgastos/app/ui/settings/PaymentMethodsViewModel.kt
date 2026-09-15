package com.misgastos.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.local.entity.PaymentMethodEntity
import com.misgastos.app.data.repository.CatalogRepository
import com.misgastos.app.data.repository.DeleteResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PaymentMethodsUiState(
    val methods: List<PaymentMethodEntity> = emptyList(),
    val message: String? = null
)

class PaymentMethodsViewModel(private val catalogRepository: CatalogRepository) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PaymentMethodsUiState> = combine(
        catalogRepository.observePaymentMethods(),
        message
    ) { methods, msg -> PaymentMethodsUiState(methods, msg) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentMethodsUiState())

    fun add(name: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextIndex = uiState.value.methods.size
            catalogRepository.addPaymentMethod(name.trim(), icon.ifBlank { "💳" }, nextIndex)
        }
    }

    fun update(method: PaymentMethodEntity, name: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            catalogRepository.updatePaymentMethod(method.copy(name = name.trim(), icon = icon.ifBlank { method.icon }))
        }
    }

    fun delete(method: PaymentMethodEntity) {
        viewModelScope.launch {
            when (val result = catalogRepository.deletePaymentMethod(method)) {
                is DeleteResult.Blocked -> message.value =
                    "No se puede eliminar \"${method.name}\": tiene ${result.expenseCount} gasto(s) registrados."
                DeleteResult.Success -> Unit
            }
        }
    }

    fun clearMessage() { message.value = null }
}
