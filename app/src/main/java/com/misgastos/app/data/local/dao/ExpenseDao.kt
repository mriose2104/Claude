package com.misgastos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.misgastos.app.data.local.entity.ExpenseEntity
import com.misgastos.app.data.local.relation.ExpenseFull
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

private const val JOIN_SELECT = """
    SELECT e.*, c.name AS categoryName, c.icon AS categoryIcon, c.colorHex AS categoryColor,
           est.name AS establishmentName, pm.name AS paymentMethodName, pm.icon AS paymentMethodIcon
    FROM expenses e
    INNER JOIN categories c ON c.id = e.categoryId
    INNER JOIN establishments est ON est.id = e.establishmentId
    INNER JOIN payment_methods pm ON pm.id = e.paymentMethodId
"""

@Dao
interface ExpenseDao {

    @Query("$JOIN_SELECT WHERE e.date BETWEEN :start AND :end ORDER BY e.date DESC, e.time DESC, e.id DESC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<ExpenseFull>>

    @Query("$JOIN_SELECT ORDER BY e.date DESC, e.time DESC, e.id DESC")
    fun observeAll(): Flow<List<ExpenseFull>>

    @Query("$JOIN_SELECT WHERE e.id = :id")
    suspend fun getFullById(id: Long): ExpenseFull?

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Query("SELECT MIN(date) FROM expenses")
    fun observeFirstExpenseDate(): Flow<LocalDate?>

    @Query("SELECT COUNT(*) FROM expenses")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}
