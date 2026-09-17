package com.misgastos.app.data.seed

import com.misgastos.app.data.local.AppDatabase
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.data.local.entity.PaymentMethodEntity

/**
 * Populates the database with the default categories, establishments and payment methods
 * described in the product spec. Runs once, the first time the app opens on an empty database.
 */
object SeedData {

    suspend fun seedIfEmpty(db: AppDatabase) {
        val categoryDao = db.categoryDao()
        val establishmentDao = db.establishmentDao()
        val paymentMethodDao = db.paymentMethodDao()

        if (categoryDao.count() > 0) return

        val comidasId = categoryDao.insert(
            CategoryEntity(name = "Comidas", icon = "🍔", colorHex = "#FF7043", orderIndex = 0)
        )
        val casaId = categoryDao.insert(
            CategoryEntity(name = "Casa / Servicios", icon = "🏠", colorHex = "#42A5F5", orderIndex = 1)
        )

        val comidasEstablishments = listOf(
            "KFC", "Carl's Jr", "Asadero El Primo", "Tacos Alex", "Salads", "Pizza",
            "Delicias", "Grill House", "Teo", "Barbacoa", "Don Pancho", "Otros"
        )
        establishmentDao.insertAll(
            comidasEstablishments.mapIndexed { index, name ->
                EstablishmentEntity(categoryId = comidasId, name = name, orderIndex = index)
            }
        )

        val casaEstablishments = listOf(
            "Tienda", "Clase de monta", "Agua", "Luz", "Total Play", "Gas", "Aurrera", "Gasolina", "Otros"
        )
        establishmentDao.insertAll(
            casaEstablishments.mapIndexed { index, name ->
                EstablishmentEntity(categoryId = casaId, name = name, orderIndex = index)
            }
        )

        val paymentMethods = listOf(
            Triple("Efectivo", "💵", 0),
            Triple("Tarjeta de débito", "💳", 1),
            Triple("Tarjeta de crédito", "💳", 2),
            Triple("Transferencia", "🏦", 3),
            Triple("Otro", "🔖", 4)
        )
        paymentMethodDao.insertAll(
            paymentMethods.map { (name, icon, order) ->
                PaymentMethodEntity(name = name, icon = icon, orderIndex = order)
            }
        )
    }
}
