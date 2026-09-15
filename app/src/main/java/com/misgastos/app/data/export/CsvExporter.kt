package com.misgastos.app.data.export

import android.content.Context
import com.misgastos.app.data.local.relation.ExpenseFull
import com.misgastos.app.domain.model.PeriodStats
import com.misgastos.app.domain.util.CurrencyFormatter
import java.io.File
import java.time.format.DateTimeFormatter

private val csvDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val csvTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

object CsvExporter {

    private fun exportsDir(context: Context): File =
        File(context.cacheDir, "exports").apply { mkdirs() }

    private fun csvEscape(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value

    /** Full movement-by-movement CSV export, usable in Excel/Sheets. */
    fun exportExpensesCsv(context: Context, expenses: List<ExpenseFull>, fileName: String = "mis_gastos.csv"): File {
        val file = File(exportsDir(context), fileName)
        file.bufferedWriter().use { writer ->
            writer.appendLine("Fecha,Hora,Categoria,Establecimiento,Forma de pago,Monto,Nota")
            expenses.forEach { e ->
                writer.appendLine(
                    listOf(
                        e.expense.date.format(csvDateFormatter),
                        e.expense.time.format(csvTimeFormatter),
                        csvEscape(e.categoryName),
                        csvEscape(e.establishmentName),
                        csvEscape(e.paymentMethodName),
                        "%.2f".format(e.expense.amount),
                        csvEscape(e.expense.note.orEmpty())
                    ).joinToString(",")
                )
            }
        }
        return file
    }

    /** Human-readable text report: period, totals, category/establishment breakdown, and full detail. */
    fun exportSummaryReport(
        context: Context,
        periodLabel: String,
        stats: PeriodStats,
        expenses: List<ExpenseFull>,
        fileName: String = "resumen_mis_gastos.txt"
    ): File {
        val file = File(exportsDir(context), fileName)
        file.bufferedWriter().use { writer ->
            writer.appendLine("REPORTE DE GASTOS - MIS GASTOS")
            writer.appendLine("Periodo: $periodLabel")
            writer.appendLine("Total gastado: ${CurrencyFormatter.format(stats.total)}")
            writer.appendLine("Número de movimientos: ${stats.count}")
            writer.appendLine("Promedio diario: ${CurrencyFormatter.format(stats.averageDaily)}")
            writer.appendLine()

            writer.appendLine("GASTOS POR CATEGORÍA")
            stats.byCategory.forEach {
                writer.appendLine("  ${it.icon} ${it.name}: ${CurrencyFormatter.format(it.total)} (${"%.1f".format(it.percentOfTotal)}%)")
            }
            writer.appendLine()

            writer.appendLine("GASTOS POR ESTABLECIMIENTO")
            stats.byEstablishment.forEach {
                writer.appendLine("  ${it.name}: ${CurrencyFormatter.format(it.total)} (${"%.1f".format(it.percentOfTotal)}%)")
            }
            writer.appendLine()

            writer.appendLine("GASTOS POR FORMA DE PAGO")
            stats.byPaymentMethod.forEach {
                writer.appendLine("  ${it.icon} ${it.name}: ${CurrencyFormatter.format(it.total)} (${"%.1f".format(it.percentOfTotal)}%)")
            }
            writer.appendLine()

            writer.appendLine("DETALLE DE MOVIMIENTOS")
            expenses.sortedWith(compareByDescending<ExpenseFull> { it.expense.date }.thenByDescending { it.expense.time })
                .forEach { e ->
                    writer.appendLine(
                        "  ${e.expense.date.format(csvDateFormatter)} ${e.expense.time.format(csvTimeFormatter)} | " +
                            "${e.categoryName} | ${e.establishmentName} | ${e.paymentMethodName} | ${CurrencyFormatter.format(e.expense.amount)}" +
                            if (!e.expense.note.isNullOrBlank()) " | ${e.expense.note}" else ""
                    )
                }

            writer.appendLine()
            writer.appendLine("Las gráficas interactivas de este periodo están disponibles dentro de la app, en la sección Análisis.")
        }
        return file
    }
}
