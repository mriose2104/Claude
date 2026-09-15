package com.misgastos.app.data.local.relation

import androidx.room.Embedded
import com.misgastos.app.data.local.entity.ExpenseEntity

/** An expense joined with the human-readable names of its category, establishment and payment method. */
data class ExpenseFull(
    @Embedded val expense: ExpenseEntity,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val establishmentName: String,
    val paymentMethodName: String,
    val paymentMethodIcon: String
)
