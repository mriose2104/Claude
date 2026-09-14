package com.misgastos.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.misgastos.app.data.local.entity.EstablishmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EstablishmentDao {

    @Query("SELECT * FROM establishments ORDER BY orderIndex ASC, name ASC")
    fun observeAll(): Flow<List<EstablishmentEntity>>

    @Query("SELECT * FROM establishments WHERE categoryId = :categoryId ORDER BY orderIndex ASC, name ASC")
    fun observeByCategory(categoryId: Long): Flow<List<EstablishmentEntity>>

    @Query("SELECT * FROM establishments WHERE categoryId = :categoryId ORDER BY orderIndex ASC, name ASC")
    suspend fun getByCategory(categoryId: Long): List<EstablishmentEntity>

    @Query("SELECT * FROM establishments WHERE id = :id")
    suspend fun getById(id: Long): EstablishmentEntity?

    @Query("SELECT COUNT(*) FROM expenses WHERE establishmentId = :establishmentId")
    suspend fun expenseCountForEstablishment(establishmentId: Long): Int

    @Insert
    suspend fun insert(establishment: EstablishmentEntity): Long

    @Insert
    suspend fun insertAll(establishments: List<EstablishmentEntity>): List<Long>

    @Update
    suspend fun update(establishment: EstablishmentEntity)

    @Delete
    suspend fun delete(establishment: EstablishmentEntity)
}
