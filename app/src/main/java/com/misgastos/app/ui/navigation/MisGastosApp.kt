package com.misgastos.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.misgastos.app.ui.addexpense.AddExpenseScreen
import com.misgastos.app.ui.analysis.AnalysisScreen
import com.misgastos.app.ui.dashboard.DashboardScreen
import com.misgastos.app.ui.history.HistoryScreen
import com.misgastos.app.ui.settings.AlertsScreen
import com.misgastos.app.ui.settings.BackupScreen
import com.misgastos.app.ui.settings.BudgetsScreen
import com.misgastos.app.ui.settings.CategoriesScreen
import com.misgastos.app.ui.settings.PaymentMethodsScreen
import com.misgastos.app.ui.settings.SettingsScreen

@Composable
fun MisGastosApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route ||
                        (item.route == Routes.ADD_EXPENSE && currentRoute == Routes.ADD_EXPENSE)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            val target = if (item.route == Routes.ADD_EXPENSE) Routes.addExpense() else item.route
                            navController.navigate(target) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateToAddExpense = { navController.navigate(Routes.addExpense()) },
                    onNavigateToAnalysis = { navController.navigate(Routes.ANALYSIS) },
                    onNavigateToHistory = { navController.navigate(Routes.HISTORY) }
                )
            }
            composable(
                route = Routes.ADD_EXPENSE,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId")?.takeIf { it >= 0 }
                AddExpenseScreen(
                    expenseId = expenseId,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Routes.ANALYSIS) { AnalysisScreen() }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    onEditExpense = { id -> navController.navigate(Routes.addExpense(id)) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateToCategories = { navController.navigate(Routes.SETTINGS_CATEGORIES) },
                    onNavigateToPaymentMethods = { navController.navigate(Routes.SETTINGS_PAYMENT_METHODS) },
                    onNavigateToBudgets = { navController.navigate(Routes.SETTINGS_BUDGETS) },
                    onNavigateToAlerts = { navController.navigate(Routes.SETTINGS_ALERTS) },
                    onNavigateToBackup = { navController.navigate(Routes.SETTINGS_BACKUP) }
                )
            }
            composable(Routes.SETTINGS_CATEGORIES) { CategoriesScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS_PAYMENT_METHODS) { PaymentMethodsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS_BUDGETS) { BudgetsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS_ALERTS) { AlertsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS_BACKUP) { BackupScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
