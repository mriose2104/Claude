package com.misgastos.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.misgastos.app.domain.util.CurrencyFormatter
import com.misgastos.app.ui.theme.extended

@Composable
fun BudgetProgressCard(
    title: String,
    spent: Double,
    budget: Double,
    modifier: Modifier = Modifier,
    warningThresholdPercent: Int = 80
) {
    val extended = MaterialTheme.extended
    val percent = if (budget > 0) (spent / budget * 100.0).coerceAtLeast(0.0) else 0.0
    val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val available = (budget - spent).coerceAtLeast(0.0)

    val (statusText, statusColor) = when {
        budget <= 0 -> "Sin presupuesto definido" to MaterialTheme.colorScheme.onSurfaceVariant
        percent >= 100 -> "🔴 Presupuesto excedido" to extended.danger
        percent >= warningThresholdPercent -> "🟡 Cerca del límite (${"%.0f".format(percent)}% usado)" to extended.warning
        else -> "🟢 Dentro del presupuesto" to extended.expenseDown
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Gastado: ${CurrencyFormatter.format(spent)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Disponible: ${CurrencyFormatter.format(available)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (budget > 0) {
                Text(
                    "Presupuesto: ${CurrencyFormatter.format(budget)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                statusText,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
