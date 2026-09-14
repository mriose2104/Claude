package com.misgastos.app.domain.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    fun format(amount: Double): String = formatter.format(amount)

    fun formatSigned(amount: Double): String {
        val formatted = formatter.format(kotlin.math.abs(amount))
        return if (amount < 0) "-$formatted" else formatted
    }

    fun formatPercent(percent: Double, showSign: Boolean = true): String {
        val sign = if (showSign && percent > 0) "+" else ""
        return "$sign${"%.2f".format(percent)}%"
    }
}
