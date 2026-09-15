package com.misgastos.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.local.entity.AlertSettingsEntity
import com.misgastos.app.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertsViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    val settings: StateFlow<AlertSettingsEntity> = budgetRepository.observeAlertSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertSettingsEntity())

    fun update(block: (AlertSettingsEntity) -> AlertSettingsEntity) {
        viewModelScope.launch { budgetRepository.updateAlertSettings(block(settings.value)) }
    }
}
