package com.misgastos.app.data.repository

import com.misgastos.app.data.local.dao.CategoryDao
import com.misgastos.app.data.local.dao.EstablishmentDao
import com.misgastos.app.data.local.dao.PaymentMethodDao
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

sealed class DeleteResult {
    data object Success : DeleteResult()
    data class Blocked(val expenseCount: Int) : DeleteResult()
}

class CatalogRepository(
    private val categoryDao: CategoryDao,
    private val establishmentDao: EstablishmentDao,
    private val paymentMethodDao: PaymentMethodDao
) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun getCategories(): List<CategoryEntity> = categoryDao.getAll()

    fun observeEstablishments(): Flow<List<EstablishmentEntity>> = establishmentDao.observeAll()

    fun observeEstablishmentsForCategory(categoryId: Long): Flow<List<EstablishmentEntity>> =
        establishmentDao.observeByCategory(categoryId)

    fun observePaymentMethods(): Flow<List<PaymentMethodEntity>> = paymentMethodDao.observeAll()

    suspend fun addCategory(name: String, icon: String, colorHex: String, orderIndex: Int): Long =
        categoryDao.insert(CategoryEntity(name = name, icon = icon, colorHex = colorHex, orderIndex = orderIndex))

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun deleteCategory(category: CategoryEntity): DeleteResult {
        val expenseCount = categoryDao.expenseCountForCategory(category.id)
        if (expenseCount > 0) return DeleteResult.Blocked(expenseCount)
        categoryDao.delete(category)
        return DeleteResult.Success
    }

    suspend fun addEstablishment(categoryId: Long, name: String, orderIndex: Int): Long =
        establishmentDao.insert(EstablishmentEntity(categoryId = categoryId, name = name, orderIndex = orderIndex))

    suspend fun updateEstablishment(establishment: EstablishmentEntity) = establishmentDao.update(establishment)

    suspend fun deleteEstablishment(establishment: EstablishmentEntity): DeleteResult {
        val expenseCount = establishmentDao.expenseCountForEstablishment(establishment.id)
        if (expenseCount > 0) return DeleteResult.Blocked(expenseCount)
        establishmentDao.delete(establishment)
        return DeleteResult.Success
    }

    suspend fun addPaymentMethod(name: String, icon: String, orderIndex: Int): Long =
        paymentMethodDao.insert(PaymentMethodEntity(name = name, icon = icon, orderIndex = orderIndex))

    suspend fun updatePaymentMethod(method: PaymentMethodEntity) = paymentMethodDao.update(method)

    suspend fun deletePaymentMethod(method: PaymentMethodEntity): DeleteResult {
        val expenseCount = paymentMethodDao.expenseCountForMethod(method.id)
        if (expenseCount > 0) return DeleteResult.Blocked(expenseCount)
        paymentMethodDao.delete(method)
        return DeleteResult.Success
    }
}
