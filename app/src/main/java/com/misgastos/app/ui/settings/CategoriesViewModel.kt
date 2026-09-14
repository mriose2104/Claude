package com.misgastos.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.data.repository.CatalogRepository
import com.misgastos.app.data.repository.DeleteResult
import com.misgastos.app.ui.theme.CategoryPalette
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val establishmentsByCategory: Map<Long, List<EstablishmentEntity>> = emptyMap(),
    val message: String? = null
)

class CategoriesViewModel(private val catalogRepository: CatalogRepository) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        catalogRepository.observeCategories(),
        catalogRepository.observeEstablishments(),
        message
    ) { categories, establishments, msg ->
        CategoriesUiState(
            categories = categories,
            establishmentsByCategory = establishments.groupBy { it.categoryId },
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriesUiState())

    fun addCategory(name: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextIndex = uiState.value.categories.size
            val colorHex = String.format(
                "#%06X",
                0xFFFFFF and CategoryPalette[nextIndex % CategoryPalette.size].toArgb()
            )
            catalogRepository.addCategory(name.trim(), icon.ifBlank { "🏷️" }, colorHex, nextIndex)
        }
    }

    fun updateCategory(category: CategoryEntity, name: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            catalogRepository.updateCategory(category.copy(name = name.trim(), icon = icon.ifBlank { category.icon }))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            when (val result = catalogRepository.deleteCategory(category)) {
                is DeleteResult.Blocked -> message.value =
                    "No se puede eliminar \"${category.name}\": tiene ${result.expenseCount} gasto(s) registrados."
                DeleteResult.Success -> Unit
            }
        }
    }

    fun addEstablishment(categoryId: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextIndex = uiState.value.establishmentsByCategory[categoryId]?.size ?: 0
            catalogRepository.addEstablishment(categoryId, name.trim(), nextIndex)
        }
    }

    fun updateEstablishment(establishment: EstablishmentEntity, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            catalogRepository.updateEstablishment(establishment.copy(name = name.trim()))
        }
    }

    fun deleteEstablishment(establishment: EstablishmentEntity) {
        viewModelScope.launch {
            when (val result = catalogRepository.deleteEstablishment(establishment)) {
                is DeleteResult.Blocked -> message.value =
                    "No se puede eliminar \"${establishment.name}\": tiene ${result.expenseCount} gasto(s) registrados."
                DeleteResult.Success -> Unit
            }
        }
    }

    fun clearMessage() { message.value = null }
}
