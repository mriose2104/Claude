package com.misgastos.app.data.repository

import com.misgastos.app.data.local.dao.BudgetDao
import com.misgastos.app.data.local.entity.AlertSettingsEntity
import com.misgastos.app.data.local.entity.BudgetSettingsEntity
import com.misgastos.app.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun observeMonthlyBudget(): Flow<Double> =
        budgetDao.observeBudgetSettings().map { it?.monthlyAmount ?: 0.0 }

    suspend fun setMonthlyBudget(amount: Double) =
        budgetDao.upsertBudgetSettings(BudgetSettingsEntity(monthlyAmount = amount))

    fun observeCategoryBudgets(): Flow<List<CategoryBudgetEntity>> = budgetDao.observeCategoryBudgets()

    suspend fun setCategoryBudget(categoryId: Long, amount: Double) {
        val existing = budgetDao.getCategoryBudget(categoryId)
        budgetDao.upsertCategoryBudget(
            CategoryBudgetEntity(id = existing?.id ?: 0, categoryId = categoryId, amount = amount)
        )
    }

    suspend fun removeCategoryBudget(budget: CategoryBudgetEntity) = budgetDao.deleteCategoryBudget(budget)

    fun observeAlertSettings(): Flow<AlertSettingsEntity> =
        budgetDao.observeAlertSettings().map { it ?: AlertSettingsEntity() }

    suspend fun updateAlertSettings(settings: AlertSettingsEntity) = budgetDao.upsertAlertSettings(settings)
}
