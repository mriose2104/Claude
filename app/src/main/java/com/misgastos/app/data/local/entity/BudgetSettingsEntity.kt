package com.misgastos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table holding the overall monthly budget (applies to every calendar month). */
@Entity(tableName = "budget_settings")
data class BudgetSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyAmount: Double = 0.0
)
