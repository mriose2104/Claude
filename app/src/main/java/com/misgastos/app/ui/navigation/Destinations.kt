package com.misgastos.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add_expense?expenseId={expenseId}"
    const val ANALYSIS = "analysis"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val SETTINGS_CATEGORIES = "settings/categories"
    const val SETTINGS_PAYMENT_METHODS = "settings/payment_methods"
    const val SETTINGS_BUDGETS = "settings/budgets"
    const val SETTINGS_ALERTS = "settings/alerts"
    const val SETTINGS_BACKUP = "settings/backup"

    fun addExpense(expenseId: Long? = null): String =
        if (expenseId == null) "add_expense" else "add_expense?expenseId=$expenseId"
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Inicio", Icons.Filled.Home),
    BottomNavItem(Routes.ADD_EXPENSE, "Nuevo gasto", Icons.Filled.Add),
    BottomNavItem(Routes.ANALYSIS, "Análisis", Icons.Filled.BarChart),
    BottomNavItem(Routes.HISTORY, "Historial", Icons.Filled.History),
    BottomNavItem(Routes.SETTINGS, "Configuración", Icons.Filled.Settings)
)
