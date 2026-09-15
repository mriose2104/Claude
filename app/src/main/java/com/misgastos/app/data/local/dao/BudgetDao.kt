package com.misgastos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.misgastos.app.data.local.entity.AlertSettingsEntity
import com.misgastos.app.data.local.entity.BudgetSettingsEntity
import com.misgastos.app.data.local.entity.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budget_settings WHERE id = 1")
    fun observeBudgetSettings(): Flow<BudgetSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudgetSettings(settings: BudgetSettingsEntity)

    @Query("SELECT * FROM category_budgets")
    fun observeCategoryBudgets(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudget(budget: CategoryBudgetEntity)

    @Delete
    suspend fun deleteCategoryBudget(budget: CategoryBudgetEntity)

    @Query("SELECT * FROM category_budgets WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getCategoryBudget(categoryId: Long): CategoryBudgetEntity?

    @Query("SELECT * FROM alert_settings WHERE id = 1")
    fun observeAlertSettings(): Flow<AlertSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlertSettings(settings: AlertSettingsEntity)
}
