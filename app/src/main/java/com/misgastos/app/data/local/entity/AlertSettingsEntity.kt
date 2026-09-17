package com.misgastos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_settings")
data class AlertSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val notificationsEnabled: Boolean = true,
    val budgetPercentThreshold: Int = 80,
    val dailyLimitEnabled: Boolean = false,
    val dailyLimitAmount: Double = 0.0,
    val categoryBudgetAlertEnabled: Boolean = true,
    val weeklySummaryEnabled: Boolean = false,
    val monthlySummaryEnabled: Boolean = false
)
