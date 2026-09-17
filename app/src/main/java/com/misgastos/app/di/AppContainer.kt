package com.misgastos.app.di

import android.content.Context
import androidx.room.Room
import com.misgastos.app.data.local.AppDatabase
import com.misgastos.app.data.notification.AlertNotifier
import com.misgastos.app.data.prefs.UserPreferences
import com.misgastos.app.data.repository.BudgetRepository
import com.misgastos.app.data.repository.CatalogRepository
import com.misgastos.app.data.repository.ExpenseRepository

/** Simple hand-rolled dependency container shared by the whole app (no DI framework needed at this scale). */
class AppContainer(context: Context) {

    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    ).build()

    val expenseRepository: ExpenseRepository by lazy { ExpenseRepository(database.expenseDao()) }

    val catalogRepository: CatalogRepository by lazy {
        CatalogRepository(database.categoryDao(), database.establishmentDao(), database.paymentMethodDao())
    }

    val budgetRepository: BudgetRepository by lazy { BudgetRepository(database.budgetDao()) }

    val userPreferences: UserPreferences by lazy { UserPreferences(context.applicationContext) }

    val alertNotifier: AlertNotifier by lazy { AlertNotifier(context.applicationContext) }
}
