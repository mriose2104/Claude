package com.misgastos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.misgastos.app.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods ORDER BY orderIndex ASC, name ASC")
    fun observeAll(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods ORDER BY orderIndex ASC, name ASC")
    suspend fun getAll(): List<PaymentMethodEntity>

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM expenses WHERE paymentMethodId = :paymentMethodId")
    suspend fun expenseCountForMethod(paymentMethodId: Long): Int

    @Insert
    suspend fun insert(method: PaymentMethodEntity): Long

    @Insert
    suspend fun insertAll(methods: List<PaymentMethodEntity>): List<Long>

    @Update
    suspend fun update(method: PaymentMethodEntity)

    @Delete
    suspend fun delete(method: PaymentMethodEntity)
}
