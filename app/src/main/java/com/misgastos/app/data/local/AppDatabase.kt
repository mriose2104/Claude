package com.misgastos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.misgastos.app.data.local.dao.BudgetDao
import com.misgastos.app.data.local.dao.CategoryDao
import com.misgastos.app.data.local.dao.EstablishmentDao
import com.misgastos.app.data.local.dao.ExpenseDao
import com.misgastos.app.data.local.dao.PaymentMethodDao
import com.misgastos.app.data.local.entity.AlertSettingsEntity
import com.misgastos.app.data.local.entity.BudgetSettingsEntity
import com.misgastos.app.data.local.entity.CategoryBudgetEntity
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.data.local.entity.ExpenseEntity
import com.misgastos.app.data.local.entity.PaymentMethodEntity

@Database(
    entities = [
        CategoryEntity::class,
        EstablishmentEntity::class,
        PaymentMethodEntity::class,
        ExpenseEntity::class,
        BudgetSettingsEntity::class,
        CategoryBudgetEntity::class,
        AlertSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun establishmentDao(): EstablishmentDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val DATABASE_NAME = "mis_gastos.db"
    }
}
