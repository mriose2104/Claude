package com.misgastos.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.misgastos.app.data.local.AppDatabase
import com.misgastos.app.data.local.entity.AlertSettingsEntity
import com.misgastos.app.data.local.entity.BudgetSettingsEntity
import com.misgastos.app.data.local.entity.CategoryBudgetEntity
import com.misgastos.app.data.local.entity.CategoryEntity
import com.misgastos.app.data.local.entity.EstablishmentEntity
import com.misgastos.app.data.local.entity.ExpenseEntity
import com.misgastos.app.data.local.entity.PaymentMethodEntity
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Whole-database JSON backup/restore. Keeps the on-disk format simple and forward compatible so a
 * future version can add cloud sync or multi-device restore on top of the same schema.
 */
object BackupManager {
    private const val BACKUP_VERSION = 1

    private fun backupsDir(context: Context): File =
        File(context.filesDir, "backups").apply { mkdirs() }

    suspend fun exportBackup(context: Context, db: AppDatabase): File {
        val json = JSONObject()
        json.put("version", BACKUP_VERSION)
        json.put("exportedAt", System.currentTimeMillis())

        json.put("categories", JSONArray(db.categoryDao().getAll().map { c ->
            JSONObject().apply {
                put("id", c.id); put("name", c.name); put("icon", c.icon)
                put("colorHex", c.colorHex); put("orderIndex", c.orderIndex)
            }
        }))

        val establishments = db.establishmentDao().observeAll().first()
        json.put("establishments", JSONArray(establishments.map { e ->
            JSONObject().apply {
                put("id", e.id); put("categoryId", e.categoryId); put("name", e.name); put("orderIndex", e.orderIndex)
            }
        }))

        json.put("paymentMethods", JSONArray(db.paymentMethodDao().getAll().map { m ->
            JSONObject().apply {
                put("id", m.id); put("name", m.name); put("icon", m.icon); put("orderIndex", m.orderIndex)
            }
        }))

        json.put("expenses", JSONArray(db.expenseDao().observeAll().first().map { full ->
            val e = full.expense
            JSONObject().apply {
                put("id", e.id); put("uuid", e.uuid)
                put("date", e.date.toString()); put("time", e.time.toString())
                put("amount", e.amount); put("categoryId", e.categoryId)
                put("establishmentId", e.establishmentId); put("paymentMethodId", e.paymentMethodId)
                put("note", e.note); put("createdAt", e.createdAt); put("updatedAt", e.updatedAt)
            }
        }))

        val budgetSettings = db.budgetDao().observeBudgetSettings().first()
        json.put("monthlyBudget", budgetSettings?.monthlyAmount ?: 0.0)

        json.put("categoryBudgets", JSONArray(db.budgetDao().observeCategoryBudgets().first().map { b ->
            JSONObject().apply { put("categoryId", b.categoryId); put("amount", b.amount) }
        }))

        val alertSettings = db.budgetDao().observeAlertSettings().first()
        json.put("alertSettings", JSONObject().apply {
            put("notificationsEnabled", alertSettings?.notificationsEnabled ?: true)
            put("budgetPercentThreshold", alertSettings?.budgetPercentThreshold ?: 80)
            put("dailyLimitEnabled", alertSettings?.dailyLimitEnabled ?: false)
            put("dailyLimitAmount", alertSettings?.dailyLimitAmount ?: 0.0)
            put("categoryBudgetAlertEnabled", alertSettings?.categoryBudgetAlertEnabled ?: true)
            put("weeklySummaryEnabled", alertSettings?.weeklySummaryEnabled ?: false)
            put("monthlySummaryEnabled", alertSettings?.monthlySummaryEnabled ?: false)
        })

        val file = File(backupsDir(context), "mis_gastos_backup.json")
        file.writeText(json.toString(2))
        return file
    }

    suspend fun restoreBackup(context: Context, db: AppDatabase, uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("No se pudo leer el archivo de respaldo")
        val json = JSONObject(text)

        db.withTransaction {
            db.clearAllTables()

            val categories = json.getJSONArray("categories")
            for (i in 0 until categories.length()) {
                val c = categories.getJSONObject(i)
                db.categoryDao().insert(
                    CategoryEntity(
                        name = c.getString("name"),
                        icon = c.getString("icon"),
                        colorHex = c.getString("colorHex"),
                        orderIndex = c.optInt("orderIndex", 0)
                    )
                )
            }
            // Re-map old category ids to new ones (autoGenerate ids reset on insert).
            val newCategories = db.categoryDao().getAll()
            val categoryIdMap = mutableMapOf<Long, Long>()
            for (i in 0 until categories.length()) {
                val c = categories.getJSONObject(i)
                val match = newCategories.elementAtIndexOrNull(i) ?: continue
                categoryIdMap[c.getLong("id")] = match.id
            }

            val establishmentIdMap = mutableMapOf<Long, Long>()
            val establishments = json.getJSONArray("establishments")
            for (i in 0 until establishments.length()) {
                val e = establishments.getJSONObject(i)
                val newCategoryId = categoryIdMap[e.getLong("categoryId")] ?: continue
                val newId = db.establishmentDao().insert(
                    EstablishmentEntity(categoryId = newCategoryId, name = e.getString("name"), orderIndex = e.optInt("orderIndex", 0))
                )
                establishmentIdMap[e.getLong("id")] = newId
            }

            val paymentMethods = json.getJSONArray("paymentMethods")
            val paymentMethodIdMap = mutableMapOf<Long, Long>()
            for (i in 0 until paymentMethods.length()) {
                val m = paymentMethods.getJSONObject(i)
                val newId = db.paymentMethodDao().insert(
                    PaymentMethodEntity(name = m.getString("name"), icon = m.getString("icon"), orderIndex = m.optInt("orderIndex", 0))
                )
                paymentMethodIdMap[m.getLong("id")] = newId
            }

            val expenses = json.getJSONArray("expenses")
            for (i in 0 until expenses.length()) {
                val e = expenses.getJSONObject(i)
                val categoryId = categoryIdMap[e.getLong("categoryId")] ?: continue
                val establishmentId = establishmentIdMap[e.getLong("establishmentId")] ?: continue
                val paymentMethodId = paymentMethodIdMap[e.getLong("paymentMethodId")] ?: continue
                db.expenseDao().insert(
                    ExpenseEntity(
                        uuid = e.optString("uuid", java.util.UUID.randomUUID().toString()),
                        date = LocalDate.parse(e.getString("date")),
                        time = LocalTime.parse(e.getString("time")),
                        amount = e.getDouble("amount"),
                        categoryId = categoryId,
                        establishmentId = establishmentId,
                        paymentMethodId = paymentMethodId,
                        note = if (e.isNull("note")) null else e.optString("note"),
                        createdAt = e.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = e.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            db.budgetDao().upsertBudgetSettings(BudgetSettingsEntity(monthlyAmount = json.optDouble("monthlyBudget", 0.0)))

            val categoryBudgets = json.optJSONArray("categoryBudgets")
            if (categoryBudgets != null) {
                for (i in 0 until categoryBudgets.length()) {
                    val b = categoryBudgets.getJSONObject(i)
                    val categoryId = categoryIdMap[b.getLong("categoryId")] ?: continue
                    db.budgetDao().upsertCategoryBudget(CategoryBudgetEntity(categoryId = categoryId, amount = b.getDouble("amount")))
                }
            }

            json.optJSONObject("alertSettings")?.let { a ->
                db.budgetDao().upsertAlertSettings(
                    AlertSettingsEntity(
                        notificationsEnabled = a.optBoolean("notificationsEnabled", true),
                        budgetPercentThreshold = a.optInt("budgetPercentThreshold", 80),
                        dailyLimitEnabled = a.optBoolean("dailyLimitEnabled", false),
                        dailyLimitAmount = a.optDouble("dailyLimitAmount", 0.0),
                        categoryBudgetAlertEnabled = a.optBoolean("categoryBudgetAlertEnabled", true),
                        weeklySummaryEnabled = a.optBoolean("weeklySummaryEnabled", false),
                        monthlySummaryEnabled = a.optBoolean("monthlySummaryEnabled", false)
                    )
                )
            }
        }
    }

    private fun <T> List<T>.elementAtIndexOrNull(index: Int): T? = if (index in indices) this[index] else null
}
