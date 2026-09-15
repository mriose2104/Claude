package com.misgastos.app.data.repository

import com.misgastos.app.data.local.dao.ExpenseDao
import com.misgastos.app.data.local.entity.ExpenseEntity
import com.misgastos.app.data.local.relation.ExpenseFull
import com.misgastos.app.domain.model.DateRange
import com.misgastos.app.domain.model.PeriodStats
import com.misgastos.app.domain.util.StatsCalculator
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun observeExpensesBetween(range: DateRange): Flow<List<ExpenseFull>> =
        expenseDao.observeBetween(range.start, range.end)

    fun observeAllExpenses(): Flow<List<ExpenseFull>> = expenseDao.observeAll()

    fun observeStats(range: DateRange): Flow<PeriodStats> =
        expenseDao.observeBetween(range.start, range.end).map { StatsCalculator.compute(it, range) }

    fun observeFirstExpenseDate(): Flow<LocalDate?> = expenseDao.observeFirstExpenseDate()

    fun observeCount(): Flow<Int> = expenseDao.observeCount()

    suspend fun getFullById(id: Long): ExpenseFull? = expenseDao.getFullById(id)

    suspend fun addExpense(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun updateExpense(expense: ExpenseEntity) =
        expenseDao.update(expense.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.delete(expense)
}
